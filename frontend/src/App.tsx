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
import { FormEvent, useEffect, useMemo, useState } from 'react';
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
  LiftResult,
  LiftType,
  ProgressPoint,
  Reservation,
  Role,
  Specialization,
  Trainer,
  TrainingType,
  User,
} from './types';

type View = 'public' | 'client' | 'trainer' | 'admin' | 'profile';
type UiError = { status: 400 | 500; message: string };

const statusLabels: Record<string, string> = {
  PENDING: 'Oczekuje',
  CONFIRMED: 'Potwierdzona',
  REJECTED: 'Odrzucona',
  CANCELLED: 'Anulowana',
  COMPLETED: 'Zrealizowana',
};

export function App() {
  const [token, setToken] = useState(localStorage.getItem('kuznia.token') ?? '');
  const [user, setUser] = useState<User | null>(() => {
    const raw = localStorage.getItem('kuznia.user');
    return raw ? (JSON.parse(raw) as User) : null;
  });
  const [view, setView] = useState<View>('public');
  const [uiError, setUiError] = useState<UiError | null>(null);

  const authenticated = Boolean(token && user);

  function handleAuth(authToken: string, authUser: User) {
    localStorage.setItem('kuznia.token', authToken);
    localStorage.setItem('kuznia.user', JSON.stringify(authUser));
    setToken(authToken);
    setUser(authUser);
    setView(defaultView(authUser.role));
  }

  function logout() {
    localStorage.removeItem('kuznia.token');
    localStorage.removeItem('kuznia.user');
    setToken('');
    setUser(null);
    setView('public');
  }

  function handleApiError(error: unknown) {
    if (error instanceof ApiError) {
      if (error.status >= 500) {
        setUiError({ status: 500, message: error.body?.message ?? 'Serwer nie odpowiada prawidlowo.' });
        return;
      }
      if (error.status >= 400) {
        setUiError({ status: 400, message: error.body?.message ?? 'Nie udalo sie wykonac operacji.' });
        return;
      }
    }
    setUiError({ status: 500, message: 'Wystapil nieoczekiwany blad aplikacji.' });
  }

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
        {view === 'public' && <PublicView onError={handleApiError} />}
        {view === 'profile' && user && <ProfileView user={user} setUser={setUser} onError={handleApiError} />}
        {view === 'client' && user?.role === 'CLIENT' && <ClientDashboard onError={handleApiError} />}
        {view === 'trainer' && user?.role === 'TRAINER' && <TrainerDashboard onError={handleApiError} />}
        {view === 'admin' && user?.role === 'ADMIN' && <AdminDashboard onError={handleApiError} />}
      </main>
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
  const [email, setEmail] = useState('admin@kuznia.local');
  const [password, setPassword] = useState('Admin123!');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      const response = mode === 'login'
        ? await api.login(email, password)
        : await api.register({ email, password, firstName, lastName, phone });
      onAuth(response.token, response.user);
    } catch (error) {
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
      <input className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" />
      <input className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Haslo" type="password" />
      {mode === 'register' && (
        <>
          <input className="form-control" value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Imie" />
          <input className="form-control" value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Nazwisko" />
          <input className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="Telefon" />
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
        <h1>{error.status === 500 ? 'Blad serwera' : 'Nieudane zadanie'}</h1>
        <p>{error.message}</p>
        <button className="btn btn-warning" type="button" onClick={onBack}>Wroc do aplikacji</button>
      </div>
    </div>
  );
}

function PublicView({ onError }: { onError: (error: unknown) => void }) {
  const [trainers, setTrainers] = useState<Trainer[]>([]);
  const [trainingTypes, setTrainingTypes] = useState<TrainingType[]>([]);

  useEffect(() => {
    Promise.all([api.publicTrainers(), api.trainingTypes()])
      .then(([trainerData, typeData]) => {
        setTrainers(trainerData);
        setTrainingTypes(typeData);
      })
      .catch(onError);
  }, [onError]);

  return (
    <section>
      <Header eyebrow="Publiczna oferta" title="Trenerzy, terminy i treningi" />
      <div className="row g-3 mb-4">
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
      <div className="trainer-grid">
        {trainers.map((trainer) => <TrainerCard key={trainer.id} trainer={trainer} />)}
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
      <div>
        <h3>{trainer.firstName} {trainer.lastName}</h3>
        <p>{trainer.bio || 'Trener Kuzni'}</p>
        <div className="tag-row">
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
        <input className="form-control" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
        <input className="form-control" value={lastName} onChange={(e) => setLastName(e.target.value)} />
        <input className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
        <button className="btn btn-warning" type="submit">Zapisz</button>
      </form>
    </section>
  );
}

function ClientDashboard({ onError }: { onError: (error: unknown) => void }) {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [trainers, setTrainers] = useState<Trainer[]>([]);
  const [trainingTypes, setTrainingTypes] = useState<TrainingType[]>([]);
  const [results, setResults] = useState<LiftResult[]>([]);
  const [progress, setProgress] = useState<ProgressPoint[]>([]);
  const [total, setTotal] = useState({ total: 0, squat: 0, benchPress: 0, deadlift: 0 });

  const [trainerId, setTrainerId] = useState('');
  const [trainingTypeId, setTrainingTypeId] = useState('');
  const [startTime, setStartTime] = useState('');

  const [liftType, setLiftType] = useState('SQUAT');
  const [weightKg, setWeightKg] = useState('');
  const [reps, setReps] = useState('1');
  const [resultDate, setResultDate] = useState(new Date().toISOString().slice(0, 10));

  const refresh = () => {
    Promise.all([
      api.clientReservations(),
      api.publicTrainers(),
      api.trainingTypes(),
      api.liftResults(),
      api.progress(),
      api.total(),
    ])
      .then(([reservationData, trainerData, typeData, resultData, progressData, totalData]) => {
        setReservations(reservationData);
        setTrainers(trainerData);
        setTrainingTypes(typeData);
        setResults(resultData);
        setProgress(progressData);
        setTotal(totalData);
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

  async function reserve(event: FormEvent) {
    event.preventDefault();
    try {
      await api.requestReservation({
        trainerId: Number(trainerId),
        trainingTypeId: Number(trainingTypeId),
        startTime: new Date(startTime).toISOString(),
      });
      refresh();
    } catch (error) {
      onError(error);
    }
  }

  async function addResult(event: FormEvent) {
    event.preventDefault();
    try {
      await api.addLiftResult({
        liftType,
        weightKg: Number(weightKg),
        reps: Number(reps),
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
            <select className="form-select" value={trainerId} onChange={(e) => setTrainerId(e.target.value)}>
              <option value="">Trener</option>
              {trainers.map((trainer) => <option key={trainer.id} value={trainer.id}>{trainer.firstName} {trainer.lastName}</option>)}
            </select>
            <select className="form-select" value={trainingTypeId} onChange={(e) => setTrainingTypeId(e.target.value)}>
              <option value="">Typ treningu</option>
              {trainingTypes.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}
            </select>
            <input className="form-control" type="datetime-local" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
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
        <div className="col-lg-4">
          <form className="panel form-grid" onSubmit={addResult}>
            <h3>Dodaj wynik</h3>
            <select className="form-select" value={liftType} onChange={(e) => setLiftType(e.target.value)}>
              <option value="SQUAT">Przysiad</option>
              <option value="BENCH_PRESS">Wyciskanie lezac</option>
              <option value="DEADLIFT">Martwy ciag</option>
            </select>
            <input className="form-control" placeholder="Ciezar kg" value={weightKg} onChange={(e) => setWeightKg(e.target.value)} />
            <input className="form-control" placeholder="Powtorzenia" value={reps} onChange={(e) => setReps(e.target.value)} />
            <input className="form-control" type="date" value={resultDate} onChange={(e) => setResultDate(e.target.value)} />
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
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');

  const refresh = () => {
    Promise.all([api.trainerProfile(), api.trainerReservations(), api.trainerAvailability(), api.trainerClients()])
      .then(([profileData, reservationData, availabilityData, clientData]) => {
        setProfile(profileData);
        setReservations(reservationData);
        setAvailability(availabilityData);
        setClients(clientData);
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

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

  return (
    <section>
      <Header eyebrow="Panel trenera" title={profile ? `${profile.firstName} ${profile.lastName}` : 'Trener'} />
      <div className="row g-3">
        <div className="col-lg-4">
          <form className="panel form-grid" onSubmit={addAvailability}>
            <h3>Dostepnosc</h3>
            <input className="form-control" type="datetime-local" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
            <input className="form-control" type="datetime-local" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
            <button className="btn btn-warning" type="submit">Dodaj termin</button>
          </form>
          <div className="panel mt-3">
            <h3>Podopieczni</h3>
            {clients.map((client) => <p key={client.id}>{client.firstName} {client.lastName}</p>)}
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
                <button className="btn btn-sm btn-warning" onClick={() => api.completeReservation(reservation.id).then(refresh).catch(onError)}>Zrealizuj</button>
              )}
            </div>
          )} />
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

  const refresh = () => {
    Promise.all([
      api.adminStats(),
      api.adminUsers(),
      api.adminTrainers(),
      api.specializations(),
      api.adminTrainingTypes(),
    ])
      .then(([statsData, userData, trainerData, specializationData, typeData]) => {
        setStats(statsData);
        setUsers(userData);
        setTrainers(trainerData);
        setSpecializations(specializationData);
        setTrainingTypes(typeData);
      })
      .catch(onError);
  };

  useEffect(refresh, [onError]);

  async function createTrainer(event: FormEvent) {
    event.preventDefault();
    try {
      await api.createTrainer({
        ...trainerForm,
        hourlyRate: Number(trainerForm.hourlyRate || 0),
        experienceYears: 0,
        active: true,
      });
      setTrainerForm({ email: '', password: '', firstName: '', lastName: '', bio: '', photoUrl: '', hourlyRate: '', specializationIds: [] });
      refresh();
    } catch (error) {
      onError(error);
    }
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
          <form className="panel form-grid" onSubmit={createTrainer}>
            <h3>Nowy trener</h3>
            <input className="form-control" placeholder="Email" value={trainerForm.email} onChange={(e) => setTrainerForm({ ...trainerForm, email: e.target.value })} />
            <input className="form-control" placeholder="Haslo" type="password" value={trainerForm.password} onChange={(e) => setTrainerForm({ ...trainerForm, password: e.target.value })} />
            <input className="form-control" placeholder="Imie" value={trainerForm.firstName} onChange={(e) => setTrainerForm({ ...trainerForm, firstName: e.target.value })} />
            <input className="form-control" placeholder="Nazwisko" value={trainerForm.lastName} onChange={(e) => setTrainerForm({ ...trainerForm, lastName: e.target.value })} />
            <input className="form-control" placeholder="/uploads/trainers/kowalski.jpg" value={trainerForm.photoUrl} onChange={(e) => setTrainerForm({ ...trainerForm, photoUrl: e.target.value })} />
            <input className="form-control" placeholder="Stawka" value={trainerForm.hourlyRate} onChange={(e) => setTrainerForm({ ...trainerForm, hourlyRate: e.target.value })} />
            <textarea className="form-control" placeholder="Bio" value={trainerForm.bio} onChange={(e) => setTrainerForm({ ...trainerForm, bio: e.target.value })} />
            <select className="form-select" multiple value={trainerForm.specializationIds.map(String)} onChange={(e) => setTrainerForm({
              ...trainerForm,
              specializationIds: Array.from(e.target.selectedOptions).map((option) => Number(option.value)),
            })}>
              {specializations.map((specialization) => <option key={specialization.id} value={specialization.id}>{specialization.name}</option>)}
            </select>
            <button className="btn btn-warning" type="submit">Utworz trenera</button>
          </form>
        </div>
        <div className="col-xl-8">
          <div className="panel">
            <h3>Trenerzy</h3>
            <div className="trainer-list">
              {trainers.map((trainer) => <TrainerCard key={trainer.id} trainer={trainer} />)}
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
            {trainingTypes.map((type) => <div className="list-line" key={type.id}><span>{type.name}</span><strong>{type.price} PLN</strong></div>)}
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
    </div>
  );
}

function ProgressPanel({ progress }: { progress: ProgressPoint[] }) {
  const data = useMemo(() => progress.map((point) => ({
    date: point.date,
    value: point.estimatedOneRepMax,
    lift: point.liftType,
  })), [progress]);

  return (
    <div className="panel chart-panel">
      <h3>Wykres progresu</h3>
      <ResponsiveContainer width="100%" height={280}>
        <LineChart data={data}>
          <CartesianGrid stroke="#292929" />
          <XAxis dataKey="date" stroke="#a3a3a3" />
          <YAxis stroke="#a3a3a3" />
          <Tooltip contentStyle={{ background: '#171717', border: '1px solid #3a3a3a' }} />
          <Line type="monotone" dataKey="value" stroke="#f5c542" strokeWidth={3} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

function Header({ eyebrow, title }: { eyebrow: string; title: string }) {
  return (
    <header className="page-header">
      <span>{eyebrow}</span>
      <h1>{title}</h1>
    </header>
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
