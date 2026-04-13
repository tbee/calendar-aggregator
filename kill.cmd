:: Kill existing java processes
for /f "tokens=1" %%a in ('jps -l ^| findstr "calendarAggregator"') do taskkill /f /pid %%a
