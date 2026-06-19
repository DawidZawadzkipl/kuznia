import {
  AlertTriangle,
  BarChart3,
  CalendarCheck,
  Check,
  Dumbbell,
  LogOut,
  Shield,
  UserCog,
  Users,
  X,
} from 'lucide-react';
import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { ApiError, api, resolveMediaUrl } from './api';
import type {
  AdminStats,
  Availability,
  AvailableSlot,
  Certificate,
  LiftResult,
  LiftType,
  ProgressPoint,
  Reservation,
  Role,
  Specialization,
  Trainer,
  TrainingNote,
  TrainingStation,
  TrainingType,
  User,
} from './types';

type View = 'public' | 'client' | 'trainer' | 'admin' | 'profile';
type UiError = { status: 401 | 500; message: string };

const statusLabels: Record<string, string> = {
  PENDING: 'Oczekuje',
  CONFIRMED: 'Potwierdzona',
  REJECTED: 'Odrzucona',
  CANCELLED: 'Anulowana',
  COMPLETED: 'Zrealizowana',
};

const liftLabels: Record<string, string> = {
  SQUAT: 'Przysiad',
  BENCH_PRESS: 'Wyciskanie',
  DEADLIFT: 'Martwy',
};

export function App() {
  const [token, setToken] = useState(localStorage.getItem('kuznia.token') ?? '');
  const [user, setUser] = useState<User | null>(() => {
    const raw = localStorage.getItem('kuznia.user');
    return raw ? (JSON.parse(raw) as User) : null;
  });
  const [view, setView] = useState<View>('public');
  const [uiError, setUiError] = useState<UiError | null>(null);
  const [notice, setNotice] = useState('');

  const authenticated = Boolean(token && user);

  function handleAuth(authToken: string, authUser: User) {
    localStorage.setItem('kuznia.token', authToken);
    localStorage.setItem('kuznia.user', JSON.stringify(authUser));
    setToken(authToken);
    setUser(authUser);
    setView(defaultView(authUser.role));
  }

  const logout = useCallback(() => {
    localStorage.removeItem('kuznia.token');
    localStorage.removeItem('kuznia.user');
    setToken('');
    setUser(null);
    setView('public');
  }, []);

  const handleApiError = useCallback((error: unknown) => {
    if (error instanceof ApiError) {
      if (error.status === 401) {
        logout();
        setUiError({ status: 401, message: 'Sesja wygasla albo token jest nieprawidlowy. Zaloguj sie ponownie.' });
        return;
      }
      if (error.status === 403) {
        setNotice(error.body?.message ?? 'Brak dostepu do tej operacji dla aktualnej roli.');
        return;
      }
      if (error.status >= 500) {
        setUiError({ status: 500, message: error.body?.message ?? 'Serwer nie odpowiada prawidlowo.' });
        return;
      }
      if (error.status >= 400) {
        setNotice(error.body?.message ?? 'Nie udalo sie wykonac operacji.');
        return;
      }
    }
    setUiError({ status: 500, message: 'Wystapil nieoczekiwany blad aplikacji.' });
  }, [logout]);

  if (uiError) {
    return <ErrorView error={uiError} onBack={() => setUiError(null)} />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="brand" type="button" onClick={() => setView('public')}>
          <span className="brand-mark">K</span>
          <span>
            <strong>Kuznia</strong>
            <small>Trojboj & coaching</small>
          </span>
        </button>

        <nav className="nav-menu">
          <NavButton active={view === 'public'} icon={<Dumbbell size={18} />} onClick={() => setView('public')}>
            Oferta
          </NavButton>
          {authenticated && (
            <NavButton active={view === 'profile'} icon={<UserCog size={18} />} onClick={() => setView('profile')}>
              Profil
            </NavButton>
          )}
          {user?.role === 'CLIENT' && (
            <NavButton active={view === 'client'} icon={<CalendarCheck size={18} />} onClick={() => setView('client')}>
              Panel klienta
            </NavButton>
          )}
          {user?.role === 'TRAINER' && (
            <NavButton active={view === 'trainer'} icon={<Users size={18} />} onClick={() => setView('trainer')}>
              Panel trenera
            </NavButton>
          )}
          {user?.role === 'ADMIN' && (
            <NavButton active={view === 'admin'} icon={<Shield size={18} />} onClick={() => setView('admin')}>
              Admin
            </NavButton>
          )}
        </nav>

        <div className="sidebar-footer">
          {authenticated && user ? (
            <>
              <div className="user-chip">
                <span>{user.firstName[0]}{user.lastName[0]}</span>
                <div>
                  <strong>{user.firstName} {user.lastName}</strong>
                  <small>{user.role}</small>
                </div>
              </div>
              <button className="btn btn-outline-light w-100" type="button" onClick={logout}>
                <LogOut size={16} /> Wyloguj
              </button>
            </>
          ) : (
            <AuthPanel onAuth={handleAuth} onError={handleApiError} />
          )}
        </div>
      </aside>

      <main className="main-content">
        {notice && <Notice message={notice} onClose={() => setNotice('')} />}
        {view === 'public' && <PublicView />}
        {view === 'profile' && user && <ProfileView user={user} setUser={setUser} onError={handleApiError} />}
        {view === 'client' && user?.role === 'CLIENT' && <ClientDashboard onError={handleApiError} />}
        {view === 'trainer' && user?.role === 'TRAINER' && <TrainerDashboard onError={handleApiError} />}
        {view === 'admin' && user?.role === 'ADMIN' && <AdminDashboard onError={handleApiError} />}
      </main>
    </div>
  );
}

function Notice({ message, onClose }: { message: string; onClose: () => void }) {
  return (
    <div className="notice-panel">
      <span>{message}</span>
      <button type="button" onClick={onClose}>Zamknij</button>
    </div>
  );
}

function defaultView(role: Role): View {
  if (role === 'ADMIN') return 'admin';
  if (role === 'TRAINER') return 'trainer';
  return 'client';
}

function NavButton({ active, icon, children, onClick }: {
  active: boolean;
  icon: React.ReactNode;
  children: React.ReactNode;
  onClick: () => void;
}) {
  return (
    <button className={`nav-link ${active ? 'active' : ''}`} type="button" onClick={onClick}>
      {icon}
      <span>{children}</span>
    </button>
  );
}

