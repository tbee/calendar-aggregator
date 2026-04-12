:: Kill existing java processes
for /f "tokens=1" %%a in ('jps -l ^| findstr "calendarAggregator"') do taskkill /f /pid %%a

:: Use 'call' to ensure the script waits for Maven to finish
call mvnw validate -P run

:: kill afterwards as well
for /f "tokens=1" %%a in ('jps -l ^| findstr "calendarAggregator"') do taskkill /f /pid %%a

:: Keep the window open if it crashes or finishes
pause
