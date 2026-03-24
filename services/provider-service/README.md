# Provider Service

Proxy layer that calls `shared-provider-service` via Feign, mirroring standards used across services.

## Run
```
./gradlew :services:provider-service:bootRun
```

Configure `shared.provider.service.url` in `application-*.properties` to point to shared-provider-service.


