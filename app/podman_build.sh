podman build --iidfile=podman_containerId.txt --file src/main/container/Containerfile .

# fetch image id of new container
id=`cat podman_containerId.txt`

podman untag org.tbee.calendaraggregator:latest

podman tag $id org.tbee.calendaraggregator:latest

podman image prune --force
