/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cnx.repository.atompub.utils;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Generator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringWriter;


/**
 * Utility class to print PrettyXmls for JDOM.
 *
 * @author Arjun Satyapal
 */
public class PrettyXmlOutputter {
    // Utility class.
    private PrettyXmlOutputter() {
    }

    public static String prettyXmlOutputDocument(Document doc) {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        return xmlOutputter.outputString(doc);
    }

    public static String prettyXmlOutputElement(Element element) {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        return xmlOutputter.outputString(element);
    }

    public static String prettyXmlOutputEntry(Entry entry) throws IllegalArgumentException, FeedException, IOException {
        StringWriter writer = new StringWriter();
        Atom10Generator.serializeEntry(entry, writer);
//        CnxAtomPubConstants.serializeEntry(entry, writer);
        return writer.toString();
    }

    public static String prettyXmlOutputMyEntry(Entry entry) throws IllegalArgumentException, FeedException, IOException {
        StringWriter writer = new StringWriter();
        CnxAtomPubConstants.serializeEntry(entry, writer);
        return writer.toString();
    }

}
