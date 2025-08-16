call podman stop dancemoments
call podman rm dancemoments

call podman run --name dancemoments -p 8080:8080 org.tbee.calendaraggregator:latest
@rem set /p id=<podman_containerId.txt
@rem call podman run --name dancemoments -p 8080:8080 %id%
@rem use --rm to automatically remove the container after it exits

pause