if [ -f javaHome.sh ]; then
   javaHome.sh
fi

cd ..
mvnw versions:set -DgenerateBackupPoms=false -DprocessAllModules=true -DgroupId='*'

cd app
../mvnw clean package -DskipTests -Pproduction $@

# run with: java -jar target/calendarAggregator-*.jar
