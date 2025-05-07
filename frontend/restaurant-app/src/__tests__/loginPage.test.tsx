import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { BrowserRouter } from "react-router-dom";
import Login from "../pages/loginPage";
import { loginUser } from "@/services/loginService";
import { toast } from "sonner";

// Mock dependencies
vi.mock("@/services/loginService", () => ({
  loginUser: vi.fn(),
}));

vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe("Login Component", () => {
  const setIsLoggedIn = vi.fn();
  const renderLogin = () =>
    render(
      <BrowserRouter>
        <Login setIsLoggedIn={setIsLoggedIn} />
      </BrowserRouter>
    );

  it("renders correctly", () => {
    renderLogin();
    expect(screen.getByText(/Sign In to Your Account/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your Email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your Password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Sign In/i })).toBeInTheDocument();
  });

  it("shows validation errors for empty fields", async () => {
    renderLogin();
    fireEvent.click(screen.getByRole("button", { name: /Sign In/i }));
    expect(await screen.findByText(/Email is required./i)).toBeInTheDocument();
    expect(await screen.findByText(/Password is required./i)).toBeInTheDocument();
  });

  it("shows error for invalid email format", async () => {
    renderLogin();
    fireEvent.change(screen.getByPlaceholderText(/Enter your Email/i), {
      target: { value: "invalid-email" },
    });
    fireEvent.blur(screen.getByPlaceholderText(/Enter your Email/i));
    expect(await screen.findByText(/Invalid email address./i)).toBeInTheDocument();
  });

  it("calls loginUser API and handles success response", async () => {
    const mockResponse = {
      accessToken: "test-token",
      username: "test-user",
      role: "Waiter",
      data: { message: "Login successful!" },
    };
    vi.mocked(loginUser).mockResolvedValueOnce(mockResponse);

    renderLogin();
    fireEvent.change(screen.getByPlaceholderText(/Enter your Email/i), {
      target: { value: "test@test.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/Enter your Password/i), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /Sign In/i }));

    await waitFor(() => {
      expect(loginUser).toHaveBeenCalledWith({
        email: "test@test.com",
        password: "password123",
      });
      expect(localStorage.getItem("token")).toBe("test-token");
      expect(setIsLoggedIn).toHaveBeenCalledWith(true);
      expect(toast.success).toHaveBeenCalledWith("Login successful!");
    });
  });

  it("handles API error response gracefully", async () => {
    const mockError = {
      response: {
        data: {
          error: "Invalid credentials",
        },
      },
    };
    vi.mocked(loginUser).mockRejectedValueOnce(mockError);

    renderLogin();
    fireEvent.change(screen.getByPlaceholderText(/Enter your Email/i), {
      target: { value: "test@test.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/Enter your Password/i), {
      target: { value: "wrongpassword" },
    });
    fireEvent.click(screen.getByRole("button", { name: /Sign In/i }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Invalid credentials");
    });
  });

  it("toggles password visibility", () => {
    renderLogin();
    const passwordInput = screen.getByPlaceholderText(/Enter your Password/i);
    const toggleButton = screen.getByRole("button", { name: /toggle visibility/i });

    expect(passwordInput).toHaveAttribute("type", "password");
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "text");
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "password");
  });
});
