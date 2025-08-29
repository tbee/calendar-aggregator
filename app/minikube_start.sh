minikube config set rootless true

#minikube start --driver=podman --container-runtime=crio --alsologtostderr
minikube start --driver=podman --container-runtime=containerd --insecure-registry "10.0.0.0/24" --alsologtostderr

minikube addons enable metrics-server
minikube addons enable registry
# expose minikube's registry container on port 5000
kubectl port-forward --namespace kube-system service/registry 5000:80 &
