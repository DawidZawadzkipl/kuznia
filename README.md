# Kuznia

Aplikacja do zarzadzania treningami w silowni trojbojowej.

![Database schema](docs/images/db_diagram.png)

## Wymagania

- Docker Desktop
- Java 21
- Maven Wrapper z repo albo Maven lokalnie
- Node.js i npm

## Baza danych

Uruchom PostgreSQL z Dockera:

```powershell
docker compose up -d
```

Domyslne ustawienia lokalnej bazy:

- database: `kuznia`
- user: `kuznia`
- password: `kuznia`
- host port: `5433`
- container port: `5432`

## Backend

Backend startuje na porcie `8082`.

### IntelliJ IDEA

1. Otworz projekt w IntelliJ.
2. Uruchom klase `org.bnabd.kuznia.KuzniaApplication`.
3. Upewnij sie, ze Docker/PostgreSQL dziala przed startem aplikacji.

### Maven

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Adresy:

- API: `http://localhost:8082`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`

## Frontend

Frontend jest w katalogu `frontend`.

```powershell
cd frontend
npm install
npm run dev
```

Aplikacja frontendowa:

- `http://127.0.0.1:5173`

Vite proxy przekierowuje `/api` oraz `/uploads` na backend:

- `http://localhost:8082`

## Seedowane konta

Hasla i konta tworza sie automatycznie przy starcie backendu, jesli nie istnieja jeszcze w bazie.

| Rola | Email | Haslo |
| --- | --- | --- |
| Admin | `admin@kuznia.local` | `Admin123!` |
| Trener | `marek.sila@kuznia.local` | `Trainer123!` |
| Trener | `ewa.lawka@kuznia.local` | `Trainer123!` |
| Klient | `jan.kowalski@kuznia.local` | `Client123!` |
| Klient | `anna.nowak@kuznia.local` | `Client123!` |

## Testowanie API

1. Zaloguj sie przez `POST /api/auth/login`.
2. Skopiuj `token` z odpowiedzi.
3. W Swaggerze kliknij `Authorize` i wpisz:

```text
Bearer <token>
```

Publiczne endpointy sa pod `/api/public/**`.
