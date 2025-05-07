// EditReservationDialog.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import EditReservationDialog from '../components/editReservation';
import { describe, test, vi, beforeEach, expect, Mocked } from 'vitest';
import axios from 'axios';

// Mock axios and localStorage
vi.mock('axios');
const mockedAxios = axios as Mocked<typeof axios>;

vi.stubGlobal('localStorage', {
  getItem: vi.fn(() => 'mocked_token'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
});

const mockOnClose = vi.fn();
const mockOnUpdate = vi.fn();

const reservation = {
  id: 1,
  locationAddress: '456 Main St',
  date: '2025-05-10',
  timeSlot: '12:15-13:45',
  guestsNumber: 2,
};

describe('EditReservationDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders modal with reservation details', () => {
    render(
      <EditReservationDialog
        isOpen={true}
        reservation={reservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    expect(screen.getByDisplayValue('2025-05-10')).toBeInTheDocument();
    expect(screen.getByText('12:15')).toBeInTheDocument();
    expect(screen.getByText('13:45')).toBeInTheDocument();
    expect(screen.getByDisplayValue('2')).toBeInTheDocument();
  });

  test('disables "To" select if "From" is not selected', () => {
    const customReservation = { ...reservation, timeSlot: '-' };

    render(
      <EditReservationDialog
        isOpen={true}
        reservation={customReservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    const toSelect = screen.getAllByText(/Select time/)[1].closest('button');
    expect(toSelect).toBeDisabled();
  });

  test('handles guest number input change', () => {
    render(
      <EditReservationDialog
        isOpen={true}
        reservation={reservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    const input = screen.getByRole('spinbutton'); // for type="number"
    fireEvent.change(input, { target: { value: '4' } });
    expect((input as HTMLInputElement).value).toBe('4');
  });

  test('successfully updates reservation', async () => {
    mockedAxios.put.mockResolvedValueOnce({
      data: { message: 'Updated successfully!' },
    });

    render(
      <EditReservationDialog
        isOpen={true}
        reservation={reservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    const button = screen.getByRole('button', { name: /update/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining('/bookings/client/1'),
        expect.objectContaining({
          date: '2025-05-10',
          guestsNumber: 2,
          timeFrom: '12:15',
          timeTo: '13:45',
        }),
        expect.anything()
      );
      expect(mockOnUpdate).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  test('handles reservation update error', async () => {
    mockedAxios.put.mockRejectedValueOnce({
      response: { data: { error: 'Update failed' } },
    });

    render(
      <EditReservationDialog
        isOpen={true}
        reservation={reservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    const button = screen.getByRole('button', { name: /update/i });
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalled();
      expect(mockOnUpdate).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  test('does not render if reservation is null', () => {
    const { container } = render(
      <EditReservationDialog
        isOpen={true}
        reservation={null}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    expect(container.innerHTML).toBe('');
  });

  test('update button is disabled without selected times', () => {
    const customReservation = { ...reservation, timeSlot: '-' };

    render(
      <EditReservationDialog
        isOpen={true}
        reservation={customReservation}
        onClose={mockOnClose}
        onUpdate={mockOnUpdate}
      />
    );

    const updateButton = screen.getByRole('button', { name: /update/i });
    expect(updateButton).toBeDisabled();
  });
});
