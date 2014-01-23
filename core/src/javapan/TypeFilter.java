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

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class TypeFilter {

	private String includePatternKey;

	private Pattern includePatternValue;

	public TypeFilter() {
		this(ParameterNameWriter.class.getSimpleName() + ".");
	}

	public TypeFilter(String prefix) {
		includePatternKey = prefix + "includePattern";
	}

	public String getIncludePatternKey() {
		return includePatternKey;
	}

	public void setIncludePatternKey(String includePatternKey) {
		this.includePatternKey = includePatternKey;
	}

	public void addKeysTo(Set<String> options) {
		if (includePatternKey != null) {
			options.add(includePatternKey);
		}
	}

	public void initValues(ProcessingEnvironment env) {
		String value = env.getOptions().get(includePatternKey);
		if (value != null) {
			try {
				includePatternValue = Pattern.compile(value);
			} catch (PatternSyntaxException e) {
				String msg = String.format("Invalid include pattern '%s'. %s", value,
						e.getMessage());
				env.getMessager().printMessage(Kind.ERROR, msg);
			}
		}
	}

	public boolean isIncluded(TypeElement element) {

		if (includePatternValue != null) {
			String name = element.getQualifiedName().toString();
			if (!includePatternValue.matcher(name).matches()) {
				return false;
			}
		}

		return true;
	}
}