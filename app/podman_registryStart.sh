podman container run -dt -p 5000:5000 --name registry --volume registry:/var/lib/registry:Z docker.io/library/registry:2
