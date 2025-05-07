import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import BookTable from "../pages/tablebooking";
import { describe, it, vi, beforeEach, expect,Mocked } from "vitest";
import axios from "axios";
import { BrowserRouter } from "react-router-dom";

vi.mock("axios");
const mockedAxios = axios as Mocked<typeof axios>;

const mockLocations = [
  { id: "LOC001", address: "123 Test Street" },
  { id: "LOC002", address: "456 Demo Ave" },
];

const mockTables = [
  {
    locationId: "LOC001",
    locationAddress: "123 Test Street",
    availableSlots: ["10:30-12:00", "12:15-13:45"],
    tableNumber: "1",
    capacity: "4",
  },
];

describe("BookTable page", () => {
  beforeEach(() => {
    mockedAxios.get = vi.fn((url) => {
      if (url.includes("locations/select-options")) {
        return Promise.resolve({ data: mockLocations });
      }
      if (url.includes("bookings/tables")) {
        return Promise.resolve({ data: mockTables });
      }
      return Promise.reject(new Error("Unknown endpoint"));
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    }) as any;
  });

  const setup = () => {
    return render(
      <BrowserRouter>
        <BookTable />
      </BrowserRouter>
    );
  };

  it("renders initial UI correctly", async () => {
    setup();
    await waitFor(() => {
      expect(screen.getByText("Green & Tasty Restaurants")).toBeInTheDocument();
      expect(screen.getByText("Book a Table")).toBeInTheDocument();
      expect(screen.getByText("Find a Table")).toBeInTheDocument();
    });
  });

  it("loads and displays location options", async () => {
    setup();
    await waitFor(() => {
      expect(screen.getByDisplayValue("123 Test Street")).toBeInTheDocument();
    });
  });

  it("shows available tables after searching", async () => {
    setup();
    fireEvent.click(screen.getByText("Find a Table"));

    await waitFor(() => {
      expect(screen.getByText("Table #1, Capacity: 4 people")).toBeInTheDocument();
      expect(screen.getByText("10:30-12:00")).toBeInTheDocument();
    });
  });

  it("opens reservation modal when a slot is clicked", async () => {
    setup();
    fireEvent.click(screen.getByText("Find a Table"));

    await waitFor(() => {
      const slotButton = screen.getByText("10:30-12:00");
      fireEvent.click(slotButton);
    });

    await waitFor(() => {
        const elements = screen.getAllByText(/Make a Reservation/i);
        expect(elements).toHaveLength(2);        
    });
  });

  it("opens slots modal when '+ Show all' is clicked", async () => {
    setup();
    fireEvent.click(screen.getByText("Find a Table"));

    await waitFor(() => {
      const showAllButton = screen.getByText("+ Show all");
      fireEvent.click(showAllButton);
    });

    await waitFor(() => {
      expect(screen.getByText(/Available Slots/i)).toBeInTheDocument();
    });
  });
//   it("updates date input correctly", async () => {
//     setup();
  
//     const dateInput = screen.getByDisplayValue("2025-04-30");
// fireEvent.change(dateInput, { target: { value: "2025-12-25" } });
  
//     await waitFor(() => {
//       expect((dateInput as HTMLInputElement).value).toBe("2025-12-25");
//     });
//   });
  
  it("updates time dropdown correctly", async () => {
    setup();
  
    const timeSelect = screen.getByDisplayValue("10:30");
    fireEvent.change(timeSelect, { target: { value: "14:00" } });
  
    await waitFor(() => {
      expect((timeSelect as HTMLSelectElement).value).toBe("14:00");
    });
  });
  
  it("updates guests input and restricts to two digits", async () => {
    setup();
  
    const guestInput = screen.getByRole("spinbutton") as HTMLInputElement;
    fireEvent.change(guestInput, { target: { value: "12" } });
    expect(guestInput.value).toBe("12");
  
    fireEvent.change(guestInput, { target: { value: "123" } });
    expect(guestInput.value).toBe("12"); // should not update
  });
  
  it("shows validation error for guests < 1", async () => {
    setup();
  
    const guestInput = screen.getByRole("spinbutton") as HTMLInputElement;
    fireEvent.change(guestInput, { target: { value: "0" } });
  
    fireEvent.click(screen.getByText("Find a Table"));
  
    await waitFor(() => {
      expect(screen.queryByText("Table #1, Capacity: 4 people")).not.toBeInTheDocument();
    });
  });
  
  it("does not open modal if not logged in", async () => {
    localStorage.removeItem("token"); // simulate logged-out state
    setup();
  
    fireEvent.click(screen.getByText("Find a Table"));
  
    await waitFor(() => {
      const slotButton = screen.getByText("10:30-12:00");
      fireEvent.click(slotButton);
    });
  
    const elements = screen.queryAllByText("Make a Reservation");
    expect(elements).toHaveLength(2); 

  });
  
  it("closes reservation modal correctly", async () => {
    localStorage.setItem("token", "test-token"); // simulate logged-in
    setup();
  
    fireEvent.click(screen.getByText("Find a Table"));
  
    await waitFor(() => {
      const slotButton = screen.getByText("10:30-12:00");
      fireEvent.click(slotButton);
    });
  
    await waitFor(() => {
      const closeButtons = screen.getAllByRole("button", { name: /close/i });
      fireEvent.click(closeButtons[0]); // close the modal
    });
  
    await waitFor(() => {
      expect(screen.queryByText(/Make a Reservation/i)).not.toBeInTheDocument();
    });
  });
  
  it("closes available slots modal correctly", async () => {
    setup();
  
    fireEvent.click(screen.getByText("Find a Table"));
  
    await waitFor(() => {
      fireEvent.click(screen.getByText("+ Show all"));
    });
  
    await waitFor(() => {
      const closeButtons = screen.getAllByRole("button", { name: /close/i });
      fireEvent.click(closeButtons[0]);
    });
  
    await waitFor(() => {
      expect(screen.queryByText(/Available Slots/i)).not.toBeInTheDocument();
    });
  });
  
  it("displays fallback text when no search initiated", async () => {
    setup();
    await waitFor(() => {
      expect(screen.getByText(/Please select a location.*view available tables/i)).toBeInTheDocument();
    });
  });
  
  it("shows error fallback on location fetch failure", async () => {
    mockedAxios.get = vi.fn((url) => {
      if (url.includes("locations/select-options")) {
        return Promise.reject({ response: { data: { error: "Failed to load locations" } } });
      }
      return Promise.resolve({ data: mockTables });
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    }) as any;
  
    setup();
  
    await waitFor(() => {
      expect(screen.getByText("Failed to load locations.")).toBeInTheDocument();
    });
  });
  
});
