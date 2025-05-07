import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import MenuPage from "../components/viewMenu";
import { MemoryRouter } from "react-router-dom";
import { describe, it, vi, beforeEach, afterEach, expect, Mocked } from "vitest";
import axios from "axios";

// Mock external dependencies

// Partially mock react-router-dom
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>;
  return {
    ...actual,
    useNavigate: vi.fn(), // Mock only useNavigate
  };
});
  
  vi.mock("sonner", () => ({
    toast: {
      error: vi.fn(),
    },
  }));
  
  vi.mock("axios");
  
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const mockNavigate = vi.fn();
  const mockedAxios = axios as Mocked<typeof axios>;

describe("MenuPage Component", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetAllMocks();
    mockedAxios.get.mockResolvedValue({ data: { content: [] } });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should fetch and display dishes based on selected category", async () => {
    const mockData = [
      {
        id: "1",
        name: "Dish 1",
        previewImageUrl: "image1.jpg",
        price: "$10",
        weight: "200g",
        state: "Available",
      },
      {
        id: "2",
        name: "Dish 2",
        previewImageUrl: "image2.jpg",
        price: "$20",
        weight: "300g",
        state: "On Stop",
      },
    ];

    mockedAxios.get.mockResolvedValueOnce({ data: { content: mockData } });
    localStorage.setItem("token", "mock-token");

    render(
      <MemoryRouter>
        <MenuPage />
      </MemoryRouter>
    );

    await waitFor(() => expect(mockedAxios.get).toHaveBeenCalled());

    const dishes = screen.getAllByRole("img");
    expect(dishes).toHaveLength(mockData.length);
    expect(screen.getByText("Dish 1")).toBeInTheDocument();
    expect(screen.getByText("Dish 2")).toBeInTheDocument();
  });

  it("should filter dishes by category", async () => {
    const mockData = [
      {
        id: "1",
        name: "Dish 1",
        previewImageUrl: "image1.jpg",
        price: "$10",
        weight: "200g",
        state: "Available",
      },
    ];

    mockedAxios.get.mockResolvedValueOnce({ data: { content: mockData } });
    localStorage.setItem("token", "mock-token");

    render(
      <MemoryRouter>
        <MenuPage />
      </MemoryRouter>
    );

    const categoryButton = screen.getByText("Appetizers");
    fireEvent.click(categoryButton);

    await waitFor(() =>
         {expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining("dishType=APPETIZERS"),
            {
                     "headers": {
                       "Authorization": "Bearer mock-token",
                     },
                   },
                  )
         });

    expect(screen.getByText("No dishes available for Appetizers.")).toBeInTheDocument();
  });

  // it("should sort dishes by selected order", async () => {
  //   // Initial default fetch (popularity-desc)
  //   mockedAxios.get.mockResolvedValueOnce({
  //     data: {
  //       content: [], // Mocked response for default fetch
  //     },
  //   });

  //   // Fetch after selecting "Price Low to High" (price-asc)
  //   mockedAxios.get.mockResolvedValueOnce({
  //     data: {
  //       content: [], // Mocked response for sorted fetch
  //     },
  //   });

  //   render(
  //     <MemoryRouter>
  //       <MenuPage />
  //     </MemoryRouter>
  //   );

  //   // Wait for initial request
  //   await waitFor(() => {
  //     expect(mockedAxios.get).toHaveBeenCalledWith(
  //       expect.stringContaining("sortBy=popularity-desc"),
  //       {
  //                "headers": {
  //                  "Authorization": "Bearer mock-token",
  //                },
  //              },
  //     );
  //   });

  //   const sortSelect = screen.getByLabelText(/sort by/i); // Match "Sort by:" label
  //   fireEvent.change(sortSelect, { target: { value: "Price Low to High" } });

  //   // Wait for the updated request
  //   await waitFor(() => {
  //     expect(mockedAxios.get).toHaveBeenCalledWith(
  //       expect.stringContaining("sortBy=price-asc"),
  //       {
  //                "headers": {
  //                  "Authorization": "Bearer mock-token",
  //                },
  //              },
  //     );
  //   });
  // });

  it("should display a spinner while loading dishes", async () => {
    mockedAxios.get.mockImplementationOnce(() => new Promise(() => {})); // Simulate loading
    localStorage.setItem("token", "mock-token");

    render(
      <MemoryRouter>
        <MenuPage />
      </MemoryRouter>
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it("should display a fallback message when no dishes are available", async () => {
    mockedAxios.get.mockResolvedValueOnce({ data: { content: [] } });
    localStorage.setItem("token", "mock-token");

    render(
      <MemoryRouter>
        <MenuPage />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByText(/no dishes available/i)).toBeInTheDocument());
  });
});