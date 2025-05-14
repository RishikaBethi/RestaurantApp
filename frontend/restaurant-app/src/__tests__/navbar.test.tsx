import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { BrowserRouter as Router } from "react-router-dom";
import Navbar from "../components/navbar";

const renderNavbar = (isLoggedIn: boolean, role = "", setIsLoggedIn = vi.fn()) => {
  localStorage.setItem("user", '"John Doe"' );
  localStorage.setItem("email", "john.doe@example.com");
  localStorage.setItem("role", role);
  localStorage.setItem("token", isLoggedIn ? "fake-token" : "");

  return render(
    <Router>
      <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
    </Router>
  );
};

describe("Navbar Component", () => {
  afterEach(() => {
    localStorage.clear();
    vi.resetAllMocks();
  });

  it("renders public links when user is not logged in", () => {
    renderNavbar(false);

    expect(screen.getByText("Main Page")).toBeInTheDocument();
    expect(screen.getByText("Book a Table")).toBeInTheDocument();
    expect(screen.queryByText("Reservations")).not.toBeInTheDocument();
    expect(screen.getByText("Sign In")).toBeInTheDocument();
  });

  it("renders appropriate links for a logged-in customer", () => {
    renderNavbar(true);

    expect(screen.getByText("Main Page")).toBeInTheDocument();
    expect(screen.getByText("Book a Table")).toBeInTheDocument();
    expect(screen.getByText("Reservations")).toBeInTheDocument();
    expect(screen.queryByText("Sign In")).not.toBeInTheDocument();
  });

  it("renders appropriate links for a waiter", () => {
    renderNavbar(true, "Waiter");

    expect(screen.getByText("Reservations")).toBeInTheDocument();
    expect(screen.getByText("Menu")).toBeInTheDocument();
    expect(screen.queryByText("Main Page")).not.toBeInTheDocument();
  });

  it("renders appropriate links for a Admin", () => {
    renderNavbar(true, "Admin");

    expect(screen.getByText("Reports")).toBeInTheDocument();
    expect(screen.queryByText("Main Page")).not.toBeInTheDocument();
  });

  it("toggles avatar dropdown when clicked", () => {
    renderNavbar(true);

    const avatar = screen.getByText("JD");
    fireEvent.click(avatar);

    expect(screen.getByText("john.doe@example.com")).toBeInTheDocument();

    fireEvent.click(avatar);
    expect(screen.queryByText("john.doe@example.com")).not.toBeInTheDocument();
  });

  it("signs out when 'Sign Out' is clicked", () => {
    const setIsLoggedIn = vi.fn();
    renderNavbar(true, "Customer", setIsLoggedIn);

    const avatar = screen.getByText("JD");
    fireEvent.click(avatar);

    const signOutButton = screen.getByText("Sign Out");
    fireEvent.click(signOutButton);

    expect(setIsLoggedIn).toHaveBeenCalledWith(false);
    expect(localStorage.getItem("token")).toBeNull();
  });

  it("opens and closes the mobile menu", () => {
    renderNavbar(true);

    const menuButton = screen.getByRole("button", { name: /toggle mobile menu/i });

fireEvent.click(menuButton);

// Mobile menu should be visible
expect(screen.getByTestId("mobile-menu")).toBeInTheDocument();

// Close mobile menu
fireEvent.click(menuButton);

// Mobile menu should not exist
expect(screen.queryByTestId("mobile-menu")).not.toBeInTheDocument();
  });

  it("renders correct mobile menu options based on role", () => {
    renderNavbar(true, "Waiter");

    const menuButton = screen.getByRole("button", { name: /Toggle mobile menu/i });
    fireEvent.click(menuButton);

    // First, get the mobile menu container
const mobileMenu = screen.getByRole('navigation').querySelector('.absolute.top-14.right-2');

// Ensure mobileMenu is not null
expect(mobileMenu).not.toBeNull();

// Cast mobileMenu to HTMLElement and use it
expect(within(mobileMenu as HTMLElement).getByText('Reservations')).toBeInTheDocument();
expect(within(mobileMenu as HTMLElement).getByText('Menu')).toBeInTheDocument();
expect(within(mobileMenu as HTMLElement).queryByText('Main Page')).not.toBeInTheDocument();
  });

  it("shows login button when user is not logged in", () => {
    renderNavbar(false);

    expect(screen.getByText("Sign In")).toBeInTheDocument();
    fireEvent.click(screen.getByText("Sign In"));
    // You could add more expectations here for navigation or state change
  });

  it("hides login button and shows avatar when user is logged in", () => {
    renderNavbar(true);

    expect(screen.queryByText("Sign In")).not.toBeInTheDocument();
  });
});
