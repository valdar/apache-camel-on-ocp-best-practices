## Retrieving Configmap in OCP from a Camel Application

In this sample you'll use the Kubernetes Secret Properties function to run a query against a Postgres Database.

## Preparing the project

We'll connect to the `camel-sql` project and check the installation status. To change project, open a terminal tab and type the following command:

```
oc new-project camel-ocp-cm
```

## Setting up Database

This example uses a PostgreSQL database. We want to install it on the project `camel-ocp-cm`. We can go to the OpenShift 4.x WebConsole page, use the OperatorHub menu item on the left hand side menu and use it to find and install "Crunchy Postgres for Kubernetes". This will install the operator and may take a couple of minutes to install.

Once the operator is installed, we can create a new database using

```
oc create -f postgres.yaml
```

We connect to the database pod to create a table and add data to be extracted later.

```
oc rsh $(oc get pods -l postgres-operator.crunchydata.com/role=master -o name)
```

```
psql -U postgres test \
-c "CREATE TABLE test (data TEXT PRIMARY KEY);
INSERT INTO test(data) VALUES ('hello'), ('world');
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgresadmin;"
```

Now let's change the password.

```
psql -U postgres -c "ALTER USER postgresadmin PASSWORD 'masteradmin1234*';"
```

```
exit
```

Now, we need to find out Postgres username, password and hostname and prepare the AWS Secret and credentials.

```
USER_NAME=$(oc get secret postgres-pguser-postgresadmin --template={{.data.user}} | base64 -d)
USER_PASSWORD=$(oc get secret postgres-pguser-postgresadmin --template={{.data.password}} | base64 -d)
HOST=$(oc get secret postgres-pguser-postgresadmin --template={{.data.host}} | base64 -d)
PASSWORD_SKIP_SPEC_CHAR=$(sed -e 's/[&\\/]/\\&/g; s/$/\\/' -e '$s/\\$//' <<<"$USER_PASSWORD")
```

Now we need to create the secret payload. Open the file cm.yaml and edit it to look like:

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: cm-postgres
data:
  username: <username>
  password: <password>
  host: <host>
```

and then do 

```
oc apply -f cm.yaml
```

Now we need to create the Cluster roles/Service Account and role binding for listing/getting configmaps.

You can run the following command:

```
oc apply -f sa.yaml
```

This complete the Database setup.

## Deploy to OCP

Once the process complete

```
./mvnw install -Dquarkus.openshift.deploy=true
```

Once everything is complete you should be able to access the logs with the following command:

```
> oc logs sql-to-log-2-prg7v
Starting the Java application using /opt/jboss/container/java/run/run-java.sh ...
INFO exec -a "java" java -javaagent:/usr/share/java/jolokia-jvm-agent/jolokia-jvm.jar=config=/opt/jboss/container/jolokia/etc/jolokia.properties -javaagent:/usr/share/java/prometheus-jmx-exporter/jmx_prometheus_javaagent.jar=9779:/opt/jboss/container/prometheus/etc/jmx-exporter-config.yaml -XX:MaxRAMPercentage=80.0 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XX:+ExitOnOutOfMemoryError -cp ".:/deployments/*" org.springframework.boot.loader.launch.JarLauncher 
INFO running in /deployments
I> No access restrictor found, access to any MBean is allowed
Jolokia: Agent started with URL https://172.17.5.140:8778/jolokia/

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.3)

2024-10-04T12:12:23.823Z  INFO 1 --- [           main] o.e.project.sqltolog.CamelApplication    : Starting CamelApplication v1.0-SNAPSHOT using Java 21.0.3 with PID 1 (/deployments/BOOT-INF/classes started by 1000890000 in /deployments)
2024-10-04T12:12:23.832Z  INFO 1 --- [           main] o.e.project.sqltolog.CamelApplication    : No active profile set, falling back to 1 default profile: "default"
2024-10-04T12:12:27.024Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-10-04T12:12:27.045Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-10-04T12:12:27.046Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.28]
2024-10-04T12:12:27.161Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-10-04T12:12:27.163Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3214 ms
2024-10-04T12:12:28.954Z  INFO 1 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2024-10-04T12:12:29.123Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2024-10-04T12:12:30.246Z  INFO 1 --- [           main] o.a.c.c.k.p.BasePropertiesFunction       : KubernetesClient using masterUrl: https://172.21.0.1:443/ with namespace: csb-kubernetes
2024-10-04T12:12:31.708Z  INFO 1 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.8.0 (camel-1) is starting
2024-10-04T12:12:32.191Z  INFO 1 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Routes startup (total:1 started:1 kamelets:1)
2024-10-04T12:12:32.191Z  INFO 1 --- [           main] o.a.c.impl.engine.AbstractCamelContext   :     Started route1 (kamelet://postgresql-source)
2024-10-04T12:12:32.191Z  INFO 1 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.8.0 (camel-1) started in 482ms (build:0ms init:0ms start:482ms)
2024-10-04T12:12:32.194Z  INFO 1 --- [           main] o.e.project.sqltolog.CamelApplication    : Started CamelApplication in 9.315 seconds (process running for 10.239)
2024-10-04T12:12:33.428Z  INFO 1 --- [%20from%20test;] route1                                   : {"data":"hello"}
2024-10-04T12:12:33.432Z  INFO 1 --- [%20from%20test;] route1                                   : {"data":"world"}
```


