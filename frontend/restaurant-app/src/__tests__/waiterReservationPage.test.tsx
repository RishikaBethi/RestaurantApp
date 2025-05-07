import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import axios from "axios";
import WaiterReservations from "../pages/waiterReservationPage";

vi.mock("axios");
vi.mock("@/components/createReservationModal", () => ({
  default: ({ open, onClose, onReservationSuccess, address }) => (
    <div data-testid="create-reservation-modal" open={open}>
      Mock Create Reservation Modal
      <button onClick={() => { onReservationSuccess(); onClose(); }}>Close</button>
    </div>
  ),
}));

const mockReservations = [
  {
    id: "1",
    date: "2025-05-01",
    timeFrom: "10:30",
    timeTo: "12:00",
    address: "123 Main St",
    name: "John Doe",
    guestsNumber: "4",
    tableNumber: "1",
  },
  {
    id: "2",
    date: "2025-05-02",
    timeFrom: "12:15",
    timeTo: "13:45",
    address: "456 Elm St",
    name: "Jane Smith",
    guestsNumber: "2",
    tableNumber: "2",
  },
];

describe("WaiterReservations", () => {
  beforeEach(() => {
    localStorage.setItem("token", "mock-token");
    localStorage.setItem("user", '"John"');
    localStorage.setItem("role", "Waiter");
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it("renders correctly and fetches reservations", async () => {
    axios.get.mockResolvedValue({ data: mockReservations });

    render(<WaiterReservations />);
    await waitFor(() => {
        expect(screen.getByText(/Hello, John \(Waiter\)/i)).toBeInTheDocument();
        expect(screen.getByText(/You have 2 reservations/i)).toBeInTheDocument();
      expect(screen.getByText("123 Main St")).toBeInTheDocument();
    });
  });

  it("filters reservations by date, time, and table", async () => {
    axios.get.mockResolvedValue({ data: mockReservations });

    render(<WaiterReservations />);
    await waitFor(() => screen.getByText("123 Main St"));

    //fireEvent.change(screen.getByText(/dd-mm-yyyy/i), { target: { value: "2025-05-01" } });
    fireEvent.change(screen.getByText(/Time/i), { target: { value: "10:30" } });
    fireEvent.change(screen.getByText("Table"), { target: { value: "Table 1" } });
    fireEvent.click(screen.getByRole("button", { name: /search/i }));

    await waitFor(() => {
      expect(screen.getByText("123 Main St")).toBeInTheDocument();
      expect(screen.queryByText("456 Elm St")).toBeInTheDocument();
    });
  });

  it("cancels a reservation", async () => {
    axios.get.mockResolvedValue({ data: mockReservations });
    axios.delete.mockResolvedValue({});

    render(<WaiterReservations />);
    await waitFor(() => screen.getByText("123 Main St"));

    fireEvent.click(screen.getAllByText(/Cancel/i)[0]);
    fireEvent.click(screen.getByText(/Yes, Cancel/i));

    await waitFor(() => {
      expect(screen.queryByText("123 Main St")).not.toBeInTheDocument();
    });
  });

  it("opens and closes the create reservation modal", async () => {
    axios.get.mockResolvedValue({ data: mockReservations });

    render(<WaiterReservations />);
    await waitFor(() => screen.getByText("123 Main St"));

    fireEvent.click(screen.getByText(/\+ Create New Reservation/i));
    expect(screen.getByTestId("create-reservation-modal")).toBeInTheDocument();

    fireEvent.click(screen.getByText(/Close/i));
    await waitFor(() => {
      expect(screen.queryByTestId("create-reservation-modal")).not.toBeInTheDocument();
    });
  });

  it("postpones a reservation", async () => {
    axios.get.mockResolvedValue({ data: mockReservations });
    axios.put.mockResolvedValue({});

    render(<WaiterReservations />);
    await waitFor(() => screen.getByText("123 Main St"));

    const postponeButtons = screen.getAllByText(/postpone/i);
    fireEvent.click(postponeButtons[0]);

    fireEvent.change(screen.getByLabelText(/Date/i), { target: { value: "2025-05-03" } });
    fireEvent.change(screen.getByLabelText(/Time Slot/i), { target: { value: "12:15 - 13:45" } });
    fireEvent.click(screen.getByText(/Save Changes/i));

  });

  it("displays a loading spinner while fetching data", () => {
    axios.get.mockReturnValue(new Promise(() => {})); // Simulate loading

    render(<WaiterReservations />);

    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });
});
