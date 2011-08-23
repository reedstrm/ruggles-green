/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.cnxml;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.SoyDataException;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;
import java.util.List;
import java.util.Set;

/**
 *  SoyExtras contains functions needed by the Soy templates for HTML generation.
 */
class SoyExtras extends AbstractModule {
    @Override public void configure() {
        final Multibinder<SoyFunction> soyFunctionsSetBinder =
                Multibinder.newSetBinder(binder(), SoyFunction.class);
        soyFunctionsSetBinder.addBinding().to(FindElemFunction.class);
        soyFunctionsSetBinder.addBinding().to(InnerTextFunction.class);
        soyFunctionsSetBinder.addBinding().to(StrtodFunction.class);
        soyFunctionsSetBinder.addBinding().to(MediaFunction.class);
    }

    /**
     *  FindElemFunction provides the <code>findElem()</code> function to Soy.
     *  The function takes two arguments: the first is a DOM node (transformed into
     *  a Soy map), and the second is a string.  It returns the first child node of
     *  the DOM node whose tag matches the string.
     */
    @Singleton private static class FindElemFunction implements SoyTofuFunction {
        private static final String NAME = "findElem";

        @Inject public FindElemFunction() {}

        @Override public String getName() {
            return NAME;
        }

        @Override public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(2);
        }

        @Override public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;
            String name;

            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData)args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to findElem() function is not SoyMapData");
            }

            try {
                name = args.get(1).stringValue();
            } catch (SoyDataException e) {
                throw new IllegalArgumentException(
                        "Argument 2 to findElem() function is not String");
            }

            for (SoyData item : elem.getListData("childNodes")) {
                if (item instanceof SoyMapData) {
                    if (name.equals(((SoyMapData)item).getString("localName"))) {
                        return item;
                    }
                }
            }
            return NullData.INSTANCE;
        }
    }

    /**
     *  InnerTextFunction provides the <code>innerText()</code> function to Soy.
     *  The function takes one argument: a DOM node.    It returns the concatenation
     *  of the text nodes inside the node.
     */
    @Singleton private static class InnerTextFunction implements SoyTofuFunction {
        private static final String NAME = "innerText";

        @Inject public InnerTextFunction() {}

        @Override public String getName() {
            return NAME;
        }

        @Override public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;
            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData)args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to innerText() function is not SoyMapData");
            }
            // Find text nodes in children
            final SoyListData childNodes = elem.getListData("childNodes");
            final StringBuilder builder = new StringBuilder();
            for (SoyData childData : childNodes) {
                if (!(childData instanceof SoyMapData)) {
                    continue;
                }

                final SoyMapData child = (SoyMapData)childData;
                final String nodeType = child.getString("nodeType");
                if ("text".equals(nodeType) || "cdata".equals(nodeType)) {
                    builder.append(child.getString("nodeValue"));
                }
            }
            return new StringData(builder.toString());
        }
    }

    /**
     *  StrtodFunction provides the <code>strtod()</code> function to Soy.
     *  The function takes one argument: a string.  It returns the equivalent
     *  integer, or null if the string does not represent an integer.
     */
    @Singleton private static class StrtodFunction implements SoyTofuFunction {
        private static final String NAME = "strtod";

        @Inject public StrtodFunction() {}

        @Override public String getName() {
            return NAME;
        }

        @Override public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override public SoyData computeForTofu(List<SoyData> args) {
            String str;

            // TODO(light): Better error messages
            if (args.get(0) instanceof StringData) {
                str = ((StringData)args.get(0)).stringValue();
            } else {
                throw new IllegalArgumentException("Argument 1 to " + NAME + "() function is not string");
            }

            try {
                return new IntegerData(Integer.valueOf(str));
            } catch (NumberFormatException e) {
                // If the string does not represent an integer, then return Soy null.
                return NullData.INSTANCE;
            }
        }
    }

    /**
     *  MediaFunction provides the <code>media()</code> function to Soy.
     *  The function takes one argument: a CNXML DOM node.  It returns the first valid media
     *  DOM node.
     */
    @Singleton private static class MediaFunction implements SoyTofuFunction {
        private static final String NAME = "media";
        private static final ImmutableSet<String> MEDIA_ELEMENTS = ImmutableSet.of(
                "audio",
                "download",
                "flash",
                "image",
                "java-applet",
                "labview",
                "object",
                "video"
        );
        private static final String PDF_MEDIA_VALUE = "pdf";
        private static final String OVERRIDE_MEDIA_VALUE = "webview2.0";

        private final String cnxmlNamespace;

        @Inject public MediaFunction(@CnxmlNamespace String cnxmlNamespace) {
            this.cnxmlNamespace = cnxmlNamespace;
        }

        @Override public String getName() {
            return NAME;
        }

        @Override public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;

            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData)args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to " + NAME + "() function is not SoyMapData");
            }

            // Find first appropriate child node
            final SoyListData childNodes = elem.getListData("childNodes");
            SoyData firstNode = null;
            for (SoyData childData : childNodes) {
                if (!(childData instanceof SoyMapData)) {
                    continue;
                }

                final SoyMapData child = (SoyMapData)childData;
                String mediaFor = null;
                try {
                    mediaFor = child.getString("attributes.for");
                } catch (IllegalArgumentException e) {
                    // The element has no "for" attribute.  Guice is documented to return null for
                    // a missing key, but actually throws an exception.  Exception can be safely
                    // ignored.
                }
                if ("element".equals(child.getString("nodeType"))
                        && cnxmlNamespace.equals(child.getString("namespaceURI"))
                        && MEDIA_ELEMENTS.contains(child.getString("localName"))) {
                    if (OVERRIDE_MEDIA_VALUE.equals(mediaFor)) {
                        // A special for="webview2.0" attribute is used to force the media element
                        // to be used.  This is necessary to provide backward compatibility with
                        // cnx.org and still allow Mathematica to be embedded.
                        return child;
                    } else if (!PDF_MEDIA_VALUE.equals(mediaFor) && firstNode == null) {
                        firstNode = child;
                    }
                }
            }

            if (firstNode != null) {
                return firstNode;
            }
            return NullData.INSTANCE;
        }
    }
}
