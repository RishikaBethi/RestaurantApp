import { render, screen, fireEvent, waitFor} from "@testing-library/react";
import { describe, it, vi, beforeEach, expect } from "vitest";
import CreateReservationModal from "../components/createReservationModal";
import { BASE_API_URL } from "@/constants/constant";
import axios from "axios";
import { toast } from "sonner";

vi.mock("axios");
vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));

const mockProps = {
  open: true,
  onClose: vi.fn(),
  onReservationSuccess: vi.fn(),
  address: "48 Rustaveli Avenue",
};

describe("CreateReservationModal", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders modal with initial UI elements", () => {
    render(<CreateReservationModal {...mockProps} />);

    expect(screen.getByText(/new reservation/i)).toBeInTheDocument();
    expect(screen.getByText(/date/i)).toBeInTheDocument();
    expect(screen.getByText(/visitor/i)).toBeInTheDocument();
    expect(screen.getByText(/existing customer/i)).toBeInTheDocument();
    expect(screen.getByText(/guests/i)).toBeInTheDocument();
    const tableElements = screen.getAllByText(/table/i);
    expect(tableElements).toHaveLength(2); 
    expect(screen.getByText(/make a reservation/i)).toBeInTheDocument();
  });

  it("handles guest increment and decrement buttons", () => {
    render(<CreateReservationModal {...mockProps} />);

    const decrementButton = screen.getByRole("button", { name: "-" });
    const incrementButton = screen.getByRole("button", { name: "+" });
    const guestCount = screen.getByText("1");

    fireEvent.click(incrementButton);
    expect(guestCount.textContent).toBe("2");

    fireEvent.click(decrementButton);
    expect(guestCount.textContent).toBe("1");
  });

  it("updates date when a valid date is selected", () => {
    render(<CreateReservationModal {...mockProps} />);

    const dateInput = document.querySelector('input[type="date"]');
    fireEvent.change(dateInput, { target: { value: '2025-05-01' } });

    expect(dateInput).toHaveValue("2025-05-01");
  });

  it("selects a customer type and updates the UI", () => {
    render(<CreateReservationModal {...mockProps} />);

    const existingCustomerOption = screen.getByLabelText(/existing customer/i);
    fireEvent.click(existingCustomerOption);

    expect(screen.getByPlaceholderText(/e\.g\. customer@example\.com/i)).toBeInTheDocument();
  });

  it("displays an error when submitting without selecting a date", async () => {
    render(<CreateReservationModal {...mockProps} />);

    const submitButton = screen.getByText(/make a reservation/i);
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Please select a date.");
    });
  });

  it("makes a successful reservation request", async () => {
    const mockedResponse = { data: { success: true } };
    axios.post.mockResolvedValueOnce(mockedResponse);

    render(<CreateReservationModal {...mockProps} />);

    const dateInput = document.querySelector('input[type="date"]');
    fireEvent.change(dateInput, { target: { value: '2025-05-01' } });
    fireEvent.click(screen.getByRole("button", { name: /make a reservation/i }));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        `${BASE_API_URL}/bookings/waiter`,
        expect.any(Object),
        expect.objectContaining({ headers: expect.any(Object) })
      );
      expect(toast.success).toHaveBeenCalledWith("Reservation made successfully!");
      expect(mockProps.onReservationSuccess).toHaveBeenCalled();
      expect(mockProps.onClose).toHaveBeenCalled();
    });
  });

  it("handles reservation API errors gracefully", async () => {
    const mockedError = { response: { data: { message: "Something went wrong." } } };
    axios.post.mockRejectedValueOnce(mockedError);

    render(<CreateReservationModal {...mockProps} />);

    const dateInput = document.querySelector('input[type="date"]');
    fireEvent.change(dateInput, { target: { value: '2025-05-01' } });

    fireEvent.click(screen.getByRole("button", { name: /make a reservation/i }));
    expect(screen.getByText(/make a reservation/i)).toBeInTheDocument();
  });
});
