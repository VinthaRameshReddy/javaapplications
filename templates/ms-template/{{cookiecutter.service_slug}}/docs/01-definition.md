# {{ cookiecutter.service_name }} Definition Document

Describe your service

## Capabilities

* Capability 1
* ..

## Service API

### Synchronous APIs

| API Operation  | Events Published   |
| -------------- | ------------------ |
| createEntity() | EntityCreatedEvent |
| deleteEntity() | EntityDeletedEvent |
| getEntity()    | NA                 |

> In the above table replace API operations with your API operations

### Asynchronous APIs

| API Operation  | Events Published   |
| -------------- | ------------------ |
| generateX()    | NA                 |

## Non-functional Requirements

* Availability: xx.xx% availability. For example, 99.95% availability
* Throughput: x y/second. For example, 1000 transactions/second
* Performance: x ms per each y operation invocation. For example, 500 ms to create a customer  

## Observability

### Key metrics

Following are the key business metrics that should be visible in our observability tool chain

* Number of successful events. Example, successful device registrations
* Number of failure events. Example, failed device registrations

### Health check URL

Service health check endpoint is located at /actuator/health

## Implementation

For implementation details refer to this [document](./02-design.md).

## Dependencies

List of downstream services this service depends on. These are both sync(invocations) and async(subscription) dependencies.

* TODO
