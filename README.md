# sas9api

RESTful APIs for SAS integration.

## Prerequisites

* Java 8
* Apache Maven 3.8
* Install the SAS jars into local Maven repository(`~/.m2/repository`).

### Install  SAS Jars

Enter the *jars* folder in the project folder.

And run the following command to install all SAS jars into your local repository.

```bash
sh install.sh
```

>For Windows users, run this command in the Git bash terminal.

## Build & Run Application

Clone the project codes and switch to project root folder and run the following command to build the project.

```bash
mvn clean package
```

After you have built the project, there is a *sas-proxy.jar* file in the *target* folder.

There are some approaches to run the application.

In the development env, it is easy to run the application via Spring Boot maven plugin.

```bash
mvn clean spring-boot:run
```

If you have built the project firstly, run the following command instead to run the application via jar files.

```bash
java -jar target/sas-proxy.jar
```

To enable **debug** mode, append a `--debug` to the above command.

```bash
java -jar target/sas-proxy.jar --debug
```

## Configuring application

By default, in this repository, the application loads a *application.yml* from the *config* folder.  

You can change it as expected at deployment. Generally run the application jar with a *config/applicaiton.yml* is ok.

To enable a special profile, eg. a **prod** profile for production env, create a new configuraiton name *application-prod.yml*.

```bash
java -jar target/sas-proxy.jar -Dspring.profiles.active=prod
```

> The profile specific properties will override the default ones at runtime.


> Follow the 12-Factor App Principle, ideally all configurations should be part of source codes and be managed via Git in this repository, and the application can switch to different configurations via Spring profiles. In a production  env,  a special profile can be applied or set up some **environment variables**( or K8s  configMap  if using K8s) to override the default configurations.


### Logging 

Besides explicitly showing logging info in the console, and file based log appender is also enabled.

When debugging applications, you can check the *app.log* for the latest logging.

You can change the logging level to your exceptions.

> For more details, please check the `logging` prefix based properties in the *application.yml* file.

### Swagger UI Authentication

By default the swagger ui can be accessed via http://localhost:8080/swagger-ui.html. Optionally, it can be protected by user/passowrd configured in the *applicaiton.yml* file.

Change it as you expected, eg, adding a new user `tom`, password is `tompwd`.

```yml
swagger:
    auth:
        users:
            tom: tompwd

```

Change `swagger.auth.enabled` to `false` and bypass authenticaiton for Swagger UI.

> For more details, check the `swagger.auth` prefix based properties in the *application.yml* file.


## ssl key store

To list contents of the keystore locate to keystore file and type:

```bash
keytool -list -v -storetype pkcs12 -keystore localhost-ssl-keystore.p12
enter password sas9api (more info in application.yml)
```

Extract an SSL certificate from a keystore:

```bash
keytool -export -keystore localhost-ssl-keystore.p12 -alias selfsigned_localhost_sslserver -file {insert filename}
```

Create a keystore:

```bash
keytool -genkey -alias {{somealias}} -keystore {{somekeystore.p12}} -storetype PKCS12 -keyalg RSA -storepass {{somepass}} -validity 730 -keysize 4096
```

Create a PKCS12 keystore from an existing private key and certificate using openssl:

```bash
openssl pkcs12 -export -in certificate.pem -inkey key.pem -out localhost-ssl-keystore.p12
```

Extract a private key from a keystore using openssl:

```bash
openssl pkcs12 -in localhost-ssl-keystore.p12 -nocerts -nodes
```

Extract certificates from a keystore using openssl

```bash
openssl pkcs12 -in localhost-ssl-keystore.p12 -nokeys
```
