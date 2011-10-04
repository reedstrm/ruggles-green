/*
 * Copyright (C) 2011 The CNX Authors
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
package org.cnx.common.repository;

import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxInternalServerErrorException;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Generator;

/**
 * Utility class to print PrettyXmls for JDOM.
 * 
 * @author Arjun Satyapal
 * 
 * TODO(arjuns) : Rename this to StringConverter or some better name.
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

    public static String prettyXmlOutputEntry(Entry entry) throws CnxException {
        StringWriter writer = new StringWriter();
        try {
            Atom10Generator.serializeEntry(entry, writer);
        } catch (IllegalArgumentException e) {
            throw new CnxInternalServerErrorException("Invalid XML", e);
        } catch (FeedException e) {
            throw new CnxInternalServerErrorException("FeedException", e);
        } catch (IOException e) {
            throw new CnxInternalServerErrorException("IoException", e);
        }
        return writer.toString();
    }
}
