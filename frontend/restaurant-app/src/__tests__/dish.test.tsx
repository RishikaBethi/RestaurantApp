import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { Dish } from "../components/dish";
import axios from "axios";
import { describe, expect, it, vi } from "vitest";

// Mock the axios request
vi.mock("axios");

// Mock localStorage.getItem
global.localStorage.getItem = vi.fn(() => "fake_token");

describe("Dish Component", () => {
  const mockDishId = "12345";
  const mockTrigger = <button>Show Dish</button>;

  it("renders without crashing", () => {
    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    expect(screen.getByText("Show Dish")).toBeInTheDocument();
  });

  it("shows loading spinner when data is being fetched", async () => {
    // Mock axios to simulate loading state
    axios.get.mockResolvedValueOnce({ data: { content: {} } });

    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    // Ensure the loading spinner is visible
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("displays dish data after successful fetch", async () => {
    const mockData = {
      name: "Pizza Margherita",
      imageUrl: "https://example.com/pizza.jpg",
      description: "A delicious cheese pizza",
      calories: "250",
      proteins: "10g",
      fats: "10g",
      carbohydrates: "30g",
      vitamins: "Vitamin C, Vitamin A",
      price: "$10",
      weight: "300g",
    };

    axios.get.mockResolvedValueOnce({ data: { content: mockData } });

    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    // Wait for the data to load
    await waitFor(() => expect(screen.getByText(mockData.name)).toBeInTheDocument());

    expect(screen.getByText(mockData.name)).toBeInTheDocument();
    expect(screen.getByText(mockData.description)).toBeInTheDocument();
    expect(screen.getByText(mockData.calories)).toBeInTheDocument();
    expect(screen.getByText(mockData.proteins)).toBeInTheDocument();
    expect(screen.getByText(mockData.price)).toBeInTheDocument();
  });

  it("shows error message if data fetch fails", async () => {
    axios.get.mockRejectedValueOnce(new Error("Failed to fetch"));

    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    await waitFor(() => expect(screen.getByText("Something went wrong. Could not load dish info.")).toBeInTheDocument());
  });

  it("opens the dialog when trigger is clicked", async () => {
    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    await waitFor(() => expect(screen.getByText("Something went wrong. Could not load dish info.")).toBeInTheDocument());
  });

  it("closes the dialog when 'close' is clicked", async () => {
    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    // Assuming there is a close button or the dialog can be closed programmatically
    const closeButton = screen.getByText("Close");
    fireEvent.click(closeButton);

    await waitFor(() => expect(screen.queryByText("Pizza Margherita")).not.toBeInTheDocument());
  });

  it("shows a spinner while loading", async () => {
    axios.get.mockResolvedValueOnce({ data: { content: {} } });

    render(<Dish dishId={mockDishId} trigger={mockTrigger} />);
    fireEvent.click(screen.getByText("Show Dish"));

    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });
});
