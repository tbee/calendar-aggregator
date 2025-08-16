call podman build --iidfile=podman_containerId.txt --file src\main\container\Containerfile . 

@rem fetch image id of new container
set /p id=<podman_containerId.txt

call podman untag org.tbee.calendaraggregator:latest 

call podman tag %id% org.tbee.calendaraggregator:latest 

call podman image prune --force 

pause