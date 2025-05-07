import { describe, it, expect } from "vitest";
import { render } from "@testing-library/react";
import ShimmerFeedback from "@/components/shimmerUI/shimmerFeedback";

describe("ShimmerFeedback", () => {
  it("renders without crashing", () => {
    const { container } = render(<ShimmerFeedback />);
    expect(container).toBeDefined();
  });

  it("contains the animate-pulse class", () => {
    const { container } = render(<ShimmerFeedback />);
    const rootDiv = container.querySelector("div");
    expect(rootDiv?.className).toContain("animate-pulse");
  });

  it("renders avatar shimmer block", () => {
    const { container } = render(<ShimmerFeedback />);
    const avatar = container.querySelector(".w-10.h-10.rounded-full.bg-gray-200");
    expect(avatar).toBeTruthy();
  });

  it("renders multiple gray shimmer lines", () => {
    const { container } = render(<ShimmerFeedback />);
    const shimmerLines = container.querySelectorAll(".bg-gray-200");
    // 6 shimmer blocks: avatar + 2 name lines + 3 comment lines
    expect(shimmerLines.length).toBe(6);
  });

  it("has the expected layout classes", () => {
    const { container } = render(<ShimmerFeedback />);
    const wrapper = container.firstChild as HTMLElement;
    expect(wrapper.className).toContain("rounded-2xl");
    expect(wrapper.className).toContain("shadow-sm");
    expect(wrapper.className).toContain("p-4");
    expect(wrapper.className).toContain("space-y-4");
    expect(wrapper.className).toContain("bg-white");
  });
});
