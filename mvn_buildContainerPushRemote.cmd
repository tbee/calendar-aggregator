call mvnw versions:set
call mvnw clean install -DskipTests -Pproduction -Pcontainer -Pcontainer-remote
pause