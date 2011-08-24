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

import static org.cnx.cnxml.SoyStringConstants.ATTRIBUTE_NODES;
import static org.cnx.cnxml.SoyStringConstants.AUDIO;
import static org.cnx.cnxml.SoyStringConstants.CDATA;
import static org.cnx.cnxml.SoyStringConstants.CHILD_NODES;
import static org.cnx.cnxml.SoyStringConstants.DOWNLOAD;
import static org.cnx.cnxml.SoyStringConstants.ELEMENT;
import static org.cnx.cnxml.SoyStringConstants.FIND_ELEM;
import static org.cnx.cnxml.SoyStringConstants.FLASH;
import static org.cnx.cnxml.SoyStringConstants.IMAGE;
import static org.cnx.cnxml.SoyStringConstants.INNER_TEXT;
import static org.cnx.cnxml.SoyStringConstants.JAVA_APPLET;
import static org.cnx.cnxml.SoyStringConstants.LABVIEW;
import static org.cnx.cnxml.SoyStringConstants.LOCAL_NAME;
import static org.cnx.cnxml.SoyStringConstants.MATH_CHILDREN_JAVA;
import static org.cnx.cnxml.SoyStringConstants.MEDIA;
import static org.cnx.cnxml.SoyStringConstants.NAMESPACE_URI;
import static org.cnx.cnxml.SoyStringConstants.NODE_TYPE;
import static org.cnx.cnxml.SoyStringConstants.NODE_VALUE;
import static org.cnx.cnxml.SoyStringConstants.OBJECT;
import static org.cnx.cnxml.SoyStringConstants.PDF;
import static org.cnx.cnxml.SoyStringConstants.STR_TO_DECIMAL;
import static org.cnx.cnxml.SoyStringConstants.TEXT;
import static org.cnx.cnxml.SoyStringConstants.VIDEO;

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
 * SoyExtras contains functions needed by the Soy templates for HTML generation.
 */
class SoyExtras extends AbstractModule {
    @Override
    public void configure() {
        final Multibinder<SoyFunction> soyFunctionsSetBinder =
                Multibinder.newSetBinder(binder(), SoyFunction.class);
        soyFunctionsSetBinder.addBinding().to(FindElemFunction.class);
        soyFunctionsSetBinder.addBinding().to(InnerTextFunction.class);
        soyFunctionsSetBinder.addBinding().to(MathChildrenJavaFunction.class);
        soyFunctionsSetBinder.addBinding().to(StrtodFunction.class);
        soyFunctionsSetBinder.addBinding().to(MediaFunction.class);
    }

    /**
     * FindElemFunction provides the <code>findElem()</code> function to Soy. The function takes two
     * arguments: the first is a DOM node (transformed into a Soy map), and the second is a string.
     * It returns the first child node of the DOM node whose tag matches the string.
     */
    @Singleton
    private static class FindElemFunction implements SoyTofuFunction {
        @Inject
        public FindElemFunction() {
        }

        @Override
        public String getName() {
            return FIND_ELEM.getSoyName();
        }

        @Override
        public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(2);
        }

