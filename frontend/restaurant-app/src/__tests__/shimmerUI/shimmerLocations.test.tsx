import { describe, it, expect } from "vitest";
import { render } from "@testing-library/react";
import ShimmerLocations from "@/components/shimmerUI/shimmerLocations";

describe("ShimmerLocations", () => {
  it("renders without crashing", () => {
    const { container } = render(<ShimmerLocations />);
    expect(container).toBeDefined();
  });

  it("renders exactly 3 shimmer cards", () => {
    const { container } = render(<ShimmerLocations />);
    const cards = container.querySelectorAll(".animate-pulse");
    expect(cards.length).toBe(3);
  });

  it("each card contains image and text placeholders", () => {
    const { container } = render(<ShimmerLocations />);
    const cards = container.querySelectorAll(".animate-pulse");

    cards.forEach((card) => {
      const divs = card.querySelectorAll("div");

      const hasImagePlaceholder = Array.from(divs).some((el) =>
        el.className.includes("h-32") && el.className.includes("bg-gray-300")
      );
      const hasTextLine1 = Array.from(divs).some((el) =>
        el.className.includes("w-3/4") && el.className.includes("h-4")
      );
      const hasTextLine2 = Array.from(divs).some((el) =>
        el.className.includes("w-1/2") && el.className.includes("h-4")
      );

      expect(hasImagePlaceholder).toBe(true);
      expect(hasTextLine1).toBe(true);
      expect(hasTextLine2).toBe(true);
    });
  });

  it("applies correct layout classes to root container", () => {
    const { container } = render(<ShimmerLocations />);
    const root = container.firstChild as HTMLElement;

    expect(root.className).toContain("grid");
    expect(root.className).toContain("grid-cols-3");
    expect(root.className).toContain("gap-4");
    expect(root.className).toContain("mt-4");
  });
});
