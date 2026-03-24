

<div align="center">
  <h3 align="center">{{ cookiecutter.service_name }}</h3>
  <p align="center">
    {{ cookiecutter.service_short_description }}
    <br />
    <a href="./docs"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="ci_job_url">CI Job</a>
    .
    <a href="http://localhost:{{ cookiecutter.service_port }}/api-docs/swagger-ui/">OpenAPI UI</a>
  </p>
</div>


<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#purpose-of-microservice">Purpose of Microservice</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#build">Build</a></li>
        <li><a href="#database-setup">Database Setup</a></li>
        <li><a href="#running-the-service">Running the Service</a></li>
      </ul>
    </li>
    <li><a href="#team">Team</a></li>
  </ol>
</details>


## Purpose of Microservice

At a high level explain the purpose of this Microservice.


<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

For exact versions refer to repository root `docs` directory.

* Java
* Docker
* IntelliJ Idea
* Postgres

### Build

To build the project execute following command.

```
$ ./gradlew clean build
```

> `$` signifies command prompt. You don't have to type it.


## Running the service

You can run Postgres in a Docker container by executing following command

```
  docker run --name postgresdb -e POSTGRES_PASSWORD=<password in application.properties> -e POSTGRES_DB={{ cookiecutter.db_name }} -p 5432:5432 -d postgres:12.6
```
You can connect to Postgres using psql CLI.

```
  docker run -it --rm --link postgresdb postgres:12.6 psql -h postgresdb -U postgres
```

{% if cookiecutter.with_kafka_producer == "y"  or  cookiecutter.with_kafka_consumer == "y" -%}

```shell
$ docker-compose -f ../../../kafka/docker-compose-kafka-setup.yml up -d
```
{% endif -%}

Next, you can run the service from either IDE or CLI

```
java -jar build/libs/{{ cookiecutter.service_slug + '-' + cookiecutter.version + '.jar'}}
```

## Team

Specify name of the team members who maintain this service.

* Team member 1
* ...
