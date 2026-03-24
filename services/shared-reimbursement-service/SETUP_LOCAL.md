# Local Setup Guide for Shared Reimbursement Service

## Prerequisites

- Docker Desktop installed and running
- Java 17+ (for running the Spring Boot application)
- Gradle (or use the included `gradlew` wrapper)

## Step 1: Start SQL Server Database

The service requires SQL Server with two databases: `medigo` and `ReimDB`.

### Option A: Using Docker Compose (Recommended)

```bash
docker-compose -f docker-compose.sqlserver.yml up -d
```

This will start SQL Server on `localhost:1433` with:
- Username: `sa`
- Password: `root@123`
- Port: `1433`

### Option B: Using Docker Run Command

```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=root@123" \
  -p 1433:1433 --name shared-reimbursement-sqlserver \
  -d mcr.microsoft.com/mssql/server:2022-latest
```

### Step 2: Create Databases

Wait for SQL Server to be ready (about 30-60 seconds), then create the databases:

```bash
# Connect to SQL Server
docker exec -it shared-reimbursement-sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "root@123" -Q "CREATE DATABASE medigo; CREATE DATABASE ReimDB;"
```

Or using SQL Server Management Studio (SSMS) or Azure Data Studio:
- Connect to `localhost,1433` with username `sa` and password `root@123`
- Run:
  ```sql
  CREATE DATABASE medigo;
  CREATE DATABASE ReimDB;
  ```

## Step 3: Verify Database Connection

Check if SQL Server is accessible:

```bash
docker exec -it shared-reimbursement-sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "root@123" -Q "SELECT @@VERSION;"
```

## Step 4: Run the Application

### Using Gradle:

```bash
./gradlew bootRun
```

### Using IDE:

Run `SharedReimbursementServiceApplication.java` from your IDE.

The service will start on: `http://localhost:10092`

## Step 5: Test with Postman

1. Import the Postman collection: `Shared_Reimbursement_Submit.postman_collection.json`
2. The collection uses `{{baseUrl}}` variable set to `http://localhost:10092`
3. Select a request and click "Send"

## Troubleshooting

### SQL Server Connection Refused

- **Check if SQL Server is running:**
  ```bash
  docker ps | grep sqlserver
  ```

- **Check SQL Server logs:**
  ```bash
  docker logs shared-reimbursement-sqlserver
  ```

- **Verify port 1433 is not in use:**
  ```bash
  # Windows PowerShell
  netstat -an | findstr 1433
  
  # Linux/Mac
  lsof -i :1433
  ```

### Database Already Exists Error

If databases already exist, you can skip the CREATE DATABASE step or drop them first:

```sql
DROP DATABASE IF EXISTS medigo;
DROP DATABASE IF EXISTS ReimDB;
CREATE DATABASE medigo;
CREATE DATABASE ReimDB;
```

### Application Won't Start

- Check `application.properties` for correct database credentials
- Ensure SQL Server is fully started (wait 30-60 seconds after starting container)
- Check application logs in `./logs` directory

## Stopping SQL Server

```bash
docker-compose -f docker-compose.sqlserver.yml down
```

Or:

```bash
docker stop shared-reimbursement-sqlserver
docker rm shared-reimbursement-sqlserver
```

## Notes

- The SQL Server container uses a volume to persist data. To completely remove data:
  ```bash
  docker-compose -f docker-compose.sqlserver.yml down -v
  ```

- Database schema migrations are handled by the application (Flyway is disabled in current config)
