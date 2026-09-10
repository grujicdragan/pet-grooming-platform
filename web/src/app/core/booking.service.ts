import { Injectable, computed, signal } from '@angular/core';

export type BookingStatus = 'scheduled' | 'completed' | 'cancelled';

export interface Booking {
  id: string;
  petName: string;
  service: string;
  serviceId: string;
  date: string;
  time: string;
  discounted: boolean;
  price: number;
  ownerName: string;
  ownerEmail: string;
  status: BookingStatus;
}

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly bookingsSignal = signal<Booking[]>(this.seed());
  private readonly lastIdSignal = signal<string | null>(null);

  readonly bookings = this.bookingsSignal.asReadonly();
  readonly lastBooking = computed(() => {
    const id = this.lastIdSignal();
    return this.bookingsSignal().find((item) => item.id === id) ?? null;
  });

  save(booking: Omit<Booking, 'id' | 'status'>): Booking {
    const created: Booking = {
      ...booking,
      id: crypto.randomUUID(),
      status: 'scheduled',
    };
    this.bookingsSignal.update((list) =>
      [...list, created].sort(compareBookings),
    );
    this.lastIdSignal.set(created.id);
    return created;
  }

  setStatus(id: string, status: BookingStatus): void {
    this.bookingsSignal.update((list) =>
      list.map((item) => (item.id === id ? { ...item, status } : item)),
    );
  }

  private seed(): Booking[] {
    const rows: Booking[] = [
      {
        id: 'seed-1',
        petName: 'Luna',
        service: 'Pun grooming',
        serviceId: 'full-groom',
        date: '2026-09-11',
        time: '10:30',
        discounted: false,
        price: 4500,
        ownerName: 'Mila Jovanović',
        ownerEmail: 'mila@primer.rs',
        status: 'scheduled',
      },
      {
        id: 'seed-2',
        petName: 'Bobi',
        service: 'Kupanje i feniranje',
        serviceId: 'bath',
        date: '2026-09-11',
        time: '13:30',
        discounted: true,
        price: 1680,
        ownerName: 'Nikola Petrović',
        ownerEmail: 'nikola@primer.rs',
        status: 'scheduled',
      },
      {
        id: 'seed-3',
        petName: 'Maca',
        service: 'Šišanje noktiju',
        serviceId: 'nails',
        date: '2026-09-12',
        time: '09:00',
        discounted: false,
        price: 900,
        ownerName: 'Ivana Savić',
        ownerEmail: 'ivana@primer.rs',
        status: 'scheduled',
      },
      {
        id: 'seed-4',
        petName: 'Rex',
        service: 'Spa paket',
        serviceId: 'spa',
        date: '2026-09-08',
        time: '15:00',
        discounted: false,
        price: 3900,
        ownerName: 'Stefan Kostić',
        ownerEmail: 'stefan@primer.rs',
        status: 'completed',
      },
    ];
    return rows.sort(compareBookings);
  }
}

function compareBookings(a: Booking, b: Booking): number {
  return `${a.date}${a.time}`.localeCompare(`${b.date}${b.time}`);
}
