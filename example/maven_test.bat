cd ../javapan && ^
mvn clean install && ^
cd ../example && ^
mvn clean install && ^
cd target && ^
java -Dexample.isMavenBuild=true -jar example-0.0.2-SNAPSHOT-jar-with-dependencies.jar && ^
cd ..

