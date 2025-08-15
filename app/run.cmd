if exist javaHome.cmd (
    call javaHome.cmd
)

rem one directory up for the application-default.properties
cd ..
for %%f in (app\target\calendarAggregator-*.jar) do (
    echo %%f
	call %JAVA_HOME%\bin\java -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dloader.path=. -Dloader.home=. -jar %%f
)
pause
