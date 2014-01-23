mkdir ..\core\bin
javac -g -d ..\core\bin ..\core\src\javapan\*.java
jar -cMf lib\javapan.jar -C ..\core\bin\ javapan
jar -cMf lib\javapan-services.jar -C ..\core\src\ META-INF
jar -cMf ..\downloads\javapan.jar -C ..\core\bin\ javapan
jar -cMf ..\downloads\javapan-services.jar -C ..\core\src\ META-INF
REM javac -J"-Xrunjdwp:transport=dt_socket,server=y,address=85" -g -cp .;lib\javapan.jar;lib\javapan-services.jar -d bin src\com\example\*.java
mkdir bin
javac -g -cp .;lib\javapan.jar;lib\javapan-services.jar -AParameterNameWriter.includePattern=".+Main(.+)?" -d bin src\com\example\*.java