import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter, useNavigate } from "react-router-dom";
import ReservationsPage from "../pages/reservations"; 
import { afterEach, beforeEach, describe, expect, it, vi, Mock } from "vitest";
import axios from "axios";
import { useCancelReservation } from "@/hooks/useCancelReservation";

vi.mock("axios");
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});
vi.mock("@/hooks/useCancelReservation", () => ({
  useCancelReservation: vi.fn(() => ({ cancelReservation: vi.fn() })),
}));

describe("ReservationsPage", () => {
  const mockNavigate = vi.fn();
  const mockCancelReservation = vi.fn();
  const mockReservations = [
    {
      id: 1,
      locationAddress: "123 Test St",
      date: "2025-04-30",
      timeSlot: "12:00 - 14:00",
      guestsNumber: 4,
      status: "Reserved",
    },
    {
      id: 2,
      locationAddress: "456 Example Rd",
      date: "2025-05-01",
      timeSlot: "14:00 - 16:00",
      guestsNumber: 2,
      status: "In Progress",
    },
  ];

  beforeEach(() => {
    vi.resetAllMocks();
    (useCancelReservation as Mock).mockReturnValue({
      cancelReservation: mockCancelReservation,
    });
    (axios.get as Mock).mockResolvedValue({ data: mockReservations });
    localStorage.setItem("user", JSON.stringify("TestUser"));
    localStorage.setItem("role", "Admin");
    localStorage.setItem("token", "test-token");
    (vi.mocked(useNavigate)).mockReturnValue(mockNavigate);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("redirects to login if not authenticated", () => {
    localStorage.removeItem("user");
    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("renders reservations correctly", async () => {
    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/hello, testuser \(admin\)/i)).toBeInTheDocument();
      expect(screen.getByText(/123 test st/i)).toBeInTheDocument();
      expect(screen.getByText(/456 example rd/i)).toBeInTheDocument();
    });
  });

  it("handles reservation cancelation", async () => {
    mockCancelReservation.mockResolvedValue({ data: { message: "Reservation cancelled!" } });

    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/123 test st/i)).toBeInTheDocument();
    });

    const cancelButton = screen.getAllByText(/cancel/i)[0];
    fireEvent.click(cancelButton);

    const confirmButton = screen.getByText(/yes, cancel/i);
    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(mockCancelReservation).toHaveBeenCalledWith(1);
    });
  });

  it("opens edit reservation dialog", async () => {
    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/123 test st/i)).toBeInTheDocument();
    });

    const editButton = screen.getAllByText(/edit/i)[0];
    fireEvent.click(editButton);

    const dateInput = screen.getByDisplayValue("2025-04-30"); // Match the date of the first reservation
    expect(dateInput).toBeInTheDocument();

    const timeSelect = screen.getByText("12:00 - 14:00"); // Match the "From" time of the reservation
    expect(timeSelect).toBeInTheDocument();

  });

  it("opens feedback modal for In Progress reservations", async () => {
    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/456 example rd/i)).toBeInTheDocument();
    });

    const feedbackButton = screen.getByText(/leave feedback/i);
    fireEvent.click(feedbackButton);

    await waitFor(() => {
      expect(screen.getByText(/submit feedback/i)).toBeInTheDocument();
    });
  });

  it("shows shimmer UI while loading", () => {
    (axios.get as Mock).mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    const shimmerContainers = document.querySelectorAll(".animate-pulse .bg-gray-300");
    expect(shimmerContainers.length).toBeGreaterThan(0);
  });

  it("handles API fetch failure gracefully", async () => {
    (axios.get as Mock).mockRejectedValue(new Error("Failed to fetch reservations"));

    render(
      <MemoryRouter>
        <ReservationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.queryByText(/123 test st/i)).not.toBeInTheDocument();
    });
  });
});
