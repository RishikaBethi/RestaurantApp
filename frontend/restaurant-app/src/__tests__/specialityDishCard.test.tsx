import { render, screen } from "@testing-library/react";
import SpecialtyDishCard from "../components/specialtyDishCard";
import { describe, expect, it } from "vitest";

describe("SpecialtyDishCard", () => {
  const props = {
    imageUrl: "https://example.com/specialty-dish.jpg",
    name: "Grilled Salmon",
    price: "$18.50",
    weight: "300g",
  };

  it("renders the image with correct src and alt attributes", () => {
    render(<SpecialtyDishCard {...props} />);
    const image = screen.getByRole("img");

    expect(image).toHaveAttribute("src", props.imageUrl);
    expect(image).toHaveAttribute("alt", props.name);
  });

  it("displays the dish name", () => {
    render(<SpecialtyDishCard {...props} />);
    expect(screen.getByText(props.name)).toBeInTheDocument();
  });

  it("displays the price", () => {
    render(<SpecialtyDishCard {...props} />);
    expect(screen.getByText(props.price)).toBeInTheDocument();
  });

  it("displays the weight", () => {
    render(<SpecialtyDishCard {...props} />);
    expect(screen.getByText(props.weight)).toBeInTheDocument();
  });
});
