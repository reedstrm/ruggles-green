/*
    Copyright 2011 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cnx.html;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.SoyDataException;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;
import java.util.List;
import java.util.Set;

/**
  SoyExtras contains functions needed by the Soy templates for HTML generation.
*/
class SoyExtras extends AbstractModule {
  @Override public void configure() {
    final Multibinder<SoyFunction> soyFunctionsSetBinder = Multibinder.newSetBinder(binder(), SoyFunction.class);
    soyFunctionsSetBinder.addBinding().to(FindElemFunction.class);
    soyFunctionsSetBinder.addBinding().to(InnerTextFunction.class);
  }

  /**
    FindElemFunction provides the <code>findElem()</code> function to Soy.
    The function takes two arguments: the first is a DOM node (transformed into
    a Soy map), and the second is a string.  It returns the first child node of
    the DOM node whose tag matches the string.
  */
  @Singleton
  private static class FindElemFunction implements SoyTofuFunction {
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
        throw new IllegalArgumentException("Argument 1 to findElem() function is not SoyMapData");
      }

      try {
        name = args.get(1).stringValue();
      } catch (SoyDataException e) {
        throw new IllegalArgumentException("Argument 2 to findElem() function is not String");
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
    InnerTextFunction provides the <code>innerText()</code> function to Soy.
    The function takes one argument: a DOM node.  It returns the concatenation
    of the text nodes inside the node.
  */
  @Singleton
  private static class InnerTextFunction implements SoyTofuFunction {
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
        throw new IllegalArgumentException("Argument 1 to innerText() function is not SoyMapData");
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
}
