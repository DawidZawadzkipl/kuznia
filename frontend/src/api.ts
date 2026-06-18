import type {
  AdminStats,
  ApiErrorBody,
  AuthResponse,
  Availability,
  AvailableSlot,
  LiftResult,
  LiftType,
  LiftTypeName,
  PowerliftingTotal,
  ProgressPoint,
  Reservation,
  Specialization,
  Trainer,
  TrainingNote,
  TrainingStation,
  TrainingType,
  User,
} from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorBody | null,
  ) {
    super(body?.message ?? `HTTP ${status}`);
  }
}

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

async function request<T>(path: string, method: Method = 'GET', data?: unknown): Promise<T> {
  const token = localStorage.getItem('kuznia.token');
  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      ...(data ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });

  if (!response.ok) {
    let body: ApiErrorBody | null = null;
    try {
      body = await response.json();
    } catch {
      body = null;
    }
    throw new ApiError(response.status, body);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

async function upload<T>(path: string, file: File): Promise<T> {
  const token = localStorage.getItem('kuznia.token');
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData,
  });

  if (!response.ok) {
    let body: ApiErrorBody | null = null;
    try {
      body = await response.json();
    } catch {
      body = null;
    }
    throw new ApiError(response.status, body);
  }

  return response.json() as Promise<T>;
}

export const api = {
  login: (email: string, password: string) => request<AuthResponse>('/api/auth/login', 'POST', { email, password }),
  register: (payload: unknown) => request<AuthResponse>('/api/auth/register', 'POST', payload),
  me: () => request<User>('/api/me'),
  updateMe: (payload: unknown) => request<User>('/api/me', 'PUT', payload),

  publicTrainers: () => request<Trainer[]>('/api/public/trainers'),
  publicTrainer: (id: number) => request<Trainer>(`/api/public/trainers/${id}`),
  publicAvailability: (id: number) => request<Availability[]>(`/api/public/trainers/${id}/availability`),
  publicAvailableSlots: (id: number) => request<AvailableSlot[]>(`/api/public/trainers/${id}/available-slots`),
  trainingTypes: () => request<TrainingType[]>('/api/public/training-types'),
  specializations: () => request<Specialization[]>('/api/public/specializations'),
  liftTypes: () => request<LiftType[]>('/api/public/lift-types'),

  clientReservations: () => request<Reservation[]>('/api/client/reservations'),
  clientHistory: () => request<Reservation[]>('/api/client/training-history'),
  requestReservation: (payload: unknown) => request<Reservation>('/api/client/reservations', 'POST', payload),
  cancelClientReservation: (id: number, reason?: string) =>
    request<Reservation>(`/api/client/reservations/${id}/cancel`, 'PUT', { reason }),
  liftResults: (liftType?: LiftTypeName) =>
    request<LiftResult[]>(`/api/client/lift-results${liftType ? `?liftType=${liftType}` : ''}`),
  addLiftResult: (payload: unknown) => request<LiftResult>('/api/client/lift-results', 'POST', payload),
  progress: (liftType?: LiftTypeName) =>
    request<ProgressPoint[]>(`/api/client/progress${liftType ? `?liftType=${liftType}` : ''}`),
  total: () => request<PowerliftingTotal>('/api/client/total'),
  clientNotes: () => request<TrainingNote[]>('/api/client/notes'),

  trainerProfile: () => request<Trainer>('/api/trainer/profile'),
  updateTrainerProfile: (payload: unknown) => request<Trainer>('/api/trainer/profile', 'PUT', payload),
  trainerAvailability: () => request<Availability[]>('/api/trainer/availability'),
  createAvailability: (payload: unknown) => request<Availability>('/api/trainer/availability', 'POST', payload),
  updateAvailability: (id: number, payload: unknown) => request<Availability>(`/api/trainer/availability/${id}`, 'PUT', payload),
  trainerReservations: () => request<Reservation[]>('/api/trainer/reservations'),
  confirmReservation: (id: number) => request<Reservation>(`/api/trainer/reservations/${id}/confirm`, 'PUT'),
  rejectReservation: (id: number) => request<Reservation>(`/api/trainer/reservations/${id}/reject`, 'PUT'),
  completeReservation: (id: number) => request<Reservation>(`/api/trainer/reservations/${id}/complete`, 'PUT'),
  trainerClients: () => request<User[]>('/api/trainer/clients'),
  trainerNotes: () => request<TrainingNote[]>('/api/trainer/notes'),
  addNote: (payload: unknown) => request<TrainingNote>('/api/trainer/notes', 'POST', payload),

  adminStats: () => request<AdminStats>('/api/admin/stats'),
  adminUsers: () => request<User[]>('/api/admin/users'),
  setUserStatus: (id: number, active: boolean) => request<User>(`/api/admin/users/${id}/status`, 'PUT', { active }),
  adminTrainers: () => request<Trainer[]>('/api/admin/trainers'),
  createTrainer: (payload: unknown) => request<Trainer>('/api/admin/trainers', 'POST', payload),
  updateTrainer: (id: number, payload: unknown) => request<Trainer>(`/api/admin/trainers/${id}`, 'PUT', payload),
  uploadTrainerPhoto: (file: File) => upload<{ url: string }>('/api/admin/uploads/trainer-photo', file),
  adminTrainingTypes: () => request<TrainingType[]>('/api/admin/training-types'),
  createTrainingType: (payload: unknown) => request<TrainingType>('/api/admin/training-types', 'POST', payload),
  adminStations: () => request<TrainingStation[]>('/api/admin/training-stations'),
  createStation: (payload: unknown) => request<TrainingStation>('/api/admin/training-stations', 'POST', payload),
};

export function resolveMediaUrl(photoUrl?: string) {
  if (!photoUrl) return '';
  if (photoUrl.startsWith('http://') || photoUrl.startsWith('https://')) return photoUrl;
  return `${API_BASE}${photoUrl.startsWith('/') ? photoUrl : `/${photoUrl}`}`;
}
