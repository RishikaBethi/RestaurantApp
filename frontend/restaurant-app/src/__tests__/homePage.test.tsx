import { describe, it, expect, beforeEach, afterEach, vi, Mock } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import axios from "axios";
import Home from "../pages/homePage";

// Mock axios
vi.mock("axios");
const mockedAxios = axios as unknown as {
  get: Mock;
};

vi.mock("@/components/popularDishCard", () => ({
  default: (props: { name: string; price: string; weight: string; imageUrl: string }) => (
    <div data-testid="dish-name">{props.name}</div>
  ),
}));

vi.mock("@/components/locationCard", () => ({
  default: (props: { image: string; address: string; totalCapacity: number; averageOccupancy: number }) => (
    <div data-testid="location-address">{props.address}</div>
  ),
}));

vi.mock("@/components/shimmerUI/shimmerDishes", () => ({
  default: () => <div data-testid="shimmer-dishes">Loading dishes...</div>,
}));
vi.mock("@/components/shimmerUI/shimmerLocations", () => ({
  default: () => <div data-testid="shimmer-locations">Loading locations...</div>,
}));

describe("Home Component", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  const popularDishesMock = [
    { id: "1", name: "Dish 1", price: "10", weight: "200g", imageUrl: "/dish1.png" },
    { id: "2", name: "Dish 2", price: "12", weight: "250g", imageUrl: "/dish2.png" },
  ];
  
  const locationsMock = [
    {
      id: "1",
      address: "Location 1",
      description: "",
      totalCapacity: "50",
      averageOccupancy: "20",
      imageUrl: "",
      rating: "4.5",
    },
    {
      id: "2",
      address: "Location 2",
      description: "",
      totalCapacity: "75",
      averageOccupancy: "35",
      imageUrl: "",
      rating: "4.2",
    },
  ];  

  it("renders loading shimmer while fetching data", async () => {
    mockedAxios.get.mockResolvedValueOnce({ data: [] }); // Mock empty array response
    mockedAxios.get.mockResolvedValueOnce({ data: [] }); // Mock empty array response

    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    expect(screen.getByTestId("shimmer-dishes")).toBeInTheDocument();
    expect(screen.getByTestId("shimmer-locations")).toBeInTheDocument();

    await waitFor(() => expect(mockedAxios.get).toHaveBeenCalledTimes(2)); // Wait for API calls to finish
  });

  it("renders popular dishes when data is fetched successfully", async () => {

    mockedAxios.get
      .mockResolvedValueOnce({ data: popularDishesMock })
      .mockResolvedValueOnce({ data: locationsMock });

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );

    await waitFor(() => expect(screen.queryByTestId("shimmer-dishes")).not.toBeInTheDocument());
    const dishElements = screen.queryAllByTestId("dish-name");
    expect(dishElements).toHaveLength(popularDishesMock.length);
  });

  it("renders locations when data is fetched successfully", async () => {

    mockedAxios.get
      .mockResolvedValueOnce({ data: popularDishesMock })
      .mockResolvedValueOnce({ data: locationsMock });

    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.queryByTestId("shimmer-locations")).not.toBeInTheDocument());
    const locationElements = screen.queryAllByTestId("location-address");
    expect(locationElements).toHaveLength(popularDishesMock.length);
  });

  it("displays error message when locations fetch fails", async () => {
    const errorMessage = "Failed to load locations!";
    mockedAxios.get
      .mockRejectedValueOnce(new Error("Failed to load locations")) // Mock failure for locations
      .mockResolvedValueOnce({ data: popularDishesMock }); // Mock success for popular dishes

    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByText(errorMessage)).toBeInTheDocument());
  });

  it("displays empty state when no popular dishes are present", async () => {

    mockedAxios.get
      .mockResolvedValueOnce({ data: [] }) // No popular dishes
      .mockResolvedValueOnce({ data: locationsMock });

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );

    expect(screen.getByText("Loading dishes...")).toBeInTheDocument();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });
});