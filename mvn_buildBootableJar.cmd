if exist javaHome.cmd (
    call javaHome.cmd
)

call mvnw clean package -DskipTests -Pproduction
pause
