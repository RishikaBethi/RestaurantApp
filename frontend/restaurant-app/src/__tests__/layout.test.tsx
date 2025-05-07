import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Layout from "../components/layout";

// Mock Navbar
vi.mock("../navbar", () => ({
  default: ({ isLoggedIn }: { isLoggedIn: boolean }) => (
    <div data-testid="navbar">{isLoggedIn ? "Logged In" : "Logged Out"}</div>
  ),
}));

// Mock Outlet to simulate child route content
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    Outlet: () => <div data-testid="outlet">Mocked Outlet Content</div>,
  };
});

describe("Layout Component", () => {
  it("renders Navbar and Outlet correctly when logged in", () => {
    const setIsLoggedIn = vi.fn();
    render(
      <MemoryRouter>
        <Layout isLoggedIn={true} setIsLoggedIn={setIsLoggedIn} />
      </MemoryRouter>
    );

    expect(screen.queryByText("Sign In")).not.toBeInTheDocument();
    expect(screen.getByTestId("outlet")).toHaveTextContent("Mocked Outlet Content");
  });

  it("renders Navbar correctly when logged out", () => {
    const setIsLoggedIn = vi.fn();
    render(
      <MemoryRouter>
        <Layout isLoggedIn={false} setIsLoggedIn={setIsLoggedIn} />
      </MemoryRouter>
    );

    expect(screen.getByText("Sign In")).toBeInTheDocument()
    expect(screen.getByTestId("outlet")).toBeInTheDocument();
  });
});
