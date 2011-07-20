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

package org.cnx.repository.common;

import org.cnx.util.Assertions;

/**
 * TODO(tal): transformation to/from base64 web safe is naive. Consider to
 * optimize if needed (e.g. lookup tables).
 * 
 * TODO(tal): consider to add to the string id checksum bits.
 * 
 * @author Tal Dayan
 */
public class KeyUtil {

	// Charset based on rfc3548. Instead of padding by '=' we extend the
	// 64 bit long into a 5*13=65 bit value by appending a zero MSB bit.
	//
	// TODO(tal): change the string to an array of chars for better efficiency?
	private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "-_";

	public static String idToString(String prefix, long id) {
		// Java longs are defines as 64 bit values but let's verify it just
		// in case.
		Assertions.check(Long.MAX_VALUE == 9223372036854775807L, "%s",
				Long.MAX_VALUE);
		Assertions.check(Long.MIN_VALUE == -9223372036854775808L, "%s",
				Long.MIN_VALUE);

		StringBuilder builder = new StringBuilder();

		builder.append(prefix);

		for (int i = 60; i >= 0; i -= 5) {
			int index = (int) ((id >>> i) & 0x1f);
			builder.append(chars.charAt(index));
		}
		return builder.toString();
	}

	// Return null if error
	public static Long stringToId(String expectedPrefix, String id) {

		if (!id.startsWith(expectedPrefix)) {
			return null;
		}

		final String subId = id.substring(expectedPrefix.length());

		// Expecting exactly ceiling(64/5) chars.
		if (subId.length() != 13) {
			return null;
		}

		long value = 0;

		for (int i = 0; i < id.length(); i++) {
			final char c = id.charAt(i);
			int index = chars.indexOf(c);
			if (index < 0) {
				return null; // bad char
			}
			value = (value << 5) + index;
		}

		return value;
	}
}
