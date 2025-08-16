call podman stop dancemoments
call podman rm dancemoments

@rem call podman run --name dancemoments -p 8080:8080 org.tbee.calendaraggregator:latest
set /p id=<podman_containerId.txt
call podman run --name dancemoments -p 8080:8080 %id%
@rem use --rm to automatically remove the container after it exits

pause