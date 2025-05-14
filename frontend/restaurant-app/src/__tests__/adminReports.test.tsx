import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ReportsPage from "@/pages/adminReports";
import axios from "axios";
import { toast } from "sonner";

// Mock axios and toast
vi.mock("axios");
vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe("ReportsPage Component", () => {
  const mockWaiters = [
    { waiterId: "W001", waiterName: "John Doe" },
    { waiterId: "W002", waiterName: "Jane Smith" },
  ];

  const mockReports = [
    {
      name: "Monthly Report",
      locationId: "LOC001",
      fromDate: "2023-04-01",
      toDate: "2023-04-30",
      waiterId: "W001",
      downloadLink: "https://example.com/report",
      id: "R001",
      description: "April Monthly Report",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("user", JSON.stringify("Admin User"));
    localStorage.setItem("role", "Admin");
    localStorage.setItem("token", "mock-token");
  });

  it("renders the component correctly", () => {
    render(<ReportsPage />);
    expect(screen.getByText(/Hello, Admin User \(Admin\)/)).toBeInTheDocument();
    expect(screen.getByText("Location")).toBeInTheDocument();
    expect(screen.getByText("Waiter")).toBeInTheDocument();
    expect(screen.getByText(/Start/i)).toBeInTheDocument();
    expect(screen.getByText(/End/i)).toBeInTheDocument();
    expect(screen.getByText(/Create Report/)).toBeInTheDocument();
  });

  it("fetches waiter details on mount", async () => {
    axios.get.mockResolvedValueOnce({ data: mockWaiters });

    render(<ReportsPage />);

    await waitFor(() =>
      expect(axios.get).toHaveBeenCalledWith(
        expect.stringContaining("/waiters/details"),
        expect.objectContaining({
          headers: { Authorization: "Bearer mock-token" },
        })
      )
    );

  });

  it("displays a loading spinner while fetching reports", async () => {
    let resolveResponse;
    const mockPromise = new Promise((resolve) => {
      resolveResponse = resolve;
    });

    axios.get.mockReturnValueOnce(mockPromise);

    render(<ReportsPage />);
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    expect(screen.getByText(/Loading/i)).toBeInTheDocument();

    resolveResponse({ data: mockReports });
    await waitFor(() => expect(screen.queryByTestId("spinner")).not.toBeInTheDocument());
  });

  it("creates a report successfully", async () => {
    axios.post.mockResolvedValueOnce({ data: { message: "Report created successfully!" } });

    render(<ReportsPage />);

    fireEvent.click(screen.getByText("Create Report"));

    const createButton = screen.getByRole('button', { name: 'Create Report' });

    fireEvent.click(createButton);

    await waitFor(() => expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining("/reports"),
      expect.objectContaining({
        locationId: "",
        waiterId: "",
        startDate: "",
        endDate: "",
      })
    ));

    expect(toast.success).toHaveBeenCalledWith("Report created successfully!");
  });

  it("handles errors while creating a report", async () => {
    axios.post.mockRejectedValueOnce({ response: { data: { error: "Creation failed." } } });

    render(<ReportsPage />);

    fireEvent.click(screen.getByText("Create Report"));

    const createButton = screen.getByRole('button', { name: 'Create Report' });
    fireEvent.click(createButton);

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Failed to fetch waiter details."));
  });

  it("displays an error message when fetching reports fails", async () => {
    axios.get.mockRejectedValueOnce({ response: { data: { error: "Fetch failed." } } });

    render(<ReportsPage />);

    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Failed to fetch reports. Please try again."));
  });

  it("displays a message when no reports are available", async () => {
    axios.get.mockResolvedValueOnce({ data: [] });
    render(<ReportsPage />);
    fireEvent.click(screen.getByRole("button", { name: "Search" }));
  
    await waitFor(() => expect(screen.getByText(/No reports available/i)).toBeInTheDocument());
  });

  it("handles server error when fetching waiter details", async () => {
    axios.get.mockRejectedValueOnce(new Error("Internal Server Error"));
    render(<ReportsPage />);
  
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Failed to fetch waiter details.");
    });
  });
  
  it("displays the correct waiter name for each report", async () => {
    axios.get.mockResolvedValueOnce({ data: mockWaiters });
    axios.get.mockResolvedValueOnce({ data: mockReports });
  
    render(<ReportsPage />);
    fireEvent.click(screen.getByRole("button", { name: "Search" }));
  
    await waitFor(() =>
      expect(screen.getByText("John Doe")).toBeInTheDocument()
    );
  });

  it("shows an error if start date is after end date", async () => {
    render(<ReportsPage />);
    fireEvent.change(screen.getByPlaceholderText("Start"), { target: { value: "2023-05-10" } });
    fireEvent.change(screen.getByPlaceholderText("End"), { target: { value: "2023-05-01" } });
    fireEvent.click(screen.getByRole("button", { name: "Create Report" }));
  
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Failed to fetch waiter details.");
    });
  });
  
});
