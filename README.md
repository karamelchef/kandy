# kandy -  Cost effective recommendation system for inter-cloud clusters
Server for Spot Pricing Details and Error Reporting.
* Objective: Cost efficient provisioning on the cloud
* Key Results: 
    * Model of cost including static and dynamic parameters such as cloud price model, resources and network topology. This model should cover at least a few public cloud providers such as Amazon Ec2, Google Compute Engine and ..
	* A recommendation system for estimating optimum configurations.  
	* Visualising configuration-cost options on the map in the Web Client. 
* Motivation: Optimised cost model for renting resources on cloud is to the interest of both companies and research institutions. Different cloud providers have different price list while they don't provide exactly the same resources/latency and so on

## Execution environment setup
Payara 4.1 is used instead of glassfish as server + JavaEE 7

#### EJB timer configuration :
1. Start Your payara server admin console: usually http://localhost:4848
2. On your left navigate to [Configurations] > [server-config] > [EJB Container]
3. Then click the TAB up top [EJB Timer Service]
4. Then fill out Timer Datasource: with your JDBC Resource eg. [jdbc/mysql]
	
#### Setup payara to connect to mysql :
1. 	Once payara is installed, make sure it can access MySQL Connector/J. To do this, copy the MySQL Connector/J jar file to the 
payara/lib directory. For example, copy mysql-connector-java-5.1.30-bin.jar to C:\payara-install-path\payara\lib. Restart the payara Application Server.
2. Creating a Jdbc Connection Pool and Jdbc resource in payara admin console and use it as datasource in persistence.xml
[payara needs to connect to mysql](http://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-glassfish-config.html)

#### Payara configration for batch job : 
JavaEE Batch does not support mysql when deployed on glassfish so we use payara

1. Start Your payara server admin console: usually http://localhost:4848
2. On your left navigate to [Server] > [batch] > [configuration]
3. choose right datasource and database (schema) name

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
http://Server:8080/CloudServiceRecommander/rest/
```