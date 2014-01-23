/*
Copyright (c) 2013 JAVAPAN
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holders nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
 */

package javapan;

import java.util.HashMap;
import java.util.Map;

class StringReplacer {
	private static final String LINE_SEP = System.getProperty("line.separator");

	private Map<String, String> vars = new HashMap<String, String>();

	public StringReplacer() {

	}

	public StringReplacer addVariable(String key, String value) {
		vars.put(key, value);
		return this;
	}

	public String replace(String source) {
		StringBuffer sb = new StringBuffer(source.length());

		char[] array = source.toCharArray();
		int i = 0;
		while (i < array.length - 1) {
			if (startsWith(array, i, "${")) {
				i = i + 2;
				int begin = i;
				while (array[i] != '}') {
					i++;
				}

				String key = source.substring(begin, i++);
				String value = vars.get(key);
				sb.append(value);

			} else if (startsWith(array, i, "%n")) {
				sb.append(LINE_SEP);
				i++;
				i++;
			} else {
				sb.append(array[i]);
				i++;
			}
		}

		if (i < array.length)
			sb.append(array[i]);

		return sb.toString();
	}

	private boolean startsWith(char[] array, int i, String twoToken) {
		boolean twoMoreLeft = (i + 1) < array.length - 1;
		if (twoMoreLeft) {
			return array[i] == twoToken.charAt(0) && array[i + 1] == twoToken.charAt(1);
		}
		return false;
	}
}