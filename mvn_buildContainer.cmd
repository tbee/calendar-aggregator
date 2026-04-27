rem toolchain takes care of the correct JVM
call mvnw clean package -DskipTests -Pproduction -Pcontainer
pause
