source /opt/concourse-java.sh

export TERM=dumb
setup_symlinks

if [[ -n $DOCKER_HUB_USERNAME ]]; then
	docker login -u $DOCKER_HUB_USERNAME -p $DOCKER_HUB_PASSWORD
fi

cleanup_maven_repo "org.springframework.backends"
