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

import static javapan.ParameterNameWriter.NAME_SUFFIX;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

public class ParameterNameReader {

	public static List<String> getParameterNames(Method method) {
		return lookUp(method.getDeclaringClass(), method);
	}

	public static List<String> getParameterNames(Constructor<?> contor) {
		return lookUp(contor.getDeclaringClass(), contor);
	}

	private static List<String> lookUp(Class<?> type, AccessibleObject obj) {
		if (type.isAnonymousClass()) {
			throw new IllegalArgumentException("anonymous classes are not supported");
		}

		String paramClassName = getParamClassName(type);
		String paramfieldName = getParamFieldName(obj, type);

		try {

			Class<?> paramClass = type.getClassLoader().loadClass(paramClassName);
			Field field = paramClass.getField(paramfieldName);

			return getFieldValue(field, null);

		} catch (NoSuchFieldException e) {
			if (isNullaryConstructor(obj)) {
				return Collections.emptyList();
			}

			throw throwException(type, obj, e);
		} catch (Exception e) {
			throw throwException(type, obj, e);
		}
	}

	private static boolean isNullaryConstructor(AccessibleObject o) {
		if (o instanceof Constructor) {
			Constructor<?> contor = (Constructor<?>) o;
			if (contor.getParameterTypes().length == 0) {
				return true;
			}
		}

		return false;
	}

	private static RuntimeException throwException(Class<?> type, AccessibleObject obj, Exception e) {
		String msg = String.format("cannot find parameter names for %s in %s", obj, type);
		throw new NotFoundException(msg, e);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getFieldValue(Field f, Object o) throws Exception {
		return (T) f.get(o);
	}

	private static boolean isStaticClassConstructor(AccessibleObject obj, Class<?> type) {
		if (!(obj instanceof Constructor)) {
			return false;
		}
		if (type.getEnclosingClass() == null) {
			return false;
		}

		int mod = type.getModifiers();
		return !Modifier.isStatic(mod) && !Modifier.isInterface(mod);
	}

	private static String getParamFieldName(AccessibleObject obj, Class<?> type) {
		String name = null;
		int numParams = isStaticClassConstructor(obj, type) ? -1 : 0;

		if (obj instanceof Constructor) {
			Constructor<?> contor = (Constructor<?>) obj;
			name = "$init$";
			numParams += contor.getParameterTypes().length;
		} else {
			Method m = (Method) obj;
			name = m.getName();
			numParams += m.getParameterTypes().length;
		}

		return String.format("%s_%s$%s", name, numParams, NAME_SUFFIX);
	}

	private static String getParamClassName(Class<?> type) {
		String typeFqName = type.getName();
		return typeFqName + NAME_SUFFIX;
	}
}
