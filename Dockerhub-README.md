# A2D2
 
A2D2 is a clinical decision-support, data integration, and workflow services platform. It supports contemporary healthcare interoperability standards including FHIR and CDS-Hooks.
 
# Running a2d2 on docker

1.  Use following docker run command to start a2d2 container

        docker run -e "JAVA_OPTS=-Xms512m -Xmx4096m -Dfhir.client.instancecount=60 -Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-Dmaven-repo-user> -Dspring.profiles.active=local,test,default -Dspring.datasource.url=<your-spring-datasource-url>  -Dspring.datasource.password=<your-spring-datasource-password> -Dspring.datasource.username=<your-spring-datasource-username> -Dorg.quartz.dataSource.nonManagedDS.URL=<your-org-quartz-dataSource-nonManagedDS-URL> -Dorg.quartz.dataSource.nonManagedDS.user=<your-org-quartz-dataSource-nonManagedDS-user>" --name a2d2 a2d2

## Environment variables

Generic variable names can be used to configure any Database type, defaults may vary depending on the Database.

`maven.repo.location`: It's specifies where Maven stores downloaded dependencies. 

`maven.repo.pwd`: The password associated with your Maven repository.

`maven.repo.user`: Username associated with your Maven repository.

`spring.profiles.active`: It is a Spring Framework configuration property used to specify which profiles should be activated. Ex. local, test, and default.

`spring.datasource.url`: JDBC URL used to connect to the database for your Spring application.

`spring.datasource.password`: The password used to authenticate with the database specified in spring.datasource.url. 

`spring.datasource.username`: This is the username used to authenticate with the database specified in spring.datasource.url.

`org.quartz.dataSource.nonManagedDS.URL`: Quartz Scheduler configuration property specifying the URL for a non-managed data source.

`org.quartz.dataSource.nonManagedDS.user`: Quartz Scheduler configuration property specifying the username for the non-managed data source.

## Database

- Create MySQL databases

- Create database a2d2

- Run a2d2/sql/A2D2_mysql.sql against a2d2 database

- Change DB name with a2d2 and DB creds in local spring profile in a2d2/a2d2-api/src/main/resources/application.yml file
