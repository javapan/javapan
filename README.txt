JAVAPAN

JAVAPAN is a java library that provides the ability to reflectively access method/constructor parameter names for Java 6+
This is accomplished via a JSR-269 annotation processing compiler plug-in.

1. Dependencies
 JAVAPAN requires Java version 6 or higher for annotation processing.

2. Quick Start
 a. download javapan.jar and javapan-services.jar.
 b. place them in classpath when compiling your code. (see special Eclipse instructions below)
 c. access the paramter names at runtime using javapan.ParameterNameReader class
 
 Example:
   C:\workspace\example>javac -cp .;lib\javapan.jar;lib\javapan-services.jar -d bin src\com\example\*.java

   C:\workspace\example>java -cp bin;lib\javapan.jar;lib com.example.Main
   it works!
   
  There is also a test_cli.bat in example project that runs the entire pipeline.
  
3. Eclipse Build
  If you build and run your code with Eclipse, you need to do the following after performing step 2b.
  
  Select your project. 
  Go to Project -> Properties -> Java Compiler -> Annotation Processing
  Check "Enable project specific settings"
  Check "Enable annotation processing"
  Go to Annotation Processing -> Factory Path
  Check "Enable project specific settings"
  Using "Add JARS" or "Add External JARS", add javapan.jar and javapan-services.jar
  
  Re-build your project.

4. Maven Build
  To build javapan library, run:
  	C:\workspace\javapan>mvn clean install

  To build example project, run:
        C:\workspace\example>mvn clean install

  There is also a test_mvn.bat in example project that runs the entire pipeline using maven.
  
5. Bugs & Known Issues
  JAVAPAN currently does not work very well with overloaded methods/constructors. It may not return correct parameter names
  for a method at runtime if the class contains another method with the same name and same number of parameters.