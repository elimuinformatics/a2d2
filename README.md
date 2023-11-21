# A2D2

A2D2 is a clinical decision-support, data integration, and workflow services platform. It supports contemporary healthcare interoperability standards including FHIR and CDS-Hooks.

## Getting started


### Prerequisite


You will need to install the following:


1. Git [(Download)](https://git-scm.com/)

2. JDK 11 [(Download)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)

3. Eclipse [(Download)](http://www.eclipse.org)

4. Maven [(Download)](https://maven.apache.org/)

5. Tomcat 8.5 [(Download)](https://tomcat.apache.org/download-80.cgi#8.5.50)

6. MySQL [(Download)](https://dev.mysql.com/downloads/installer/)

  


### Installing

1. Clone the git repository

2. Compile the projects

        mvn clean install -DskipTests --settings=a2d2-settings.xml

3. Create MySQL databases 

    * Create database `a2d2`

    * Run a2d2/sql/A2D2_mysql.sql against `a2d2` database

    * Change DB name with `a2d2` and DB creds in local spring profile in a2d2/a2d2-api/src/main/resources/application.yml file


4. Import projects to Eclipse

    In Eclipse’s project explorer, select import -> existing maven projects, and select the path to a2d2 repository. Import all existing projects


### Running the tests

    mvn test


## Versioning

Whenever changes are made to the project, it's important to update the version numbers in the project's POM files. This includes both the sub-project POM files and the parent POM file.

### Updating Sub-Project POM Version

If you make changes to a specific sub-project, navigate to its POM file and update the version number accordingly. Look for the `<version>` element and modify it appropriately.

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>sub-project</artifactId>
    <version>1.0.0</version> <!-- Update this version number -->
    <!-- ... other configurations ... -->
</project>
```

### Updating Parent-Project POM Version

If you make changes to a project, navigate to its POM file of parent and update the version number accordingly. Look for the `<version>` element and modify it appropriately.

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>project</artifactId>
    <version>1.0.0</version> <!-- Update this version number -->
    <!-- ... other configurations ... -->
</project>
```

## Deployment

### Deploying as a WAR file in Tomcat

1. Add the following to the bin/setenv.sh in the Tomcat installation directory if you run on Linux or Mac:

        JAVA_OPTS=$JAVA_OPTS -Dfhir.client.conpool=20 -Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-maven-repo-user> -Dkie.wb.user=<your-kie-wb-user> -Dkie.wb.location=<your-kie-wb-location> -Dkie.wb.pwd=<your-kie-wb-pwd> -Dspring.profiles.active=local,test,default


    Add the following to the bin/catalina.bat around line 108 if you run Windows: 

        JAVA_OPTS=%JAVA_OPTS% -Dfhir.client.conpool=20 -Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-maven-repo-user> -Dkie.wb.user=<your-kie-wb-user> -Dkie.wb.location=<your-kie-wb-location> -Dkie.wb.pwd=<your-kie-wb-pwd> -Dspring.profiles.active=local,test,default


    NOTE: To test offline mode, add flags -Dkie.maven.offline.force=true -Dservices.pom.path=/path/to/pom.xml in above JAVA_OPTS


2. Copy a2d2/a2d2-api/target/a2d2-api.war to webapps/


3. In the tomcat/bin folder, run the following to start the environment:

    * For Linux/Mac: `catalina.sh run`
    * For Windows: `catalina.bat run`

4. Debugging

    In order to debug the omnibus application, because the classloading of the rules / process engine is very sensitive to changes in structure, you must debug it running remotely, even within the same local environment. To do so, you need to start the application like this:

    * For Linux/Mac: `catalina.sh jpda run`
    * For Windows: `catalina.bat jpda run`

    Then, from Eclipse, click on debug -> Run as -> Remote Java Application -> new, and connect using localhost port 8000

    Also, if you want to debug the rule and process execution of a particular request, add the HTTP header X-Enable-Debug: true to the request for enabling it on the server logs, or X-Output-Debug: true to enable it on the HTTP output 

### Deploying with Docker 

1. Use following docker build command in a2d2 

	docker build -t a2d2 . --target final

2. Run docker a2d2 with local database

        docker run -e "JAVA_OPTS=-Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-maven-repo-user> -Dkie.wb.user=<your-kie-wb-user> -Dkie.wb.location=<your-kie-wb-location> -Dkie.wb.pwd=<your-kie-wb-pwd> -Dspring.profiles.active=local,test,default" --network="host" -d --name a2d2 a2d2

### Optional configurations

1. Redis Cache: components like the `fhir-query-helper-r4` or `generic-helpers` use cache implementations that are configurable via JVM properties. If you want to use JedisHelper, or jedis implementation of caching of values, you need to add these JVM properties:

	-Dcache.type.impl=REDIS -Dcache.jedis.host=<your-redis-host> -Dcache.jedis.port=<your-redis-port>

   Also, if you want to also configure secure access to it, you will need the following JVM properties as well:

	-Dcache.jedis.use_ssl=true -Dcache.jedis.password=<your-optional-redis-password> -Dcache.jedis.connection.timeout=<your-optional-conn-timeout-in-millis-defaults-to-10-seconds>



