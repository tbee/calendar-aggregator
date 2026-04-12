mvnw versions:set -DgenerateBackupPoms=false -DprocessAllModules=true -DgroupId='*'
mvnw clean package -Pproduction $@
