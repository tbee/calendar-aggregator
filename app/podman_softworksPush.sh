# use podman login nexus.softworks.nl to authenticate
#podman tag org.tbee.calendaraggregator:latest nexus.softworks.nl/docker-private/org.tbee.calendaraggregator:latest
#podman push nexus.softworks.nl/docker-private/org.tbee.calendaraggregator:latest
podman tag org.tbee.calendaraggregator:latest nyota.softworks.nl:5080/docker-private/org.tbee.calendaraggregator:latest
podman push nyota.softworks.nl:5080/docker-private/org.tbee.calendaraggregator:latest
