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

package org.cnx.cnxml;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;

import java.net.URI;
import java.net.URISyntaxException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 *  Links provides methods for working with link attributes encountered in CNXML.
 *  <p>
 *  A link is not the same as a URI.  A link may contain spaces and Unicode characters.
 */
public final class Links {
    public final static String FRAGMENT = "#";

    // These constants are defined in RFC3986.
    // http://tools.ietf.org/html/rfc3986
    private static final CharMatcher URI_RESERVED = CharMatcher.anyOf(":/?#[]@!$&'()*+,;=");
    private static final CharMatcher URI_UNRESERVED = CharMatcher.anyOf("-._~")
            .or(CharMatcher.inRange('A', 'Z'))
            .or(CharMatcher.inRange('a', 'z'))
            .or(CharMatcher.inRange('0', '9')).precomputed();
    private static final CharMatcher URI_CHAR =
            CharMatcher.is('%').or(URI_RESERVED).or(URI_UNRESERVED).precomputed();

    /**
     *  Converts a link into a proper URI.
     *  <p>
     *  This should be used for <code>url</code> and <code>src</code> attributes.
     */
    public static URI convertLinkAttributeToUri(final String attrValue) throws URISyntaxException {
        return new URI(convert(checkNotNull(attrValue), URI_CHAR));
    }

    /**
     *  Converts a file name into a URI.
     *  <p>
     *  This should be used for resource names and the like.
     */
    public static URI convertFileNameToUri(final String fileName) throws URISyntaxException {
        return new URI(convert(checkNotNull(fileName), URI_UNRESERVED));
    }

    /**
     *  Converts a string into a URI by escaping characters that are not in the safe character set.
     *
     *  @param input The string to convert
     *  @param safe The characters to leave unescaped
     *  @return The corresponding URI string
     */
    private static String convert(final String input, final CharMatcher safe) {
        final StringBuilder sb = new StringBuilder();

        // XXX(light): Ideally, you should be able to wrap the input directly.  Sadly, this fails
        // because of this bug:
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4997655
        final CharBuffer cbuf = CharBuffer.wrap(input.toCharArray());
        boolean escaping = false;

        while (cbuf.hasRemaining()) {
            // Find the boundaries of the current run (safe content or non-safe content)
            final int index = (escaping ? safe : safe.negate()).indexIn(cbuf);
            final CharBuffer slice = cbuf.slice();
            if (index != -1) {
                slice.limit(index);
            }

            // Escape if necessary, otherwise, copy the current run straight to the new string.
            if (escaping) {
                encode(sb, slice);
            } else {
                sb.append(slice);
            }

            // Advance position
            cbuf.position(cbuf.position() + slice.limit());

            // Alternate escaping and non-escaping
            escaping = !escaping;
        }
        return sb.toString();
    }

    /**
     *  Percent escapes a sequence of characters.
     *
     *  @param sb The string builder to store the escape in
     *  @param cbuf The characters to encode
     */
    private static void encode(final StringBuilder sb, final CharBuffer cbuf) {
        final ByteBuffer buf = Charsets.UTF_8.encode(cbuf);
        while (buf.hasRemaining()) {
            // Get next character
            final byte b = buf.get();

            // XXX(light): Don't remove the mask. Remember that Java doesn't have unsigned values.
            final String hex = Integer.toHexString(((int)b) & 0x000000ff).toUpperCase();

            // Build percent escape
            sb.append('%');
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
    }
}
