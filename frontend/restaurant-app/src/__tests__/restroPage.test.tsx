import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, it, expect, vi, beforeEach } from "vitest";
import RestroPage from "../pages/restroPage";
import axios from "axios";
import { useLocationDetails } from "@/hooks/useLocationDetails";
import { useFeedbacks } from "@/hooks/useFeedbacks";

vi.mock("axios");
vi.mock("@/hooks/useLocationDetails", () => ({
  useLocationDetails: vi.fn(),
}));
vi.mock("@/hooks/useFeedbacks", () => ({
  useFeedbacks: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("RestroPage Component", () => {
  const mockLocationId = "1";
  const mockLocationDetails = {
    address: "123 Green Street",
    totalCapacity: 10,
    averageOccupancy: 80,
    imageUrl: "test-image-url",
  };

  const mockSpecialtyDishes = [
    { id: 1, name: "Dish 1", price: "$10", weight: "200g", imageUrl: "dish1.jpg" },
    { id: 2, name: "Dish 2", price: "$15", weight: "250g", imageUrl: "dish2.jpg" },
  ];

  const mockFeedbacks = [
    { id: 1, rating: 5, comment: "Excellent service!", type: "SERVICE" },
    { id: 2, rating: 4, comment: "Great food!", type: "CUISINE_EXPERIENCE" },
  ];

  beforeEach(() => {
    vi.resetAllMocks();
    useLocationDetails.mockReturnValue({
      location: mockLocationDetails,
      loading: false,
    });
    useFeedbacks.mockReturnValue({
      feedbacks: mockFeedbacks,
      loading: false,
    });
    axios.get.mockResolvedValue({ data: mockSpecialtyDishes });
  });

  it("renders loading state initially", () => {
    useLocationDetails.mockReturnValue({ location: null, loading: true });

    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it("renders error state when location is not found", () => {
    useLocationDetails.mockReturnValue({ location: null, loading: false });

    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/location not found/i)).toBeInTheDocument();
  });

  it("renders location details and specialty dishes", async () => {
    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/green & tasty/i)).toBeInTheDocument();
    const locationText = screen.getAllByText(/123 green street/i);
    expect(locationText).toHaveLength(3);  // Check how many times the address appears
    expect(locationText[0]).toBeInTheDocument(); 
    expect(screen.getByText(/specialty dishes/i)).toBeInTheDocument();
    await waitFor(() => {
        expect(screen.getByText(/dish 1/i)).toBeInTheDocument();
        expect(screen.getByText(/dish 2/i)).toBeInTheDocument();
      });
  });

  it("navigates to book table page on button click", () => {
    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByText(/book a table/i));
    expect(mockNavigate).toHaveBeenCalledWith("/book-table");
  });

  it("renders feedback section with sorting and filtering", async () => {
    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/customer reviews/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/cuisine experience/i));
    expect(useFeedbacks).toHaveBeenCalledWith(mockLocationId, "CUISINE_EXPERIENCE", expect.anything());

    fireEvent.change(screen.getByRole("combobox"), { target: { value: "Newest first" } });
    expect(useFeedbacks).toHaveBeenCalledWith(mockLocationId, expect.anything(), "Newest first");
  });

  it("handles pagination correctly", async () => {
    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/customer reviews/i)).toBeInTheDocument();
    });

    const pageButtons = screen.queryAllByText(/2/i);
    expect(pageButtons).toHaveLength(6);  // Ensure the pagination button for page 2 exists
    expect(screen.getByText(/excellent service/i)).toBeInTheDocument();
  });
  it("renders restaurant image", () => {
    render(
      <MemoryRouter initialEntries={[`/locations/${mockLocationId}`]}>
        <Routes>
          <Route path="/locations/:locationId" element={<RestroPage />} />
        </Routes>
      </MemoryRouter>
    );
  
    const image = screen.getByAltText(/restaurant/i);
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute("src", "test-image-url");
  });
  
});
