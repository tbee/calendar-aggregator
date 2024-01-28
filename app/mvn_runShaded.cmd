if exist javaHome.cmd (
    call javaHome.cmd
)

cd ..
for %%f in (app\target\calendarAggregator-*.shaded.jar) do (
    echo %%f
	call %JAVA_HOME%\bin\java -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -jar %%f
)
pause
