rem current directory name as window title
for %%I in (.) do title %%~nxI

if exist javaHome.cmd (
    call javaHome.cmd
)

for %%f in (..\..\app\target\calendarAggregator-*.jar) do (
    echo %%f
	call %JAVA_HOME%\bin\java -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dloader.path=. -Dloader.home=. -jar %%f
)
pause
