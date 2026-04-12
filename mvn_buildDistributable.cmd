if exist javaHome.cmd (
    call javaHome.cmd
)

call mvnw versions:set -DgenerateBackupPoms=false -DprocessAllModules=true -DgroupId='*'
call mvnw clean package -Pproduction %*
pause 
