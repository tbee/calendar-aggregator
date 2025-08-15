call podman rm dancemoments

@rem call podman run --name dancemoments -p 8080:8080 org.tbee.calendaraggregator:latest
set /p id=<containerId.txt
call podman run --name dancemoments -p 8080:8080 %id%

pause