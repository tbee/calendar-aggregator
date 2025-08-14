rem call mvn_buildDistributable.cmd -Pcontainer

call ..\mvnw install -DskipTests -Pcontainer
pause
