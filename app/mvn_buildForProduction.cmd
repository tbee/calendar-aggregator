cd ..
call mvnw versions:set -DgenerateBackupPoms=false -DprocessAllModules=true -DgroupId='*'
cd app
call ..\mvnw clean package -DskipTests -Pproduction
pause 

# run with: java -jar target\calendarAggregator-*.jar
