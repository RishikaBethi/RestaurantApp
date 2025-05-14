import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import App from "@/App";

// Mock pages and components
vi.mock("@/components/layout", () => ({
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  default: ({ children }: any) => <div data-testid="layout">{children}</div>,
}));
vi.mock("@/pages/homePage", () => ({ default: () => <div>Home Page</div> }));
vi.mock("@/pages/loginPage", () => ({
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  default: ({ setIsLoggedIn }: any) => (
    <div>
      Login Page
      <button onClick={() => setIsLoggedIn(true)}>Login</button>
    </div>
  ),
}));
vi.mock("@/pages/registerPage", () => ({ default: () => <div>Register Page</div> }));
vi.mock("@/pages/profile", () => ({ default: () => <div>Profile Page</div> }));
vi.mock("@/pages/restroPage", () => ({ default: () => <div>Restaurant Page</div> }));
vi.mock("@/pages/reservations", () => ({ default: () => <div>Reservations Page</div> }));
vi.mock("@/pages/tablebooking", () => ({ default: () => <div>Book Table Page</div> }));
vi.mock("@/pages/waiterReservationPage", () => ({ default: () => <div>Waiter Reservations Page</div> }));
vi.mock("@/components/viewMenu", () => ({ default: () => <div>Menu Page</div> }));
vi.mock("@/pages/adminReports", () => ({ default: () => <div>Admin Reports</div> }));

describe("App routing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  const renderWithRoute = (route: string, isLoggedIn = false) => {
    window.history.pushState({}, "Test page", route);
    render(<App initialIsLoggedIn={isLoggedIn} />);
  };  

  it("renders Register page on /register route", () => {
    renderWithRoute("/register");
    expect(screen.getByText("Register Page")).toBeInTheDocument();
  });

  it("renders Login page on /login route", () => {
    renderWithRoute("/login");
    expect(screen.getByText("Login Page")).toBeInTheDocument();
  });

  it("can update login state via login page", () => {
    renderWithRoute("/login");
    const loginButton = screen.getByText("Login");
    expect(loginButton).toBeInTheDocument();
  });

  it("renders Home page inside Layout on / route", () => {
    renderWithRoute("/",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Profile page", () => {
    renderWithRoute("/profile",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Restaurant page with dynamic route", () => {
    renderWithRoute("/restaurant/abc123",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Reservations page", () => {
    renderWithRoute("/reservations",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Book Table page", () => {
    renderWithRoute("/book-table",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Waiter Reservations page", () => {
    renderWithRoute("/waiter-reservations",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Menu page", () => {
    renderWithRoute("/menu",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("renders Reports page", () => {
    renderWithRoute("/reports",true);
    expect(screen.getByTestId("layout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });
});
