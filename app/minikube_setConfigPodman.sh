minikube config set rootless true
minikube config set driver podman
minikube config set container-runtime crio
# minikube config set container-runtime cri-o
# Exiting due to MK_USAGE: The podman-env command is only compatible with the "crio" runtime, but this cluster was configured to use the "containerd" runtime.
# minikube config set container-runtime containerd
