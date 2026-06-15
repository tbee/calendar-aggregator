rem just in case
call podman machine start
call podman container prune --force
call podman pod prune --force

call mvnw versions:set
call mvnw clean install -DskipTests -Pproduction -Pcontainer
pause