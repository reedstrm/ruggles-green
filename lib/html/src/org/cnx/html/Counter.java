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

import java.util.HashMap;
import java.util.Map;

/**
    Counter provides monotonically increasing numbers for CNXML elements like list, equation, and
    proof.

    This class is package-private; it should not be used outside of org.cnx.html.
*/
class Counter {
    /**
        A CounterKey instance is a reference to a counter.  Each element (e.g. list, equation,
        proof, etc.) has a separate namespace for counters, and the counters are identified by
        an attribute called type.
    */
    private static final class CounterKey {
        private String element;
        private String type;

        public CounterKey(String e, String t) {
            element = e;
            type = t;
        }

        public String getElement() {
            return element;
        }

        public String getType() {
            return type;
        }

        public int hashCode() {
            return (element + type).hashCode();
        }

        public boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            } else if (otherObject == null) {
                return false;
            } else if (otherObject instanceof CounterKey) {
                final CounterKey other = (CounterKey)otherObject;
                return element.equals(other.element) && type.equals(other.type);
            }
            return false;
        }
    }

    private Map<CounterKey, Integer> counters;

    public Counter() {
        counters = new HashMap<CounterKey, Integer>();
    }

    /**
        getNextNumber returns the next 1-based number for the counter and increments the counter.

        @param element The tag of the counted element
        @param type The type attribute of the counted element
    */
    public int getNextNumber(String element, String type) {
        final CounterKey key = new CounterKey(element, type);
        Integer i = counters.get(key);

        if (i == null) {
            i = 1;
        }
        counters.put(key, i + 1);
        return i;
    }

    /**
        reset sets the counter such that the next call to
        {@link #getNextNumber(String, String)} with the given parameters will return 1.

        @param element The tag of the counted element
        @param type The type attribute of the counted element
    */
    public void reset(String element, String type) {
        counters.remove(new CounterKey(element, type));
    }
}
