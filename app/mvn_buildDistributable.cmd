if exist javaHome.cmd (
    call javaHome.cmd
)

cd ..
call mvnw versions:set -DgenerateBackupPoms=false -DprocessAllModules=true -DgroupId='*'

cd app
call ..\mvnw -version
call ..\mvnw clean package -DskipTests -Pproduction %*
pause 

rem run with: java -jar target\calendarAggregator-*.jar
