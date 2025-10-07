minikube config set rootless true

minikube start --driver=podman --container-runtime=containerd --insecure-registry="localhost:5000" --insecure-registry="nyota.softworks.nl:5080" --addons=metrics-server --addons=ingress --alsologtostderr
minikube addons enable metrics-server
minikube addons enable ingress
