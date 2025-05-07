import { render, screen } from "@testing-library/react";
import LocationCard from "../components/locationCard";
import { describe, expect, it } from "vitest";

describe("LocationCard", () => {
  const mockProps = {
    image: "https://example.com/image.jpg",
    address: "123 Main St, Springfield",
    totalCapacity: 20,
    averageOccupancy: 75,
  };

  it("renders the image with correct src and alt text", () => {
    render(<LocationCard {...mockProps} />);
    const image = screen.getByRole("img");

    expect(image).toHaveAttribute("src", mockProps.image);
    expect(image).toHaveAttribute("alt", mockProps.address);
  });

  it("displays the address with location icon", () => {
    render(<LocationCard {...mockProps} />);
    expect(screen.getByText(mockProps.address)).toBeInTheDocument();
  });

  it("displays total capacity correctly", () => {
    render(<LocationCard {...mockProps} />);
    expect(screen.getByText(`Total capacity: ${mockProps.totalCapacity} tables`)).toBeInTheDocument();
  });

  it("displays average occupancy correctly", () => {
    render(<LocationCard {...mockProps} />);
    expect(screen.getByText(`Average occupancy: ${mockProps.averageOccupancy}%`)).toBeInTheDocument();
  });
});
