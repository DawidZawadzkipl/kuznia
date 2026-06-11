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
- port: `5432`

Seeded admin account:

- email: `admin@kuznia.local`
- password: `Admin123!`
