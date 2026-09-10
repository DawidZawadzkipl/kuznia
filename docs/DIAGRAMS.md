# 📊 Diagramy UML Projektu Kuznia

Dokumentacja zawiera pełne diagramy UML dla systemu zarządzania treningami trojbojowymi Kuznia.

## 📋 Spis Treści

1. [Diagram Przypadków Użycia (Use Case)](#diagram-przypadków-użycia)
2. [Diagramy Sekwencji](#diagramy-sekwencji)
3. [Diagram Encji (ERD)](#diagram-encji)
4. [Diagram Klas](#diagram-klas)

---

## Diagram Przypadków Użycia

**Plik:** `docs/diagrams/use-cases.puml`

![Use Cases](use-cases.puml)

### Opis

Diagram pokazuje wszystkie główne przypadki użycia systemu Kuznia, podzielone na 3 role:

#### 👤 Klient (Client)
- **UC1: Zalogować się** — Logowanie do systemu
- **UC2: Rejestrować konto** — Tworzenie nowego konta
- **UC3: Rezerwować trening** — Rezerwacja sesji z trenerem
- **UC6: Logować wyniki podnoszów** — Zapisywanie wyników treningów (Squat, Bench, Deadlift)
- **UC7: Przeglądać postępy** — Śledzenie postępów treningowych na wykresach
- **UC11: Dodawać notatki treningowe** — Notatki z sesji treningowych

#### 💪 Trener (Trainer)
- **UC1: Zalogować się**
- **UC4: Potwierdzić rezerwację** — Akceptacja lub odrzucenie rezerwacji klienta
- **UC5: Odrzucić rezerwację** — Odrzucenie rezerwacji
- **UC10: Edytować dostępność** — Zarządzanie dostępnymi terminami
- **UC11: Dodawać notatki treningowe** — Notatki dla każdego klienta
- **UC7: Przeglądać postępy** — Monitorowanie postępów swoich klientów

#### 🔐 Admin (Administrator)
- **UC1: Zalogować się**
- **UC8: Zarządzać użytkownikami** — CRUD na użytkownikach, aktywacja/blokada
- **UC9: Zarządzać trenerami** — Dodawanie/edycja trenerów
- **UC12: Przeglądać statystyki** — Dashboard z metryki systemu
- **UC13: Zarządzać certyfikatami** — Certyfikaty trenerów

---

## Diagramy Sekwencji

### 1️⃣ Logowanie Użytkownika

**Plik:** `docs/diagrams/sequence-login.puml`

![Sequence Login](sequence-login.puml)

#### Przepływ:
1. Użytkownik wpisuje email i hasło w frontend'zie
2. Frontend wysyła żądanie `POST /api/auth/login` do AuthController
3. AuthController deleguje do AuthService
4. AuthService szuka użytkownika w UserRepository
5. UserRepository wykonuje zapytanie do bazy PostgreSQL
6. Jeśli użytkownik istnieje:
   - PasswordEncoder sprawdza hasło (BCrypt)
   - Jeśli prawidłowe → JwtService generuje token
   - Zwraca AuthResponse (token + dane użytkownika)
7. Frontend zapisuje token w localStorage
8. Użytkownik zostaje zalogowany

**Token JWT zawiera:**
- email (subject)
- userId
- role (ADMIN/TRAINER/CLIENT)
- expiration (24 godziny)

---

### 2️⃣ Rezerwacja Treningu

**Plik:** `docs/diagrams/sequence-reservation.puml`

![Sequence Reservation](sequence-reservation.puml)

#### Przepływ:
1. Klient wybiera trenera, typ treningu i dostępny termin
2. Frontend wysyła `POST /api/client/reservations`
3. ClientController deleguje do ReservationService
4. ReservationService sprawdza dostępność (AvailabilityService)
5. Jeśli termin dostępny:
   - Walidacja danych wejściowych
   - Zapis rezerwacji w bazie (status = **PENDING**)
   - Zwrot ReservationResponse
6. Frontend potwierdza rezerwację
7. Rezerwacja czeka na potwierdzenie trenera

**Status rezerwacji: PENDING** ➜ Czeka na trenera

---

### 3️⃣ Potwierdzenie Rezerwacji przez Trenera

**Plik:** `docs/diagrams/sequence-reservation-confirm.puml`

![Sequence Confirm](sequence-reservation-confirm.puml)

#### Przepływ:
1. Trener przegląda rezerwacje (status = PENDING)
2. Klika przycisk "Potwierdź rezerwację"
3. Frontend wysyła `PUT /api/trainer/reservations/{id}/confirm`
4. TrainerController deleguje do ReservationService
5. ReservationService sprawdza, czy rezerwacja należy do trenera
6. Zmienia status rezerwacji: **PENDING → CONFIRMED**
7. Zwraca zaktualizowaną rezerwację

**Możliwe przejścia stanu:**
- PENDING → CONFIRMED (trener potwierdza)
- PENDING → REJECTED (trener odrzuca)
- CONFIRMED → COMPLETED (trening odbył się)
- Dowolny status → CANCELLED (anulowanie)

---

### 4️⃣ Logowanie Wyniku Podnoszenia

**Plik:** `docs/diagrams/sequence-lift-result.puml`

![Sequence Lift Result](sequence-lift-result.puml)

#### Przepływ:
1. Klient wypełnia formularz wyniku:
   - Bój (Squat/Bench Press/Deadlift)
   - Ciężar (kg)
   - Powtórzenia
   - Data wyniku
2. Frontend wysyła `POST /api/client/lift-results`
3. ClientController deleguje do LiftResultService
4. LiftResultService waliduje dane
5. **Kalkulacja Estimated 1RM** (wzór Epley'a):
   ```
   e1RM = weight × (1 + reps/30)
   Przykład: 100 kg × 5 reps = 100 × (1 + 5/30) = 116.7 kg
   ```
6. Zapis do bazy danych
7. Zwrot wyniku
8. Frontend pokazuje nowy total

**Przechowywane dane:**
- Wszystkie wyniki historyczne
- Umożliwia śledzenie postępów na wykresach
- Obliczanie totalnego wyniku (Squat + Bench + Deadlift)

---

## Diagram Encji (ERD)

**Plik:** `docs/diagrams/database-schema.puml`

![Database Schema](database-schema.puml)

### Główne tabele:

#### 👥 **ROLES** (Role aplikacyjne)
- `id` (PK)
- `name` (ENUM: ADMIN, TRAINER, CLIENT)

#### 👤 **USERS** (Użytkownicy)
- `id` (PK)
- `role_id` (FK → ROLES)
- `email` (UNIQUE)
- `password_hash` (BCrypt)
- `first_name`, `last_name`
- `is_active` (aktywny/zablokowany)
- `created_at`

#### 💪 **TRAINER_PROFILES** (Profile trenerów)
- `id` (PK)
- `user_id` (FK → USERS, 1:1)
- `bio` (opis)
- `photo_url` (zdjęcie)
- `hourly_rate` (stawka godzinowa)
- `experience_years`

#### 📅 **RESERVATIONS** (Rezerwacje)
- `id` (PK)
- `client_id` (FK → USERS)
- `trainer_id` (FK → TRAINER_PROFILES)
- `training_type_id` (FK → TRAINING_TYPES)
- `start_time`, `end_time`
- `status` (PENDING/CONFIRMED/REJECTED/CANCELLED/COMPLETED)
- `cancellation_reason`

#### 💯 **LIFT_RESULTS** (Wyniki podnoszów)
- `id` (PK)
- `client_id` (FK → USERS)
- `lift_type_id` (FK → LIFT_TYPES)
- `weight_kg`, `reps`
- `estimated_one_rep_max`
- `result_date`

#### 📝 **TRAINING_NOTES** (Notatki)
- `id` (PK)
- `trainer_id` (FK → TRAINER_PROFILES)
- `client_id` (FK → USERS)
- `reservation_id` (FK → RESERVATIONS)
- `note` (treść notatki)

#### 🕐 **TRAINER_AVAILABILITY** (Dostępność trenera)
- `id` (PK)
- `trainer_id` (FK → TRAINER_PROFILES)
- `start_time`, `end_time`
- `available` (czy slot jest dostępny)

---

## Diagram Klas

**Plik:** `docs/diagrams/class-diagram.puml`

![Class Diagram](class-diagram.puml)

### Warstwy Architekturalnej:

#### 🏛️ **Domain Layer** (Encje)
Klasy reprezentujące biznesowe obiekty:
- `User`, `Role`, `TrainerProfile`
- `Reservation`, `LiftResult`
- `TrainingNote`, `TrainerAvailability`

#### 🔧 **Service Layer** (Logika biznesowa)
Interfejsy serwisów (implementacja w plikach `*Service.java`):
- `ReservationService` — zarządzanie rezerwacjami
- `AuthService` — logowanie i rejestracja
- `LiftResultService` — zarządzanie wynikami
- `AvailabilityService` — dostępność trenerów
- `TrainingNoteService` — notatki

#### 🌐 **Web Layer** (REST API)
Kontrolery HTTP:
- `ClientController` — endpointy dla klientów
- `TrainerController` — endpointy dla trenerów
- `AdminController` — endpointy dla admina
- `AuthController` — logowanie/rejestracja

#### 💾 **Repository Layer** (Dostęp do danych)
Interfejsy Spring Data JPA:
- `UserRepository`
- `ReservationRepository`
- `LiftResultRepository`

#### 🔐 **Security Layer**
- `JwtService` — generowanie i walidacja tokenów
- `UserPrincipal` — principal użytkownika dla Spring Security

---

## Jak generować wykresy z PlantUML

### Online Editor
1. Przejdź na: https://www.plantuml.com/plantuml/uml/
2. Skopiuj zawartość pliku `.puml`
3. Kliknij "Render"
4. Export → PNG

### VSCode Plugin
1. Zainstaluj: "PlantUML" by jebbs
2. Otwórz plik `.puml`
3. Prawy przycisk → "Preview PlantUML"
4. Export PNG

### Linia poleceń
```bash
# Zainstaluj PlantUML
brew install plantuml  # macOS
apt-get install plantuml  # Linux

# Generuj PNG
plantuml docs/diagrams/use-cases.puml
```

---

## 📝 Notatki

- Wszystkie diagramy są w formacie **PlantUML** (tekstowe, version-control friendly)
- Mogą być renderowane online lub lokalnie
- Należy aktualizować diagramy przy zmianach w architekturze
- Diagramy są dokumentacją żywą dla zespołu
