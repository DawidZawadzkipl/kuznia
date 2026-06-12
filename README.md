# kuznia
![Database schema](docs/images/db_diagram.png)

## Backend

Start PostgreSQL:

```bash
docker compose up -d
```

Run the application:

```bash
./mvnw spring-boot:run
```

Default local database settings:

- database: `kuznia`
- user: `kuznia`
- password: `kuznia`
- host port: `5433`
- container port: `5432`

Application URL:

- `http://localhost:8082`

Seeded admin account:

- email: `admin@kuznia.local`
- password: `Admin123!`