function AuthPanel({ onAuth, onError }: {
  onAuth: (token: string, user: User) => void;
  onError: (error: unknown) => void;
}) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [formError, setFormError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setFormError('');
    try {
      const response = mode === 'login'
        ? await api.login(email, password)
        : await api.register({ email, password, firstName, lastName, phone });
      onAuth(response.token, response.user);
    } catch (error) {
      if (error instanceof ApiError && error.status < 500) {
        setFormError(error.body?.message ?? 'Nie udalo sie zalogowac. Sprawdz dane i sprobuj ponownie.');
        return;
      }
      onError(error);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="auth-panel" onSubmit={submit}>
      <div className="btn-group w-100 mb-3" role="group">
        <button className={`btn ${mode === 'login' ? 'btn-warning' : 'btn-outline-warning'}`} type="button" onClick={() => setMode('login')}>Login</button>
        <button className={`btn ${mode === 'register' ? 'btn-warning' : 'btn-outline-warning'}`} type="button" onClick={() => setMode('register')}>Rejestracja</button>
      </div>
      <FieldLabel required>Email</FieldLabel>
      <input className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
      <FieldLabel required>Haslo</FieldLabel>
      <input className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
      {formError && <div className="alert alert-warning mb-0">{formError}</div>}
      {mode === 'register' && (
        <>
          <FieldLabel required>Imie</FieldLabel>
          <input className="form-control" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
          <FieldLabel required>Nazwisko</FieldLabel>
          <input className="form-control" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          <FieldLabel>Telefon opcjonalnie</FieldLabel>
          <input className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
        </>
      )}
      <button className="btn btn-warning w-100" disabled={loading} type="submit">
        {loading ? 'Przetwarzanie...' : mode === 'login' ? 'Zaloguj' : 'Utworz konto'}
      </button>
    </form>
  );
}

function ErrorView({ error, onBack }: { error: UiError; onBack: () => void }) {
  return (
    <div className="error-screen">
      <div className="error-card">
        <AlertTriangle size={44} />
        <span className="error-code">{error.status}</span>
        <h1>{error.status === 500 ? 'Blad serwera' : 'Sesja wygasla'}</h1>
        <p>{error.message}</p>
        <button className="btn btn-warning" type="button" onClick={onBack}>Wroc do aplikacji</button>
      </div>
    </div>
  );
}

