rem just in case
call podman machine start

call mvnw versions:set
call mvnw clean install -DskipTests -Pproduction -Pcontainer
pause