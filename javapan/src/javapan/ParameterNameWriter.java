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

import java.io.*;
import java.util.*;

import javax.tools.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.*;

@SupportedAnnotationTypes("*")
public class ParameterNameWriter extends AbstractProcessor {

	static final String NAME_SUFFIX = "$$javapan";

	private Elements elementUtils;

	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		processElements(rootElements);
		return false;
	}

	private void processElements(Collection<? extends Element> rootElements) {
		Collection<? extends TypeElement> typeElements = ElementFilter.typesIn(rootElements);

		for (TypeElement type : typeElements) {
			processElements(type.getEnclosedElements());
			processType(type);
		}
	}

	private void processType(TypeElement type) {
		if (type.getQualifiedName().toString().endsWith(NAME_SUFFIX))
			return;

		try {
			writeSourceFile(type);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
					"Could not create source file for " + type + ": " + e);
		}
	}

	private void writeSourceFile(TypeElement type) throws IOException {
		// e.g. com.example.FooBar$$javapan
		String generatedClassName = getParamClassName(type);

		// e.g. FooBar$$javapan
		String genSimpleClassName = substringAfterLast(generatedClassName, ".");

		PackageElement pkgElem = elementUtils.getPackageOf(type);
		String packageName = pkgElem == null ? null : pkgElem.getQualifiedName().toString();

		String sourceTemplate = "" + "// ${gen.source.file}.java - generated by ${writer.name}%n"
								+ "${package.declaration}%n"
								+ "%n"
								+ "public final class ${gen.source.file} { %n"
								+ "%n"
								+ "\tprivate ${gen.source.file}() { throw new java.lang.AssertionError(); }%n"
								+ "%n"
								+ "${gen.fields}"
								+ "%n"
								+ "}";

		String genFields = generateFields(type);

		StringReplacer replacer = new StringReplacer();
		replacer.addVariable("writer.name", ParameterNameWriter.class.getName());
		if (packageName != null && !packageName.isEmpty()) {
			String pkgStatement = String.format("package %s;", packageName);
			replacer.addVariable("package.declaration", pkgStatement);
		}else {
			replacer.addVariable("package.declaration", "");
		}
		replacer.addVariable("gen.source.file", genSimpleClassName);
		replacer.addVariable("gen.fields", genFields);

		String genSource = replacer.replace(sourceTemplate);

		createSourceFile(generatedClassName, genSource);
	}

	private void createSourceFile(String fileName, String fileContent) throws IOException {
		JavaFileObject sourceFile = filer.createSourceFile(fileName);
		OutputStream os = sourceFile.openOutputStream();
		PrintWriter pw = new PrintWriter(os);
		pw.print(fileContent);
		pw.close();
		os.close();
	}

	private String generateFields(TypeElement type) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		List<? extends Element> elements = type.getEnclosedElements();
		List<ExecutableElement> contors = ElementFilter.constructorsIn(elements);
		writeParamNameFields(pw, contors);

		List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
		writeParamNameFields(pw, methods);

		pw.close();
		return sw.toString();
	}

	private void writeParamNameFields(PrintWriter pw, Collection<ExecutableElement> elements) {
		Set<String> writtenFields = new HashSet<String>();

		for (ExecutableElement e : elements) {
			writeParamNameField(pw, e, writtenFields);
		}
	}

	private void writeParamNameField(PrintWriter pw, ExecutableElement method, Set<String> writtenFields) {
		List<? extends VariableElement> parameters = method.getParameters();
		
		String fieldName = getParamFieldName(method);
		if(writtenFields.contains(fieldName))
			return;
		writtenFields.add(fieldName);
		
		pw.printf("\tpublic static final java.util.List<String> %s = ", fieldName);

		if (parameters.isEmpty()) {
			pw.printf("%n\t\t\tjava.util.Collections.emptyList();%n");
			pw.println();

		} else {
			pw.printf("%n\t\t\tjava.util.Collections.unmodifiableList(java.util.Arrays.asList(");

			boolean first = true;
			for (VariableElement param : parameters) {
				if (!first) {
					pw.print(", ");
				}
				pw.printf("\"%s\"", param.getSimpleName());
				first = false;
			}
			pw.println("));");
			pw.println();
		}
	}

	private String getParamFieldName(ExecutableElement method) {
		List<? extends VariableElement> parameters = method.getParameters();

		String name = method.getSimpleName().toString();
		if (name.equals("<init>")) {
			name = "$init$";
		}
		return String.format("%s_%s$%s", name, parameters.size(), NAME_SUFFIX);
	}

	private String getParamClassName(TypeElement type) {
		String binaryName = elementUtils.getBinaryName(type).toString();
		return binaryName + NAME_SUFFIX;
	}

	private static String substringAfterLast(String str, String separator) {
		int pos = str.lastIndexOf(separator);
		if (pos == -1 || pos == (str.length() - separator.length())) {
			return str;
		}
		return str.substring(pos + separator.length());
	}
}