function PublicView() {
	const [trainers, setTrainers] = useState<Trainer[]>([]);
	const [trainingTypes, setTrainingTypes] = useState<TrainingType[]>([]);
	const [loadError, setLoadError] = useState('');

	useEffect(() => {
		Promise.all([api.publicTrainers(), api.trainingTypes()])
			.then(([trainerData, typeData]) => {
				setLoadError('');
				setTrainers(trainerData);
				setTrainingTypes(typeData);
			})
			.catch(() => setLoadError('Backend jest niedostepny. Uruchom Docker Desktop, Postgresa i aplikacje Spring Boot na porcie 8082.'));
	}, []);

	return (
		<section className="landing-page">
      <div className="landing-hero">
        <div className="landing-hero-content">
          <span>Kuznia Trojboju</span>
          <h1>Kuznia</h1>
          <p>Tutaj toczymy najciezsze boje. Pracujemy nad przysiadem, wyciskaniem i martwym ciagiem w warunkach, w ktorych liczy sie technika, progres i spokojna glowa pod sztanga.</p>
          <div className="landing-actions">
            <a className="btn btn-warning" href="#trenerzy">Poznaj trenerow</a>
            <a className="btn btn-outline-warning" href="#treningi">Zobacz treningi</a>
          </div>
        </div>
        <div className="landing-facts" aria-label="Najwazniejsze informacje">
          <div>
            <strong>90 min</strong>
            <span>jedna sesja</span>
          </div>
          <div>
            <strong>3 boje</strong>
            <span>przysiad, lawka, martwy</span>
          </div>
          <div>
            <strong>1 cel</strong>
            <span>mocniejszy wynik</span>
          </div>
        </div>
      </div>

			{loadError && (
				<div className="panel border-warning mb-3">
					<strong className="text-warning">Nie mozna pobrac danych.</strong>
					<p className="mb-0 mt-2">{loadError}</p>
				</div>
			)}

      <div className="landing-section">
        <Header eyebrow="Jak trenujemy" title="Ciezka praca, proste zasady" />
        <div className="row g-3">
          <div className="col-lg-4">
            <div className="panel h-100">
              <h3>Silownia trojbojowa</h3>
              <p>Przestrzen do pracy nad przysiadem, wyciskaniem lezac i martwym ciagiem pod okiem trenerow.</p>
            </div>
          </div>
          <div className="col-lg-4">
            <div className="panel h-100">
              <h3>Sesje 90 minut</h3>
              <p>Kazdy termin rezerwacji jest liczony jako pelna sesja techniczna albo konsultacyjna.</p>
            </div>
          </div>
          <div className="col-lg-4">
            <div className="panel h-100">
              <h3>Progres i historia</h3>
              <p>Klient zapisuje wyniki bojow, obserwuje total i wraca do historii treningow.</p>
            </div>
          </div>
        </div>
      </div>

      <div className="landing-section" id="treningi">
        <Header eyebrow="Rodzaje treningow" title="Wybierz rodzaj pracy" />
			  <div className="row g-3">
          {trainingTypes.map((type) => (
            <div className="col-md-4" key={type.id}>
              <div className="panel h-100">
                <small className="text-warning">{type.durationMinutes} min</small>
                <h3>{type.name}</h3>
                <p>{type.description}</p>
                <strong>{type.price} PLN</strong>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="landing-section" id="trenerzy">
        <Header eyebrow="Trenerzy" title="Specjalizacje pod konkretne boje" />
        <div className="trainer-grid">
          {trainers.map((trainer) => <TrainerCard key={trainer.id} trainer={trainer} />)}
        </div>
      </div>
    </section>
  );
}

function TrainerCard({ trainer }: { trainer: Trainer }) {
  const image = resolveMediaUrl(trainer.photoUrl);
  return (
    <article className="trainer-card">
      <div className="trainer-photo">
        {image ? <img src={image} alt={`${trainer.firstName} ${trainer.lastName}`} /> : <Dumbbell size={42} />}
      </div>
      <div className="trainer-card-body">
        <h3>{trainer.firstName} {trainer.lastName}</h3>
        <p>{trainer.bio || 'Trener Kuzni'}</p>
        <div className="tag-row trainer-tags">
          {trainer.specializations.map((specialization) => (
            <span className="tag" key={specialization.id}>{specialization.name}</span>
          ))}
        </div>
      </div>
    </article>
  );
}

function ProfileView({ user, setUser, onError }: {
  user: User;
  setUser: (user: User) => void;
  onError: (error: unknown) => void;
}) {
  const [firstName, setFirstName] = useState(user.firstName);
  const [lastName, setLastName] = useState(user.lastName);
  const [phone, setPhone] = useState(user.phone ?? '');
  const [saved, setSaved] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      const updated = await api.updateMe({ firstName, lastName, phone });
      localStorage.setItem('kuznia.user', JSON.stringify(updated));
      setUser(updated);
      setSaved(true);
    } catch (error) {
      onError(error);
    }
  }

  return (
    <section>
      <Header eyebrow="Konto" title="Profil uzytkownika" />
      <form className="panel form-grid" onSubmit={submit}>
        {saved && <div className="alert alert-success">Zapisano zmiany.</div>}
        <FieldLabel required>Imie</FieldLabel>
        <input className="form-control" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
        <FieldLabel required>Nazwisko</FieldLabel>
        <input className="form-control" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
        <FieldLabel>Telefon opcjonalnie</FieldLabel>
        <input className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
        <button className="btn btn-warning" type="submit">Zapisz</button>
      </form>
    </section>
  );
}

function ClientDashboard({ onError }: { onError: (error: unknown) => void }) {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [history, setHistory] = useState<Reservation[]>([]);
  const [trainers, setTrainers] = useState<Trainer[]>([]);
  const [trainingTypes, setTrainingTypes] = useState<TrainingType[]>([]);
  const [results, setResults] = useState<LiftResult[]>([]);
  const [progress, setProgress] = useState<ProgressPoint[]>([]);
  const [total, setTotal] = useState({ total: 0, squat: 0, benchPress: 0, deadlift: 0 });
  const [notes, setNotes] = useState<TrainingNote[]>([]);

  const [trainerId, setTrainerId] = useState('');
  const [trainingTypeId, setTrainingTypeId] = useState('');
  const [startTime, setStartTime] = useState('');
  const [availableSlots, setAvailableSlots] = useState<AvailableSlot[]>([]);
  const [slotsLoading, setSlotsLoading] = useState(false);

  const [liftType, setLiftType] = useState('SQUAT');
  const [weightKg, setWeightKg] = useState('');
  const [reps, setReps] = useState('1');
  const [resultDate, setResultDate] = useState(new Date().toISOString().slice(0, 10));
  const [resultError, setResultError] = useState('');

  const refresh = () => {
    Promise.all([
      api.clientReservations(),
      api.clientHistory(),
      api.publicTrainers(),
      api.trainingTypes(),
      api.liftResults(),
      api.progress(),
      api.total(),
      api.clientNotes(),
    ])
      .then(([reservationData, historyData, trainerData, typeData, resultData, progressData, totalData, noteData]) => {
        setReservations(reservationData);
        setHistory(historyData);
        setTrainers(trainerData);
        setTrainingTypes(typeData);
        setResults(resultData);
        setProgress(progressData);
        setTotal(totalData);
        setNotes(noteData);
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

  useEffect(() => {
    setStartTime('');
    setAvailableSlots([]);
    if (!trainerId) {
      return;
    }
    setSlotsLoading(true);
    api.publicAvailableSlots(Number(trainerId))
      .then(setAvailableSlots)
      .catch(onError)
      .finally(() => setSlotsLoading(false));
  }, [trainerId, onError]);

  async function reserve(event: FormEvent) {
    event.preventDefault();
    try {
      await api.requestReservation({
        trainerId: Number(trainerId),
        trainingTypeId: Number(trainingTypeId),
        startTime,
      });
      setStartTime('');
      setAvailableSlots((current) => current.filter((slot) => slot.startTime !== startTime));
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  async function addResult(event: FormEvent) {
    event.preventDefault();
    const weightValue = Number(weightKg);
    const repsValue = Number(reps);
    if (!Number.isFinite(weightValue) || weightValue <= 0 || !Number.isInteger(repsValue) || repsValue <= 0 || !resultDate) {
      setResultError('Podaj poprawny ciezar, liczbe powtorzen i date wyniku.');
      return;
    }
    setResultError('');
    try {
      await api.addLiftResult({
        liftType,
        weightKg: weightValue,
        reps: repsValue,
        resultDate,
      });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  return (
    <section>
      <Header eyebrow="Panel klienta" title="Rezerwacje i progres" />
      <div className="row g-3">
        <div className="col-xl-4">
          <form className="panel form-grid" onSubmit={reserve}>
            <h3>Nowa rezerwacja</h3>
            <FieldLabel required>Trener</FieldLabel>
            <select className="form-select" value={trainerId} onChange={(e) => setTrainerId(e.target.value)} required>
              <option value="">Trener</option>
              {trainers.map((trainer) => <option key={trainer.id} value={trainer.id}>{trainer.firstName} {trainer.lastName}</option>)}
            </select>
            <FieldLabel required>Typ treningu</FieldLabel>
            <select className="form-select" value={trainingTypeId} onChange={(e) => setTrainingTypeId(e.target.value)} required>
              <option value="">Typ treningu</option>
              {trainingTypes.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}
            </select>
            <FieldLabel required>Termin</FieldLabel>
            <select
              className="form-select"
              disabled={!trainerId || slotsLoading || availableSlots.length === 0}
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              required
            >
              <option value="">
                {!trainerId
                  ? 'Najpierw wybierz trenera'
                  : slotsLoading
                    ? 'Ladowanie terminow...'
                    : availableSlots.length === 0
                      ? 'Brak dostepnych terminow'
                      : 'Wybierz termin'}
              </option>
              {availableSlots.map((slot) => (
                <option key={slot.startTime} value={slot.startTime}>
                  {formatDate(slot.startTime)} - {formatTime(slot.endTime)}
                </option>
              ))}
            </select>
            <button className="btn btn-warning" type="submit">Popros o termin</button>
          </form>
        </div>
        <div className="col-xl-8">
          <ReservationTable reservations={reservations} actions={(reservation) => (
            reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'
              ? <button className="btn btn-sm btn-outline-warning" onClick={() => api.cancelClientReservation(reservation.id).then(refresh).catch(onError)}>Anuluj</button>
              : null
          )} />
        </div>
      </div>

      <div className="row g-3 mt-1">
        <div className="col-lg-6">
          <div className="panel">
            <h3>Historia treningow</h3>
            <ReservationMiniList reservations={history} empty="Brak zrealizowanych treningow." />
          </div>
        </div>
        <div className="col-lg-6">
          <div className="panel">
            <h3>Notatki od trenera</h3>
            {notes.length === 0 && <p className="text-muted mb-0">Brak notatek treningowych.</p>}
            {notes.map((note) => (
              <div className="list-line align-items-start" key={note.id}>
                <span>{note.note}</span>
                <small>{formatDate(note.createdAt)}</small>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="row g-3 mt-1">
        <div className="col-lg-4">
          <form className="panel form-grid" onSubmit={addResult}>
            <h3>Dodaj wynik</h3>
            {resultError && <div className="alert alert-warning mb-0">{resultError}</div>}
            <FieldLabel required>Boj</FieldLabel>
            <select className="form-select" value={liftType} onChange={(e) => setLiftType(e.target.value)}>
              <option value="SQUAT">Przysiad</option>
              <option value="BENCH_PRESS">Wyciskanie lezac</option>
              <option value="DEADLIFT">Martwy ciag</option>
            </select>
            <FieldLabel required>Ciezar kg</FieldLabel>
            <input className="form-control" value={weightKg} onChange={(e) => setWeightKg(e.target.value)} type="number" min="0.01" step="0.01" required />
            <FieldLabel required>Powtorzenia</FieldLabel>
            <input className="form-control" value={reps} onChange={(e) => setReps(e.target.value)} type="number" min="1" step="1" required />
            <FieldLabel required>Data wyniku</FieldLabel>
            <input className="form-control" type="date" value={resultDate} onChange={(e) => setResultDate(e.target.value)} required />
            <button className="btn btn-warning" type="submit">Zapisz wynik</button>
          </form>
          <div className="panel mt-3">
            <h3>Total</h3>
            <div className="metric">{total.total} kg</div>
            <small>Przysiad {total.squat} / Lawka {total.benchPress} / Martwy {total.deadlift}</small>
          </div>
        </div>
        <div className="col-lg-8">
          <ProgressPanel progress={progress} />
          <div className="panel mt-3">
            <h3>Ostatnie wyniki</h3>
            <div className="table-responsive">
              <table className="table table-dark table-hover align-middle">
                <tbody>
                  {results.slice(-6).reverse().map((result) => (
                    <tr key={result.id}>
                      <td>{result.resultDate}</td>
                      <td>{result.liftDisplayName}</td>
                      <td>{result.weightKg} kg x {result.reps}</td>
                      <td>{result.estimatedOneRepMax} kg e1RM</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function TrainerDashboard({ onError }: { onError: (error: unknown) => void }) {
  const [profile, setProfile] = useState<Trainer | null>(null);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [availability, setAvailability] = useState<Availability[]>([]);
  const [clients, setClients] = useState<User[]>([]);
  const [specializations, setSpecializations] = useState<Specialization[]>([]);
  const [selectedClientId, setSelectedClientId] = useState('');
  const [clientResults, setClientResults] = useState<LiftResult[]>([]);
  const [clientProgress, setClientProgress] = useState<ProgressPoint[]>([]);
  const [notes, setNotes] = useState<TrainingNote[]>([]);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [profileForm, setProfileForm] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    bio: '',
    hourlyRate: '',
    specializationIds: [] as number[],
  });
  const [noteForm, setNoteForm] = useState({ reservationId: '', note: '' });

  const refresh = () => {
    Promise.all([
      api.trainerProfile(),
      api.trainerReservations(),
      api.trainerAvailability(),
      api.trainerClients(),
      api.specializations(),
      api.trainerNotes(),
    ])
      .then(([profileData, reservationData, availabilityData, clientData, specializationData, noteData]) => {
        setProfile(profileData);
        setReservations(reservationData);
        setAvailability(availabilityData);
        setClients(clientData);
        setSpecializations(specializationData);
        setNotes(noteData);
        setProfileForm({
          firstName: profileData.firstName,
          lastName: profileData.lastName,
          phone: profileData.phone ?? '',
          bio: profileData.bio ?? '',
          hourlyRate: profileData.hourlyRate ? String(profileData.hourlyRate) : '',
          specializationIds: profileData.specializations.map((specialization) => specialization.id),
        });
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

  useEffect(() => {
    if (!selectedClientId) {
      setClientResults([]);
      setClientProgress([]);
      return;
    }
    Promise.all([
      api.trainerClientLiftResults(Number(selectedClientId)),
      api.trainerClientProgress(Number(selectedClientId)),
    ])
      .then(([resultData, progressData]) => {
        setClientResults(resultData);
        setClientProgress(progressData);
      })
      .catch(onError);
  }, [selectedClientId, onError]);

  function toggleOwnSpecialization(specializationId: number) {
    setProfileForm((current) => ({
      ...current,
      specializationIds: current.specializationIds.includes(specializationId)
        ? current.specializationIds.filter((id) => id !== specializationId)
        : [...current.specializationIds, specializationId],
    }));
  }

  async function addAvailability(event: FormEvent) {
    event.preventDefault();
    try {
      await api.createAvailability({
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
        available: true,
      });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  async function saveProfile(event: FormEvent) {
    event.preventDefault();
    try {
      const updated = await api.updateTrainerProfile({
        ...profileForm,
        hourlyRate: profileForm.hourlyRate ? Number(profileForm.hourlyRate) : null,
      });
      setProfile(updated);
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  async function addTrainingNote(event: FormEvent) {
    event.preventDefault();
    try {
      await api.addNote({ reservationId: Number(noteForm.reservationId), note: noteForm.note });
      setNoteForm({ reservationId: '', note: '' });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  const completedReservations = reservations.filter((reservation) => reservation.status === 'COMPLETED');

  return (
    <section>
      <Header eyebrow="Panel trenera" title={profile ? `${profile.firstName} ${profile.lastName}` : 'Trener'} />
      <div className="row g-3">
        <div className="col-lg-4">
          <form className="panel form-grid mb-3" onSubmit={saveProfile}>
            <h3>Profil trenera</h3>
            <FieldLabel required>Imie</FieldLabel>
            <input className="form-control" value={profileForm.firstName} onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })} required />
            <FieldLabel required>Nazwisko</FieldLabel>
            <input className="form-control" value={profileForm.lastName} onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })} required />
            <FieldLabel>Telefon</FieldLabel>
            <input className="form-control" value={profileForm.phone} onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })} />
            <FieldLabel>Bio</FieldLabel>
            <textarea className="form-control" value={profileForm.bio} onChange={(e) => setProfileForm({ ...profileForm, bio: e.target.value })} />
            <FieldLabel>Stawka</FieldLabel>
            <input className="form-control" type="number" min="0" step="0.01" value={profileForm.hourlyRate} onChange={(e) => setProfileForm({ ...profileForm, hourlyRate: e.target.value })} />
            <FieldLabel required>Specjalizacje</FieldLabel>
            <div className="check-grid">
              {specializations.map((specialization) => (
                <label className="check-option" key={specialization.id}>
                  <input
                    checked={profileForm.specializationIds.includes(specialization.id)}
                    onChange={() => toggleOwnSpecialization(specialization.id)}
                    type="checkbox"
                  />
                  <span>{specialization.name}</span>
                </label>
              ))}
            </div>
            <button className="btn btn-warning" type="submit">Zapisz profil</button>
          </form>
          <form className="panel form-grid" onSubmit={addAvailability}>
            <h3>Dostepnosc</h3>
            <FieldLabel required>Poczatek</FieldLabel>
            <input className="form-control" type="datetime-local" value={startTime} onChange={(e) => setStartTime(e.target.value)} required />
            <FieldLabel required>Koniec</FieldLabel>
            <input className="form-control" type="datetime-local" value={endTime} onChange={(e) => setEndTime(e.target.value)} required />
            <button className="btn btn-warning" type="submit">Dodaj termin</button>
          </form>
          <div className="panel mt-3">
            <h3>Podopieczni</h3>
            <select className="form-select" value={selectedClientId} onChange={(e) => setSelectedClientId(e.target.value)}>
              <option value="">Wybierz podopiecznego</option>
              {clients.map((client) => <option key={client.id} value={client.id}>{client.firstName} {client.lastName}</option>)}
            </select>
          </div>
        </div>
        <div className="col-lg-8">
          <ReservationTable reservations={reservations} actions={(reservation) => (
            <div className="d-flex gap-2">
              {reservation.status === 'PENDING' && (
                <>
                  <IconButton title="Akceptuj" onClick={() => api.confirmReservation(reservation.id).then(refresh).catch(onError)}><Check size={16} /></IconButton>
                  <IconButton title="Odrzuc" onClick={() => api.rejectReservation(reservation.id).then(refresh).catch(onError)}><X size={16} /></IconButton>
                </>
              )}
              {reservation.status === 'CONFIRMED' && (
                <>
                  <button className="btn btn-sm btn-warning" onClick={() => api.completeReservation(reservation.id).then(refresh).catch(onError)}>Zrealizuj</button>
                  <button className="btn btn-sm btn-outline-warning" onClick={() => api.cancelTrainerReservation(reservation.id).then(refresh).catch(onError)}>Anuluj</button>
                </>
              )}
            </div>
          )} />
          <div className="row g-3 mt-1">
            <div className="col-lg-6">
              <div className="panel h-100">
                <h3>Wyniki podopiecznego</h3>
                {!selectedClientId && <p className="text-muted mb-0">Wybierz podopiecznego z listy.</p>}
                {selectedClientId && clientResults.length === 0 && <p className="text-muted mb-0">Brak wynikow.</p>}
                {clientResults.slice(-6).reverse().map((result) => (
                  <div className="list-line" key={result.id}>
                    <span>{result.resultDate} / {result.liftDisplayName}</span>
                    <strong>{result.estimatedOneRepMax} kg</strong>
                  </div>
                ))}
              </div>
            </div>
            <div className="col-lg-6">
              <ProgressPanel progress={clientProgress} />
            </div>
          </div>
          <form className="panel form-grid mt-3" onSubmit={addTrainingNote}>
            <h3>Notatka treningowa</h3>
            <FieldLabel required>Rezerwacja</FieldLabel>
            <select className="form-select" value={noteForm.reservationId} onChange={(e) => setNoteForm({ ...noteForm, reservationId: e.target.value })} required>
              <option value="">{completedReservations.length ? 'Wybierz zrealizowany trening' : 'Brak zrealizowanego treningu'}</option>
              {completedReservations.map((reservation) => (
                <option key={reservation.id} value={reservation.id}>
                  {formatDate(reservation.startTime)} - {reservation.clientName}
                </option>
              ))}
            </select>
            <FieldLabel required>Notatka</FieldLabel>
            <textarea className="form-control" value={noteForm.note} onChange={(e) => setNoteForm({ ...noteForm, note: e.target.value })} required />
            <button className="btn btn-warning" disabled={completedReservations.length === 0} type="submit">Dodaj notatke</button>
          </form>
          <div className="panel mt-3">
            <h3>Ostatnie notatki</h3>
            {notes.length === 0 && <p className="text-muted mb-0">Brak notatek.</p>}
            {notes.slice(0, 5).map((note) => (
              <div className="list-line align-items-start" key={note.id}>
                <span>{note.note}</span>
                <small>{formatDate(note.createdAt)}</small>
              </div>
            ))}
          </div>
          <div className="panel mt-3">
            <h3>Nadchodzaca dostepnosc</h3>
            {availability.map((slot) => (
              <div className="list-line" key={slot.id}>
                <span>{formatDate(slot.startTime)} - {formatDate(slot.endTime)}</span>
                <span className={slot.available ? 'text-success' : 'text-muted'}>{slot.available ? 'Aktywna' : 'Nieaktywna'}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function AdminDashboard({ onError }: { onError: (error: unknown) => void }) {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [trainers, setTrainers] = useState<Trainer[]>([]);
  const [specializations, setSpecializations] = useState<Specialization[]>([]);
  const [trainingTypes, setTrainingTypes] = useState<TrainingType[]>([]);
  const [stations, setStations] = useState<TrainingStation[]>([]);
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [editingTrainerId, setEditingTrainerId] = useState<number | null>(null);
  const [editingTrainingTypeId, setEditingTrainingTypeId] = useState<number | null>(null);
  const [editingSpecializationId, setEditingSpecializationId] = useState<number | null>(null);
  const [editingStationId, setEditingStationId] = useState<number | null>(null);
  const [editingCertificateId, setEditingCertificateId] = useState<number | null>(null);
  const [photoUploading, setPhotoUploading] = useState(false);

  const [trainerForm, setTrainerForm] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    bio: '',
    photoUrl: '',
    hourlyRate: '',
    specializationIds: [] as number[],
  });
  const [trainingTypeForm, setTrainingTypeForm] = useState({
    name: '',
    description: '',
    durationMinutes: '90',
    price: '',
  });
  const [specializationForm, setSpecializationForm] = useState({ name: '', description: '' });
  const [stationForm, setStationForm] = useState({ name: '', description: '' });
  const [certificateForm, setCertificateForm] = useState({
    trainerId: '',
    name: '',
    issuingOrganization: '',
    issueDate: '',
    expirationDate: '',
    certificateNumber: '',
  });

  const refresh = () => {
    Promise.all([
      api.adminStats(),
      api.adminUsers(),
      api.adminTrainers(),
      api.adminSpecializations(),
      api.adminTrainingTypes(),
      api.adminStations(),
      api.adminCertificates(),
      api.adminReservations(),
    ])
      .then(([statsData, userData, trainerData, specializationData, typeData, stationData, certificateData, reservationData]) => {
        setStats(statsData);
        setUsers(userData);
        setTrainers(trainerData);
        setSpecializations(specializationData);
        setTrainingTypes(typeData);
        setStations(stationData);
        setCertificates(certificateData);
        setReservations(reservationData);
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

  function resetTrainerForm() {
    setEditingTrainerId(null);
    setTrainerForm({ email: '', password: '', firstName: '', lastName: '', bio: '', photoUrl: '', hourlyRate: '', specializationIds: [] });
  }

  function editTrainer(trainer: Trainer) {
    setEditingTrainerId(trainer.id);
    setTrainerForm({
      email: trainer.email,
      password: '',
      firstName: trainer.firstName,
      lastName: trainer.lastName,
      bio: trainer.bio ?? '',
      photoUrl: trainer.photoUrl ?? '',
      hourlyRate: trainer.hourlyRate ? String(trainer.hourlyRate) : '',
      specializationIds: trainer.specializations.map((specialization) => specialization.id),
    });
  }

  function toggleSpecialization(specializationId: number) {
    setTrainerForm((current) => ({
      ...current,
      specializationIds: current.specializationIds.includes(specializationId)
        ? current.specializationIds.filter((id) => id !== specializationId)
        : [...current.specializationIds, specializationId],
    }));
  }

  async function handlePhotoFile(file?: File) {
    if (!file) return;
    setPhotoUploading(true);
    try {
      const response = await api.uploadTrainerPhoto(file);
      setTrainerForm((current) => ({ ...current, photoUrl: response.url }));
    } catch (error) {
      onError(error);
    } finally {
      setPhotoUploading(false);
    }
  }

  async function saveTrainer(event: FormEvent) {
    event.preventDefault();
    const payload = {
      ...trainerForm,
      password: editingTrainerId && !trainerForm.password ? null : trainerForm.password,
      hourlyRate: trainerForm.hourlyRate ? Number(trainerForm.hourlyRate) : null,
      experienceYears: 0,
      active: true,
    };

    try {
      if (editingTrainerId) {
        await api.updateTrainer(editingTrainerId, payload);
      } else {
        await api.createTrainer(payload);
      }
      resetTrainerForm();
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  async function createTrainingType(event: FormEvent) {
    event.preventDefault();
    try {
      const payload = {
        ...trainingTypeForm,
        durationMinutes: 90,
        price: Number(trainingTypeForm.price),
        active: true,
      };
      if (editingTrainingTypeId) {
        await api.updateTrainingType(editingTrainingTypeId, payload);
      } else {
        await api.createTrainingType(payload);
      }
      resetTrainingTypeForm();
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  function resetTrainingTypeForm() {
    setEditingTrainingTypeId(null);
    setTrainingTypeForm({ name: '', description: '', durationMinutes: '90', price: '' });
  }

  function editTrainingType(type: TrainingType) {
    setEditingTrainingTypeId(type.id);
    setTrainingTypeForm({
      name: type.name,
      description: type.description ?? '',
      durationMinutes: '90',
      price: String(type.price),
    });
  }

  async function saveSpecialization(event: FormEvent) {
    event.preventDefault();
    try {
      if (editingSpecializationId) {
        await api.updateSpecialization(editingSpecializationId, specializationForm);
      } else {
        await api.createSpecialization(specializationForm);
      }
      setEditingSpecializationId(null);
      setSpecializationForm({ name: '', description: '' });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  function editSpecialization(specialization: Specialization) {
    setEditingSpecializationId(specialization.id);
    setSpecializationForm({ name: specialization.name, description: specialization.description ?? '' });
  }

  async function saveStation(event: FormEvent) {
    event.preventDefault();
    const payload = { ...stationForm, active: true };
    try {
      if (editingStationId) {
        await api.updateStation(editingStationId, payload);
      } else {
        await api.createStation(payload);
      }
      setEditingStationId(null);
      setStationForm({ name: '', description: '' });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  function editStation(station: TrainingStation) {
    setEditingStationId(station.id);
    setStationForm({ name: station.name, description: station.description ?? '' });
  }

  async function saveCertificate(event: FormEvent) {
    event.preventDefault();
    const payload = {
      trainerId: Number(certificateForm.trainerId),
      name: certificateForm.name,
      issuingOrganization: certificateForm.issuingOrganization,
      issueDate: certificateForm.issueDate,
      expirationDate: certificateForm.expirationDate || null,
      certificateNumber: certificateForm.certificateNumber,
    };
    try {
      if (editingCertificateId) {
        await api.updateCertificate(editingCertificateId, payload);
      } else {
        await api.createCertificate(payload);
      }
      setEditingCertificateId(null);
      setCertificateForm({ trainerId: '', name: '', issuingOrganization: '', issueDate: '', expirationDate: '', certificateNumber: '' });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  function editCertificate(certificate: Certificate) {
    setEditingCertificateId(certificate.id);
    setCertificateForm({
      trainerId: String(certificate.trainerId),
      name: certificate.name,
      issuingOrganization: certificate.issuingOrganization,
      issueDate: certificate.issueDate,
      expirationDate: certificate.expirationDate ?? '',
      certificateNumber: certificate.certificateNumber ?? '',
    });
  }

  return (
    <section>
      <Header eyebrow="Panel administratora" title="Zarzadzanie silownia" />
      {stats && (
        <div className="metric-grid mb-3">
          <Metric icon={<Users />} label="Uzytkownicy" value={stats.users} />
          <Metric icon={<Dumbbell />} label="Trenerzy" value={stats.trainers} />
          <Metric icon={<CalendarCheck />} label="Rezerwacje" value={stats.reservations} />
          <Metric icon={<BarChart3 />} label="Zrealizowane" value={stats.completedReservations} />
        </div>
      )}

      <div className="row g-3">
        <div className="col-xl-4">
          <form className="panel form-grid" onSubmit={saveTrainer}>
            <div className="d-flex justify-content-between align-items-center gap-2">
              <h3 className="mb-0">{editingTrainerId ? 'Edycja trenera' : 'Nowy trener'}</h3>
              {editingTrainerId && <button className="btn btn-sm btn-outline-light" type="button" onClick={resetTrainerForm}>Anuluj</button>}
            </div>
            <FieldLabel required>Email</FieldLabel>
            <input className="form-control" value={trainerForm.email} onChange={(e) => setTrainerForm({ ...trainerForm, email: e.target.value })} type="email" required />
            <FieldLabel required={!editingTrainerId}>{editingTrainerId ? 'Nowe haslo opcjonalnie' : 'Haslo'}</FieldLabel>
            <input className="form-control" type="password" value={trainerForm.password} onChange={(e) => setTrainerForm({ ...trainerForm, password: e.target.value })} required={!editingTrainerId} />
            <FieldLabel required>Imie</FieldLabel>
            <input className="form-control" value={trainerForm.firstName} onChange={(e) => setTrainerForm({ ...trainerForm, firstName: e.target.value })} required />
            <FieldLabel required>Nazwisko</FieldLabel>
            <input className="form-control" value={trainerForm.lastName} onChange={(e) => setTrainerForm({ ...trainerForm, lastName: e.target.value })} required />
            <FieldLabel>Zdjecie trenera</FieldLabel>
            <div
              className="file-drop"
              onDragOver={(event) => event.preventDefault()}
              onDrop={(event) => {
                event.preventDefault();
                void handlePhotoFile(event.dataTransfer.files[0]);
              }}
            >
              {trainerForm.photoUrl && (
                <img className="photo-preview" src={resolveMediaUrl(trainerForm.photoUrl)} alt="Podglad zdjecia trenera" />
              )}
              <input className="form-control" type="file" accept="image/*" onChange={(event) => void handlePhotoFile(event.target.files?.[0])} />
              <small>{photoUploading ? 'Przesylanie zdjecia...' : 'Przeciagnij plik tutaj albo wybierz go z komputera.'}</small>
            </div>
            <FieldLabel>Stawka godzinowa</FieldLabel>
            <input className="form-control" value={trainerForm.hourlyRate} onChange={(e) => setTrainerForm({ ...trainerForm, hourlyRate: e.target.value })} type="number" min="0" step="0.01" />
            <FieldLabel>Bio</FieldLabel>
            <textarea className="form-control" value={trainerForm.bio} onChange={(e) => setTrainerForm({ ...trainerForm, bio: e.target.value })} />
            <FieldLabel required>Specjalizacje</FieldLabel>
            <div className="check-grid">
              {specializations.map((specialization) => (
                <label className="check-option" key={specialization.id}>
                  <input
                    checked={trainerForm.specializationIds.includes(specialization.id)}
                    onChange={() => toggleSpecialization(specialization.id)}
                    type="checkbox"
                  />
                  <span>{specialization.name}</span>
                </label>
              ))}
            </div>
            <button className="btn btn-warning" type="submit">{editingTrainerId ? 'Zapisz trenera' : 'Utworz trenera'}</button>
          </form>

          <form className="panel form-grid mt-3" onSubmit={createTrainingType}>
            <div className="d-flex justify-content-between align-items-center gap-2">
              <h3 className="mb-0">{editingTrainingTypeId ? 'Edycja typu treningu' : 'Nowy typ treningu'}</h3>
              {editingTrainingTypeId && <button className="btn btn-sm btn-outline-light" type="button" onClick={resetTrainingTypeForm}>Anuluj</button>}
            </div>
            <FieldLabel required>Nazwa</FieldLabel>
            <input className="form-control" value={trainingTypeForm.name} onChange={(e) => setTrainingTypeForm({ ...trainingTypeForm, name: e.target.value })} required />
            <FieldLabel>Opis</FieldLabel>
            <textarea className="form-control" value={trainingTypeForm.description} onChange={(e) => setTrainingTypeForm({ ...trainingTypeForm, description: e.target.value })} />
            <FieldLabel>Czas trwania</FieldLabel>
            <div className="locked-field">90 minut</div>
            <FieldLabel required>Cena PLN</FieldLabel>
            <input className="form-control" type="number" min="1" step="0.01" value={trainingTypeForm.price} onChange={(e) => setTrainingTypeForm({ ...trainingTypeForm, price: e.target.value })} required />
            <button className="btn btn-warning" type="submit">{editingTrainingTypeId ? 'Zapisz typ' : 'Dodaj typ treningu'}</button>
          </form>

          <form className="panel form-grid mt-3" onSubmit={saveSpecialization}>
            <h3>{editingSpecializationId ? 'Edycja specjalizacji' : 'Nowa specjalizacja'}</h3>
            <FieldLabel required>Nazwa</FieldLabel>
            <input className="form-control" value={specializationForm.name} onChange={(e) => setSpecializationForm({ ...specializationForm, name: e.target.value })} required />
            <FieldLabel>Opis</FieldLabel>
            <textarea className="form-control" value={specializationForm.description} onChange={(e) => setSpecializationForm({ ...specializationForm, description: e.target.value })} />
            <button className="btn btn-warning" type="submit">{editingSpecializationId ? 'Zapisz specjalizacje' : 'Dodaj specjalizacje'}</button>
          </form>

          <form className="panel form-grid mt-3" onSubmit={saveStation}>
            <h3>{editingStationId ? 'Edycja stanowiska' : 'Nowe stanowisko'}</h3>
            <FieldLabel required>Nazwa</FieldLabel>
            <input className="form-control" value={stationForm.name} onChange={(e) => setStationForm({ ...stationForm, name: e.target.value })} required />
            <FieldLabel>Opis</FieldLabel>
            <textarea className="form-control" value={stationForm.description} onChange={(e) => setStationForm({ ...stationForm, description: e.target.value })} />
            <button className="btn btn-warning" type="submit">{editingStationId ? 'Zapisz stanowisko' : 'Dodaj stanowisko'}</button>
          </form>

          <form className="panel form-grid mt-3" onSubmit={saveCertificate}>
            <h3>{editingCertificateId ? 'Edycja certyfikatu' : 'Nowy certyfikat'}</h3>
            <FieldLabel required>Trener</FieldLabel>
            <select className="form-select" value={certificateForm.trainerId} onChange={(e) => setCertificateForm({ ...certificateForm, trainerId: e.target.value })} required>
              <option value="">Wybierz trenera</option>
              {trainers.map((trainer) => <option key={trainer.id} value={trainer.id}>{trainer.firstName} {trainer.lastName}</option>)}
            </select>
            <FieldLabel required>Nazwa</FieldLabel>
            <input className="form-control" value={certificateForm.name} onChange={(e) => setCertificateForm({ ...certificateForm, name: e.target.value })} required />
            <FieldLabel required>Organizacja</FieldLabel>
            <input className="form-control" value={certificateForm.issuingOrganization} onChange={(e) => setCertificateForm({ ...certificateForm, issuingOrganization: e.target.value })} required />
            <FieldLabel required>Data wydania</FieldLabel>
            <input className="form-control" type="date" value={certificateForm.issueDate} onChange={(e) => setCertificateForm({ ...certificateForm, issueDate: e.target.value })} required />
            <FieldLabel>Data waznosci</FieldLabel>
            <input className="form-control" type="date" value={certificateForm.expirationDate} onChange={(e) => setCertificateForm({ ...certificateForm, expirationDate: e.target.value })} />
            <FieldLabel>Numer</FieldLabel>
            <input className="form-control" value={certificateForm.certificateNumber} onChange={(e) => setCertificateForm({ ...certificateForm, certificateNumber: e.target.value })} />
            <button className="btn btn-warning" type="submit">{editingCertificateId ? 'Zapisz certyfikat' : 'Dodaj certyfikat'}</button>
          </form>
        </div>
        <div className="col-xl-8">
          <div className="panel">
            <h3>Trenerzy</h3>
            <div className="trainer-list">
              {trainers.map((trainer) => (
                <div className="trainer-admin-item" key={trainer.id}>
                  <TrainerCard trainer={trainer} />
                  <button className="btn btn-sm btn-outline-warning" type="button" onClick={() => editTrainer(trainer)}>Edytuj</button>
                </div>
              ))}
            </div>
          </div>
          <div className="panel mt-3">
            <h3>Uzytkownicy</h3>
            <div className="table-responsive">
              <table className="table table-dark table-hover align-middle">
                <tbody>
                  {users.map((entry) => (
                    <tr key={entry.id}>
                      <td>{entry.firstName} {entry.lastName}</td>
                      <td>{entry.email}</td>
                      <td>{entry.role}</td>
                      <td>
                        <button className="btn btn-sm btn-outline-warning" onClick={() => api.setUserStatus(entry.id, !entry.active).then(refresh).catch(onError)}>
                          {entry.active ? 'Zablokuj' : 'Aktywuj'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="panel mt-3">
            <h3>Typy treningow</h3>
            {trainingTypes.map((type) => (
              <div className="list-line" key={type.id}>
                <span>{type.name}</span>
                <div className="d-flex align-items-center gap-2">
                  <strong>{type.price} PLN</strong>
                  <button className="btn btn-sm btn-outline-warning" type="button" onClick={() => editTrainingType(type)}>Edytuj</button>
                </div>
              </div>
            ))}
          </div>
          <div className="panel mt-3">
            <h3>Specjalizacje</h3>
            {specializations.map((specialization) => (
              <div className="list-line" key={specialization.id}>
                <span>{specialization.name}</span>
                <button className="btn btn-sm btn-outline-warning" type="button" onClick={() => editSpecialization(specialization)}>Edytuj</button>
              </div>
            ))}
          </div>
          <div className="panel mt-3">
            <h3>Stanowiska treningowe</h3>
            {stations.map((station) => (
              <div className="list-line" key={station.id}>
                <span>{station.name}</span>
                <button className="btn btn-sm btn-outline-warning" type="button" onClick={() => editStation(station)}>Edytuj</button>
              </div>
            ))}
          </div>
          <div className="panel mt-3">
            <h3>Certyfikaty trenerow</h3>
            {certificates.length === 0 && <p className="text-muted mb-0">Brak certyfikatow.</p>}
            {certificates.map((certificate) => (
              <div className="list-line align-items-start" key={certificate.id}>
                <span>
                  <strong>{certificate.name}</strong>
                  <small>{certificate.issuingOrganization} / {certificate.issueDate}</small>
                </span>
                <button className="btn btn-sm btn-outline-warning" type="button" onClick={() => editCertificate(certificate)}>Edytuj</button>
              </div>
            ))}
          </div>
          <div className="panel mt-3">
            <h3>Wszystkie rezerwacje</h3>
            <ReservationTableContent reservations={reservations} />
          </div>
        </div>
      </div>
    </section>
  );
}

function ReservationTable({ reservations, actions }: {
  reservations: Reservation[];
  actions?: (reservation: Reservation) => React.ReactNode;
}) {
  return (
    <div className="panel">
      <h3>Rezerwacje</h3>
      <ReservationTableContent reservations={reservations} actions={actions} />
    </div>
  );
}

function ReservationTableContent({ reservations, actions }: {
  reservations: Reservation[];
  actions?: (reservation: Reservation) => React.ReactNode;
}) {
  if (reservations.length === 0) {
    return <p className="text-muted mb-0">Brak rezerwacji.</p>;
  }
  return (
    <div className="table-responsive">
      <table className="table table-dark table-hover align-middle">
        <thead>
          <tr>
            <th>Termin</th>
            <th>Klient</th>
            <th>Trener</th>
            <th>Typ</th>
            <th>Status</th>
            {actions && <th></th>}
          </tr>
        </thead>
        <tbody>
          {reservations.map((reservation) => (
            <tr key={reservation.id}>
              <td>{formatDate(reservation.startTime)}</td>
              <td>{reservation.clientName}</td>
              <td>{reservation.trainerName}</td>
              <td>{reservation.trainingTypeName}</td>
              <td><span className={`status status-${reservation.status.toLowerCase()}`}>{statusLabels[reservation.status]}</span></td>
              {actions && <td>{actions(reservation)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ReservationMiniList({ reservations, empty }: { reservations: Reservation[]; empty: string }) {
  if (reservations.length === 0) {
    return <p className="text-muted mb-0">{empty}</p>;
  }
  return (
    <>
      {reservations.slice(0, 6).map((reservation) => (
        <div className="list-line" key={reservation.id}>
          <span>{formatDate(reservation.startTime)} / {reservation.trainerName}</span>
          <span className={`status status-${reservation.status.toLowerCase()}`}>{statusLabels[reservation.status]}</span>
        </div>
      ))}
    </>
  );
}

function ProgressPanel({ progress }: { progress: ProgressPoint[] }) {
  const [chartMode, setChartMode] = useState<'TOTAL' | 'SQUAT' | 'BENCH_PRESS' | 'DEADLIFT'>('TOTAL');
  const data = useMemo(() => buildProgressData(progress, chartMode), [progress, chartMode]);

  return (
    <div className="panel chart-panel">
      <div className="chart-header">
        <h3>Wykres progresu</h3>
        <div className="segmented-control" aria-label="Zakres wykresu">
          {(['TOTAL', 'SQUAT', 'BENCH_PRESS', 'DEADLIFT'] as const).map((mode) => (
            <button
              className={chartMode === mode ? 'active' : ''}
              key={mode}
              onClick={() => setChartMode(mode)}
              type="button"
            >
              {mode === 'TOTAL' ? 'Total' : liftLabels[mode]}
            </button>
          ))}
        </div>
      </div>
      {data.length === 0 ? (
        <div className="empty-chart">Brak danych dla wybranego zakresu.</div>
      ) : (
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={data}>
            <CartesianGrid stroke="#292929" />
            <XAxis dataKey="date" stroke="#a3a3a3" />
            <YAxis stroke="#a3a3a3" />
            <Tooltip contentStyle={{ background: '#171717', border: '1px solid #3a3a3a' }} />
            <Line type="monotone" dataKey="value" stroke="#f5c542" strokeWidth={3} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

function buildProgressData(
  progress: ProgressPoint[],
  chartMode: 'TOTAL' | 'SQUAT' | 'BENCH_PRESS' | 'DEADLIFT',
) {
  const sorted = [...progress].sort((first, second) => first.date.localeCompare(second.date));
  if (chartMode !== 'TOTAL') {
    return sorted
      .filter((point) => point.liftType === chartMode)
      .map((point) => ({
        date: point.date,
        value: point.estimatedOneRepMax,
        label: liftLabels[point.liftType],
      }));
  }

  const best = {
    SQUAT: 0,
    BENCH_PRESS: 0,
    DEADLIFT: 0,
  };
  return sorted.map((point) => {
    best[point.liftType] = Math.max(best[point.liftType], Number(point.estimatedOneRepMax));
    return {
      date: point.date,
      value: best.SQUAT + best.BENCH_PRESS + best.DEADLIFT,
      label: 'Total',
    };
  });
}

function Header({ eyebrow, title }: { eyebrow: string; title: string }) {
  return (
    <header className="page-header">
      <span>{eyebrow}</span>
      <h1>{title}</h1>
    </header>
  );
}

function FieldLabel({ children }: { children: React.ReactNode; required?: boolean }) {
  return (
    <label className="field-label">
      {children}
      <span aria-label="pole formularza">*</span>
    </label>
  );
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: number }) {
  return (
    <div className="metric-card">
      {icon}
      <small>{label}</small>
      <strong>{value}</strong>
    </div>
  );
}

function IconButton({ title, children, onClick }: {
  title: string;
  children: React.ReactNode;
  onClick: () => void;
}) {
  return (
    <button className="icon-button" title={title} type="button" onClick={onClick}>
      {children}
    </button>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pl-PL', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('pl-PL', {
    timeStyle: 'short',
  }).format(new Date(value));
}
