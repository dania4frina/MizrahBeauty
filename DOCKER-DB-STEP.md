## Docker SQL Server Setup

- **Container**: `sqlserver2022` (`mssql/server` image exposing port `1433`)
- **Credentials**: `sa` / `Fbi22031978&`
- **UI option (macOS)**: install Azure Data Studio (`https://azure.microsoft.com/en-us/products/data-studio/`) for a GUI client, or continue with `sqlcmd`.

## Verify Container Access

1. `docker ps` → confirm `sqlserver2022` is running
2. Test login:
   ```
   docker exec sqlserver2022 /opt/mssql-tools18/bin/sqlcmd \
     -S localhost -U sa -P "Fbi22031978&" -C -Q "SELECT @@SERVERNAME;"
   ```
   `-C` trusts the container’s self-signed cert.

## Run App Schema

- All required tables are in `sql_files/mizrahbeauty_schema_combined.sql`
- Execute from project root:
  ```
  cat sql_files/mizrahbeauty_schema_combined.sql | docker exec -i sqlserver2022 \
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Fbi22031978&" -C -d master
  ```

## Database Overview

- **Database name**: `dania`
- **Tables created**:
  - `users`
  - `staff`
  - `services`
  - `bookings`
  - `reviews`
  - `feedback`
  - `password_reset_codes`

## Helpful SQLCMD snippets

- List databases:
  ```
  docker exec sqlserver2022 /opt/mssql-tools18/bin/sqlcmd \
    -S localhost -U sa -P "Fbi22031978&" -C \
    -Q "SELECT name FROM sys.databases ORDER BY name;"
  ```
- List tables in `dania`:
  ```
  docker exec sqlserver2022 /opt/mssql-tools18/bin/sqlcmd \
    -S localhost -U sa -P "Fbi22031978&" -C -d dania \
    -Q "SELECT name FROM sys.tables ORDER BY name;"
  ```

