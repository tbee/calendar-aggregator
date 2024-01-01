call ..\mvnw versions:set
call ..\mvnw clean package -DskipTests -Pproduction
pause 

# run with: java -jar target\calendarAggregator-*.jar
