# Running a2d2 on docker

1. Use following docker build command in a2d2 

	docker build -t a2d2 . --target final

2. Run docker a2d2

        docker run -e "JAVA_OPTS=-Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-maven-repo-user> -Dkie.wb.user=<your-kie-wb-user> -Dkie.wb.location=<your-kie-wb-location> -Dkie.wb.pwd=<your-kie-wb-pwd> -Dspring.profiles.active=local,test,default" --network="host" -d --name a2d2 a2d2
