// ReservationModal.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ReservationModal from '../components/reservationModal';
import { beforeEach, describe, expect, test, vi, Mocked } from 'vitest';
import axios from 'axios';

vi.mock('../components/editReservation', () => ({
    default: () => null, // or a dummy fallback
  }));  

// Mock axios
vi.mock('axios');
const mockedAxios = axios as Mocked<typeof axios>;

// Mock localStorage
vi.stubGlobal('localStorage', {
  getItem: vi.fn(() => 'mocked_token'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
});

describe('ReservationModal Component', () => {
  const mockOnClose = vi.fn();
  const mockTable = {
    locationId: 'loc123',
    locationAddress: '123 Main St',
    availableSlots: ['12:00-13:00', '13:00-14:00'],
    tableNumber: '5',
    capacity: '4',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders modal with correct information', () => {
    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        selectedSlot={{ fromTime: '12:00', toTime: '13:00' }}
        guests={2}
      />
    );

    const elements = screen.getAllByText(/make a reservation/i);
    expect(elements).toHaveLength(2);
    expect(screen.getByText(/123 Main St/i)).toBeInTheDocument();
    expect(screen.getByText(/Table 5/i)).toBeInTheDocument();
    expect(screen.getByText(/May 2, 2025/i)).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  test('increments and decrements guest count', () => {
    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        selectedSlot={{ fromTime: '12:00', toTime: '13:00' }}
        guests={2}
      />
    );

    const incrementButton = screen.getByText('+');
    const decrementButton = screen.getByText('-');

    fireEvent.click(incrementButton);
    expect(screen.getByText('3')).toBeInTheDocument();

    fireEvent.click(decrementButton);
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  test('selects from and to time slots', () => {
    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        guests={2}
      />
    );

    const fromSelect = screen.getByText('From').parentElement!.querySelector('select')!;
    fireEvent.change(fromSelect, { target: { value: '12:00' } });
    expect(fromSelect).toHaveValue('12:00');    

    const toSelect = screen.getByText('To').parentElement!.querySelector('select')!;
    fireEvent.change(toSelect, { target: { value: '13:00' } });
    expect(toSelect).toHaveValue('13:00');
  });

  test('handles successful reservation', async () => {
    mockedAxios.post.mockResolvedValueOnce({ data: { success: true } });

    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        selectedSlot={{ fromTime: '12:00', toTime: '13:00' }}
        guests={2}
      />
    );

    const reserveButton = screen.getByRole('button', {
  name: /make a reservation/i,
});
    fireEvent.click(reserveButton);

    await waitFor(() => {
      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.stringContaining('/bookings/client'),
        expect.objectContaining({
          locationId: 'loc123',
          tableNumber: '5',
          date: '2025-05-02',
          guestsNumber: '2',
          timeFrom: '12:00',
          timeTo: '13:00',
        }),
        expect.any(Object)
      );
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  test('handles reservation error', async () => {
    mockedAxios.post.mockRejectedValueOnce({
      response: { data: { error: 'Reservation failed' } },
    });

    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        selectedSlot={{ fromTime: '12:00', toTime: '13:00' }}
        guests={2}
      />
    );

    const reserveButton = screen.getByRole('button', { name: /Make a Reservation/i });
    fireEvent.click(reserveButton);

    await waitFor(() => {
        const elements = screen.getAllByText(/make a reservation/i);
        expect(elements).toHaveLength(2);        
    });
  });

  test('does not submit without selecting times', () => {
    render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        selectedDate="2025-05-02"
        guests={2}
      />
    );

    const reserveButton = screen.getByRole('button', { name: /Make a Reservation/i });
    fireEvent.click(reserveButton);

    expect(screen.getByText(/Please choose your preferred time from the dropdowns below/i)).toBeInTheDocument();
  });

  test('does not render when table is null', () => {
    const { container } = render(
      <ReservationModal
        isOpen={true}
        onClose={mockOnClose}
        table={null}
        selectedDate="2025-05-02"
        guests={2}
      />
    );

    expect(container.firstChild).toBeNull();
  });
});
