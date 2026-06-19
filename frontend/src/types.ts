export type Role = 'ADMIN' | 'TRAINER' | 'CLIENT';

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Specialization {
  id: number;
  name: string;
  description?: string;
}

export interface TrainingType {
  id: number;
  name: string;
  description?: string;
  durationMinutes: number;
  price: number;
  active: boolean;
}

export interface TrainingStation {
  id: number;
  name: string;
  description?: string;
  active: boolean;
}

export interface Trainer {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  active: boolean;
  bio?: string;
  photoUrl?: string;
  experienceYears?: number;
  hourlyRate?: number;
  specializations: Specialization[];
}

export interface Availability {
  id: number;
  trainerId: number;
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface AvailableSlot {
  startTime: string;
  endTime: string;
}

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface Reservation {
  id: number;
  clientId: number;
  clientName: string;
  trainerId: number;
  trainerName: string;
  trainingTypeId: number;
  trainingTypeName: string;
  trainingStationId?: number;
  trainingStationName?: string;
  startTime: string;
  endTime: string;
  status: ReservationStatus;
  cancellationReason?: string;
  createdAt: string;
}

export type LiftTypeName = 'SQUAT' | 'BENCH_PRESS' | 'DEADLIFT';

export interface LiftType {
  id: number;
  name: LiftTypeName;
  displayName: string;
}

export interface LiftResult {
  id: number;
  clientId: number;
  liftType: LiftTypeName;
  liftDisplayName: string;
  weightKg: number;
  reps: number;
  estimatedOneRepMax: number;
  resultDate: string;
  notes?: string;
  createdAt: string;
}

export interface ProgressPoint {
  date: string;
  liftType: LiftTypeName;
  estimatedOneRepMax: number;
  weightKg: number;
  reps: number;
}

export interface PowerliftingTotal {
  squat: number;
  benchPress: number;
  deadlift: number;
  total: number;
}

export interface TrainingNote {
  id: number;
  reservationId: number;
  trainerId: number;
  clientId: number;
  note: string;
  createdAt: string;
}

export interface Certificate {
  id: number;
  trainerId: number;
  name: string;
  issuingOrganization: string;
  issueDate: string;
  expirationDate?: string;
  certificateNumber?: string;
}

export interface AdminStats {
  users: number;
  trainers: number;
  clients: number;
  reservations: number;
  pendingReservations: number;
  completedReservations: number;
}
