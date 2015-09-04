# kandy -  Cost effective recommendation system for inter-cloud clusters
Server for Spot Pricing Details and Error Reporting.
* Objective: Cost efficient provisioning on the cloud
* Key Results: 
    * Model of cost including static and dynamic parameters such as cloud price model, resources and network topology. This model should cover at least a few public cloud providers such as Amazon Ec2, Google Compute Engine and ..
	* A recommendation system for estimating optimum configurations.  
	* Visualising configuration-cost options on the map in the Web Client. 
* Motivation: Optimised cost model for renting resources on cloud is to the interest of both companies and research institutions. Different cloud providers have different price list while they don't provide exactly the same resources/latency and so on

## Development environment

* Payara 4.1
* JavaEE 7
* Rest Service(Jersey)
* J2ee Batch procesing
* Maven web application - JSF
* JPA - mySql
* Jclouds

## Execution environment setup

#### Setup payara to connect to mysql :
1. 	Once payara is installed, make sure it can access MySQL Connector/J. To do this, copy the MySQL Connector/J jar file to the payara/lib directory. For example, copy mysql-connector-java-5.1.30-bin.jar to C:\payara-install-path\payara\lib. Restart the payara Application Server.
2. Creating a Jdbc Connection Pool and Jdbc resource used as datasource in persistence.xml. You can find all these configurations in glassfish-resources.xml. [tutorial](https://netbeans.org/kb/docs/web/mysql-webapp.html).
  * You can also use payara admin console to load this xml file and generate resource.
  * Start Your payara server admin console: usually http://localhost:4848
  * On your left navigate to [Resources]
  * Use [Add Resources Button] to load xml file
  * restart the server
3. An empty databse called **servicerecommender** should be created in mysql manually.

#### Payara configration for batch job : 
JavaEE Batch does not support mysql when deployed on glassfish so we use payara

1. Start Your payara server admin console: usually http://localhost:4848
2. On your left navigate to [Server] > [batch] > [configuration]
3. choose right datasource and database (schema) name. Required data source will be created after first time deploying the application.
4. restrart the server

#### Payara CDI aspect :
Disable implicit CDI in payara permennatly, so beans.xml will be required for CDI. (Jclouds CDI issues)
```sh
$ asadmin set configs.config.server-config.cdi-service.enable-implicit-cdi=false
```

#### Aws credentials :
Should be stored in a file in specified path for the server to find them. This is how the file looks like.
```sh
[default]
aws_access_key_id = ........
aws_secret_access_key = .........
```

#### Restful webservice is available at :
```sh
http://Server:8080/CloudServiceRecommender/api/
```

#### EJB timer configuration (optional):
1. Start Your payara server admin console: usually http://localhost:4848
2. On your left navigate to [Configurations] > [server-config] > [EJB Container]
3. Then click the TAB up top [EJB Timer Service]
4. Then fill out Timer Datasource: with your JDBC Resource eg. [jdbc/mysql]
