# msgen : Microservice Generator

This is a tool for generating template for new microservice.


## Prerequisites

1. Python 3
2. CookieCutter (Install via pip)

## Usage

You can generate both Microservice a library using the msgen tool.

```shell
$ cd tools

$ ./msgen.py --service

[('-o', '../services/core')]
output directory:  ../services/core
service_name [My Service]:
service_name_combined [MyService]: 
service_slug [my-service]: 
service_package [myservice]: 
with_db[y]:
with_kafka_producer [n]: 
with_kafka_consumer [n]: 
db_name [myservicedb]: 
db_password [EUeWVhnGMcLn]: 
version [0.1.0]: 
service_short_description [This is a Spring Boot Microservice]: 
entity [MyEntity]:
service_port [12141]:
```

For library do following

```
$ ./msgen.py --lib
```
### Specs

`output directory` : The directory where service requires to be generated. example : ../services/core

`service_name` : Intended name of service. example : My Service

`service_name_combined` : Combined name of service.  example : MyService

`service_slug` : Slug of a service. example : my-service

`service_package` : Name of a service package. example : myservice

`with_db`: Whether you need database related files or not. Default value is `y`.

`with_kafka_producer [n]`: Whether you need kafka producer related configurations or not. Default value is `n`.

`with_kafka_consumer [n]`: Whether you need kafka consumer related configurations or not. Default value is `n`.

`db_name` : Database Name of a service. example : myservicedb

`db_password` : random ascii string. example : EUeWVhnGMcLn 

`version` : Version of a service. example : 0.1.0

`service_short_description` : Short Description of a service. example : This is a Spring Boot Microservice

`entity` : Entity of a service. example : MyEntity

`service_port` : random generated integer between 10000 and 15000


Note : There are some default values. You can accept them by pressing enter key.

### Optional Database support

If you specify `with_db`  as 'n' then no db related file will be created. This will be helpful when creating journey related microservices or any other microservice that don't need db. Also, now docker-compose create db container with new name and bind a different port. This means you will not get errors if you create postgres container in multiple microservices.

### Optional Kafka support

If you specify `with_kafka_producer`  as 'y' or `with_kafka_consumer` as 'y' then kafka related configurations will be added to `application.properties` and `build.gradle` files.
This will be helpful when creating journey related microservices or any other microservice that need kafka. 


### Generated Service

```shell
$ cd ../services/core

$ tree

.
└── my-service
    ├── README.md
    ├── build.gradle
    ├── codequality
    │   └── checkstyle.xml
    ├── docker-compose.yml
    ├── docs
    │   ├── 01-definition.md
    │   ├── 02-design.md
    │   ├── README.md
    │   ├── diagrams
    │   └── images
    ├── gradle
    │   └── wrapper
    │       ├── gradle-wrapper.jar
    │       └── gradle-wrapper.properties
    ├── gradlew
    ├── gradlew.bat
    ├── settings.gradle
    ├── src
    │   ├── main
    │   │   ├── java
    │   │   │   └── com
    │   │   │       └── mybank
    │   │   │          └── myservice
    │   │   │                   ├── MyServiceApplication.java
    │   │   │                   ├── config
    │   │   │                   │   └── WebMvcConfig.java
    │   │   │                   ├── controller
    │   │   │                   │   └── MyEntityController.java
    │   │   │                   ├── domain
    │   │   │                   │   ├── entity
    │   │   │                   │   │   ├── AbstractBaseEntity.java
    │   │   │                   │   │   └── MyEntity.java
    │   │   │                   │   ├── request
    │   │   │                   │   │   └── MyEntityRequest.java
    │   │   │                   │   └── response
    │   │   │                   │       └── MyEntityResponse.java
    │   │   │                   ├── repository
    │   │   │                   │   └── MyEntityRepository.java
    │   │   │                   └── service
    │   │   │                       └── MyEntityService.java
    │   │   └── resources
    │   │       ├── application.properties
    │   │       ├── db
    │   │       │   └── migration
    │   │       │       └── V001__init.sql
    │   │       ├── docker
    │   │       │   └── Dockerfile
    │   │       └── logback-spring.xml
    │   └── test
    │       ├── java
    │       │   └── com
    │       │       └── mybank
    │       │           └── myservice
    │       │                   └── MyServiceApplicationTests.java
    │       └── resources
    │           └── application-test.properties
    └── tmp

34 directories, 27 files

```

