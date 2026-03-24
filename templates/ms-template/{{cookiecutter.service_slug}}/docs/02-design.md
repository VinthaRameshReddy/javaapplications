# {{ cookiecutter.service_name }} Design Document

Author: [Venkat k]

## Introduction

* What are you trying to accomplish? What’s wrong with things the way they are now?
* Describe any historical context that would be needed to understand the document, including legacy considerations.
  
## Proposed Design

Start with a brief, high-level description of the solution. The following sections will go into more detail.

### Open API document

The OpenAPI documents for the API is located at http://localhost:{{ cookiecutter.service_port }}/v3/api-docs
The Swagger UI is located at http://localhost:{{ cookiecutter.service_port }}/api-docs/swagger-ui/

#### Interface/API Definitions

Describe how the various components talk to each other. For example, if there are REST endpoints, describe the endpoint URL and the format of the data and parameters used.

### Data Model

Describe how the data is stored. This could include a description of the database schema.

### Business Logic

If the design requires any non-trivial algorithms or logic, describe them.

### Migration Strategy

If the design incurs non-backwards-compatible changes to an existing system, describe the process whereby entities that depend on the system are going to migrate to the new design.

## Risks

If there are any risks or unknowns, list them here. Also, if there is additional research to be done, mention that as well.
