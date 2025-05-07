import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ConfirmationModal from "../components/confirmationModal";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { toast } from "sonner";
import { useCancelReservation } from "@/hooks/useCancelReservation";

vi.mock("sonner", () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock("@/hooks/useCancelReservation", () => ({
  useCancelReservation: vi.fn(() => ({
    cancelReservation: vi.fn(),
    loading: false,
  })),
}));

describe("ConfirmationModal Component", () => {
  const mockOnClose = vi.fn();
  const mockOnReservationCancel = vi.fn();
  const mockCancelReservation = vi.fn();
  const mockBookingData = {
    date: "2025-04-30",
    timeSlot: "10:00 AM - 12:00 PM",
    guestsNumber: "4",
    locationAddress: "123 Green St",
    status: "Confirmed",
    id: 1,
  };

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCancelReservation).mockReturnValue({
        cancelReservation: mockCancelReservation,
        loading: false,
        error: null
    });
  });

  it("should not render when bookingData is null", () => {
    render(<ConfirmationModal isOpen={true} onClose={mockOnClose} bookingData={null} />);
    expect(screen.queryByText("Reservation Confirmed!")).not.toBeInTheDocument();
  });

  it("should render correctly with booking data", () => {
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        bookingData={mockBookingData}
      />
    );

    expect(screen.getByText("Reservation Confirmed!")).toBeInTheDocument();
    expect(screen.getByText(/Your table reservation at/i)).toBeInTheDocument();
    expect(screen.getByText("Green & Tasty")).toBeInTheDocument();
    expect(screen.getByText("4 people")).toBeInTheDocument();
    expect(screen.getByText("2025-04-30")).toBeInTheDocument();
    expect(screen.getByText("10:00 AM - 12:00 PM")).toBeInTheDocument();
    expect(screen.getByText("123 Green St")).toBeInTheDocument();
    expect(screen.getByText("Confirmed")).toBeInTheDocument();
  });

  it("should call cancelReservation and close modal on successful cancel", async () => {
    mockCancelReservation.mockResolvedValueOnce(null);
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        onReservationCancel={mockOnReservationCancel}
        bookingData={mockBookingData}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Cancel Reservation/i }));

    expect(mockCancelReservation).toHaveBeenCalledWith(1);
    await waitFor(() => {
      expect(mockOnReservationCancel).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("should show error toast on cancel failure", async () => {
    mockCancelReservation.mockRejectedValueOnce(new Error("Cancel failed"));
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        bookingData={mockBookingData}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Cancel Reservation/i }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Failed to cancel the reservation.");
    });
  });

  it("should open EditReservationDialog on Edit button click", () => {
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        bookingData={mockBookingData}
      />
    );
  
    // Find and click the "Edit Reservation" button
    const editButton = screen.getByRole("button", { name: /Edit Reservation/i });
    fireEvent.click(editButton);
  
    // Assert that the EditReservationDialog content is displayed
    expect(
      screen.getByRole("heading", { name: /Edit Reservation/i })
    ).toBeInTheDocument();
  });  

  it("should close EditReservationDialog on close", () => {
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        bookingData={mockBookingData}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Edit Reservation/i }));

    const closeEditButton = screen.getByRole("button", { name: /Close/i });
    fireEvent.click(closeEditButton);
  });

  it("should show success toast on successful reservation update", async () => {
    render(
      <ConfirmationModal
        isOpen={true}
        onClose={mockOnClose}
        bookingData={mockBookingData}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Edit Reservation/i }));
    const updateButton = screen.getByRole("button", { name: /Update/i });

    fireEvent.click(updateButton);
  });
});