        @Override
        public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;
            String name;

            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData) args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to " + getName() + "() function is not SoyMapData");
            }

            try {
                name = args.get(1).stringValue();
            } catch (SoyDataException e) {
                throw new IllegalArgumentException(
                        "Argument 2 to findElem() function is not String");
            }

            for (SoyData item : elem.getListData(CHILD_NODES.getSoyName())) {
                if (item instanceof SoyMapData) {
                    if (name.equals(((SoyMapData) item).getString(LOCAL_NAME.getSoyName()))) {
                        return item;
                    }
                }
            }
            return NullData.INSTANCE;
        }
    }

    /**
     * InnerTextFunction provides the <code>innerText()</code> function to Soy. The function takes
     * one argument: a DOM node. It returns the concatenation of the text nodes inside the node.
     */
    @Singleton
    private static class InnerTextFunction implements SoyTofuFunction {
        @Inject
        public InnerTextFunction() {
        }

        @Override
        public String getName() {
            return INNER_TEXT.getSoyName();
        }

        @Override
        public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override
        public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;
            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData) args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to " + getName() + "() function is not SoyMapData");
            }
            // Find text nodes in children
            final SoyListData childNodes = elem.getListData(CHILD_NODES.getSoyName());
            final StringBuilder builder = new StringBuilder();
            for (SoyData childData : childNodes) {
                if (!(childData instanceof SoyMapData)) {
                    continue;
                }

                final SoyMapData child = (SoyMapData) childData;
                final String nodeType = child.getString(NODE_TYPE.getSoyName());
                if ("text".equals(nodeType) || CDATA.getSoyName().equals(nodeType)) {
                    builder.append(child.getString(NODE_VALUE.getSoyName()));
                }
            }
            return new StringData(builder.toString());
        }
    }

    /**
     * MathChildrenJavaFunction takes a DOM node (in soy data format) and returns a string
     * representing its rendering in HTML.
     *
     * TODO(tal): take care of escaping.
     */
    @Singleton
    private static class MathChildrenJavaFunction implements SoyTofuFunction {
        @Inject
        public MathChildrenJavaFunction() {
        }

        @Override
        public String getName() {
            return MATH_CHILDREN_JAVA.getSoyName();
        }

        @Override
        public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override
        public SoyData computeForTofu(List<SoyData> args) {
            final SoyMapData node;
            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                node = (SoyMapData) args.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Argument 1 to " + getName() + "() function is not SoyMapData");
            }

            final StringBuilder builder = new StringBuilder();
            internalCompute(builder, node);

            return new StringData(builder.toString());
        }


        private void internalCompute(StringBuilder builder, SoyMapData node) {

            final SoyListData childList = node.getListData(CHILD_NODES.getSoyName());

            for (int i = 0; i < childList.length(); i++) {
                final SoyMapData child = childList.getMapData(i);

                final String childType = child.getString(NODE_TYPE.getSoyName());

                if (TEXT.getSoyName().equals(childType)) {
                    builder.append(child.getString(NODE_VALUE.getSoyName()));
                } else if (ELEMENT.getSoyName().equals(childType)) {
                    builder.append("<");
                    builder.append(child.getString(LOCAL_NAME.getSoyName()));

                    final SoyListData attributeList =
                        child.getListData(ATTRIBUTE_NODES.getSoyName());
                    for (int j = 0; j < attributeList.length(); j++) {
                        final SoyMapData attribute = attributeList.getMapData(j);
                        //System.out.println("*** Attribute: " + attribute);
                        builder.append(" ");
                        builder.append(attribute.getString(LOCAL_NAME.getSoyName()));
                        builder.append("=\"");
                        builder.append(attribute.getString(NODE_VALUE.getSoyName()));
                        builder.append("\"");
                    }
                    builder.append(">");
                    internalCompute(builder, child);  // recursion
                    builder.append("</");
                    builder.append(child.getString(LOCAL_NAME.getSoyName()));
                    builder.append(">");
                }
                // else ignore silently
            }

        }
    }

    /**
     * StrtodFunction provides the <code>strtod()</code> function to Soy. The function takes one
     * argument: a string. It returns the equivalent integer, or null if the string does not
     * represent an integer.
     */
    @Singleton
    private static class StrtodFunction implements SoyTofuFunction {
        @Inject
        public StrtodFunction() {
        }

        @Override
        public String getName() {
            return STR_TO_DECIMAL.getSoyName();
        }

        @Override
        public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override
        public SoyData computeForTofu(List<SoyData> args) {
            String str;

            // TODO(light): Better error messages
            if (args.get(0) instanceof StringData) {
                str = ((StringData) args.get(0)).stringValue();
            } else {
                throw new IllegalArgumentException("Argument 1 to " + getName()
                        + "() function is not string");
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
     * MediaFunction provides the <code>media()</code> function to Soy. The function takes one
     * argument: a CNXML DOM node. It returns the first valid media DOM node.
     */
    @Singleton
    private static class MediaFunction implements SoyTofuFunction {
        private static final ImmutableSet<String> MEDIA_ELEMENTS = ImmutableSet.of(
            AUDIO.getSoyName(),
            DOWNLOAD.getSoyName(),
            FLASH.getSoyName(),
            IMAGE.getSoyName(),
            JAVA_APPLET.getSoyName(),
            LABVIEW.getSoyName(),
            OBJECT.getSoyName(),
            VIDEO.getSoyName());

        private static final String OVERRIDE_MEDIA_VALUE = SoyStringConstants.WEBVIEW_2_0
            .getSoyName();

        private final String cnxmlNamespace;

        @Inject
        public MediaFunction(@CnxmlNamespace String cnxmlNamespace) {
            this.cnxmlNamespace = cnxmlNamespace;
        }

        @Override
        public String getName() {
            return MEDIA.getSoyName();
        }

        @Override
        public Set<Integer> getValidArgsSizes() {
            return ImmutableSet.of(1);
        }

        @Override
        public SoyData computeForTofu(List<SoyData> args) {
            SoyMapData elem;

            // TODO(light): Better error messages
            if (args.get(0) instanceof SoyMapData) {
                elem = (SoyMapData) args.get(0);
            } else {
                throw new IllegalArgumentException("Argument 1 to " + getName()
                        + "() function is not SoyMapData");
            }

            // Find first appropriate child node
            final SoyListData childNodes = elem.getListData(CHILD_NODES.getSoyName());
            SoyData firstNode = null;
            for (SoyData childData : childNodes) {
                if (!(childData instanceof SoyMapData)) {
                    continue;
                }

                final SoyMapData child = (SoyMapData) childData;
                String mediaFor = null;
                try {
                    mediaFor = child.getString("attributes.for");
                } catch (IllegalArgumentException e) {
                    // The element has no "for" attribute. Guice is documented to return null for
                    // a missing key, but actually throws an exception. Exception can be safely
                    // ignored.
                }
                if ("element".equals(child.getString(NODE_TYPE.getSoyName()))
                        && cnxmlNamespace.equals(child.getString(NAMESPACE_URI.getSoyName()))
                        && MEDIA_ELEMENTS.contains(child.getString(LOCAL_NAME.getSoyName()))) {
                    if (OVERRIDE_MEDIA_VALUE.equals(mediaFor)) {
                        // A special for="webview2.0" attribute is used to force the media element
                        // to be used. This is necessary to provide backward compatibility with
                        // cnx.org and still allow Mathematica to be embedded.
                        return child;
                    } else if (!PDF.getSoyName().equals(mediaFor) && firstNode == null) {
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