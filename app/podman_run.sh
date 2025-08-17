podman stop dancemoments
podman rm dancemoments

podman run --name dancemoments -p 8080:8080 org.tbee.calendaraggregator:latest
