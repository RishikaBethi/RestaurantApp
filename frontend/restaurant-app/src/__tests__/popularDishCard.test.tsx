import { render, screen } from "@testing-library/react";
import PopularDishCard from "../components/popularDishCard";
import { describe, expect, it } from "vitest";

describe("PopularDishCard", () => {
  const props = {
    imageUrl: "https://example.com/dish.jpg",
    name: "Pasta Primavera",
    price: "$12.99",
    weight: "350g",
  };

  it("renders image with correct src and alt attributes", () => {
    render(<PopularDishCard {...props} />);
    const image = screen.getByRole("img");

    expect(image).toHaveAttribute("src", props.imageUrl);
    expect(image).toHaveAttribute("alt", props.name);
  });

  it("displays the dish name", () => {
    render(<PopularDishCard {...props} />);
    expect(screen.getByText(props.name)).toBeInTheDocument();
  });

  it("displays the price", () => {
    render(<PopularDishCard {...props} />);
    expect(screen.getByText(props.price)).toBeInTheDocument();
  });

  it("displays the weight", () => {
    render(<PopularDishCard {...props} />);
    expect(screen.getByText(props.weight)).toBeInTheDocument();
  });
});
