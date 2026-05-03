call mvnw versions:set
call mvnw clean install -DskipTests -Pproduction -Pcontainer
pause