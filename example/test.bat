javac -g -d ..\javapan\bin ..\javapan\src\javapan\*.java
jar -cMf lib\javapan.jar -C ..\javapan\bin\ javapan
jar -cMf lib\javapan-services.jar -C ..\javapan\src\ META-INF
jar -cMf ..\downloads\javapan.jar -C ..\javapan\bin\ javapan
jar -cMf ..\downloads\javapan-services.jar -C ..\javapan\src\ META-INF
REM javac -J"-Xrunjdwp:transport=dt_socket,server=y,address=85" -g -cp .;lib\javapan.jar;lib\javapan-services.jar -d bin src\com\example\*.java
javac -g -cp .;lib\javapan.jar;lib\javapan-services.jar -d bin src\com\example\*.java
java -cp bin;lib\javapan.jar;lib\javapan-services.jar com.example.Main