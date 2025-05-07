import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, Mock } from "vitest";
import { BrowserRouter } from "react-router-dom";
import RegisterPage from "../pages/registerPage";
import { registerUser } from "../services/registerService";
import { toast } from "sonner";

vi.mock("../services/registerService", () => ({
  registerUser: vi.fn(),
}));

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

describe("RegisterPage", () => {
  const setup = () => {
    render(
      <BrowserRouter>
        <RegisterPage />
      </BrowserRouter>
    );
  };

  it("renders all form fields and buttons", () => {
    setup();

    expect(screen.getByPlaceholderText(/Enter your First Name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your Last Name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your Email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your Password/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Confirm New Password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Create an Account/i })).toBeInTheDocument();
  });

  it("validates inputs and displays errors", () => {
    setup();

    const firstNameInput = screen.getByPlaceholderText(/Enter your First Name/i);
    fireEvent.change(firstNameInput, { target: { value: "@" } });
    expect(screen.getByText(/First name must start with a letter/i)).toBeInTheDocument();

    const emailInput = screen.getByPlaceholderText(/Enter your Email/i);
    fireEvent.change(emailInput, { target: { value: "invalidemail" } });
    expect(screen.getByText(/Invalid email address/i)).toBeInTheDocument();

    const passwordInput = screen.getByPlaceholderText(/Enter your Password/i);
    fireEvent.change(passwordInput, { target: { value: "short" } });
    expect(screen.getByText(/Password must be at least 8-16 characters long/i)).toBeInTheDocument();
  });

  it("toggles password visibility", () => {
    setup();

    const passwordInput = screen.getByPlaceholderText(/Enter your Password/i);
    const toggleButton = screen.getByRole("button", { name: /toggle visibility/i });

    // Initially password is hidden
    expect(passwordInput).toHaveAttribute("type", "password");

    // Toggle visibility on
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "text");

    // Toggle visibility off
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "password");
  });

  it("prevents submission if there are validation errors", async () => {
    setup();

    const submitButton = screen.getByRole("button", { name: /Create an Account/i });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/First name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/Email is required/i)).toBeInTheDocument();
    });

    expect(registerUser).not.toHaveBeenCalled();
  });

  it("calls registerUser on valid form submission", async () => {
    setup();
 
    // Mock success response
    (registerUser as Mock).mockResolvedValue({ success: true });
 
    // Fill valid data
    fireEvent.change(screen.getByPlaceholderText("Enter your First Name"), {
      target: { value: "John" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Last Name"), {
      target: { value: "Doe" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Email"), {
      target: { value: "john_doe@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Password"), {
      target: { value: "Password1!" },
    });
    fireEvent.change(screen.getByPlaceholderText("Confirm New Password"), {
      target: { value: "Password1!" },
    });
 
    fireEvent.click(screen.getByRole("button", { name: "Create an Account" }));
 
    // Assert `registerUser` call and toast
    await waitFor(() => expect(registerUser).toHaveBeenCalledWith({
      firstName: "John",
      lastName: "Doe",
      email: "john_doe@example.com",
      password: "Password1!",
      confirmPassword:"Password1!"
    }));
    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("User registered successfully"));
  });
 
  it("shows error toast if registration fails", async () => {
    setup();
 
    // Mock error response
    (registerUser as Mock).mockRejectedValue({
      response: { data: { message: "Email already exists." } },
    });
 
    // Fill valid data
    fireEvent.change(screen.getByPlaceholderText("Enter your First Name"), {
      target: { value: "John" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Last Name"), {
      target: { value: "Doe" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Email"), {
      target: { value: "john_doe@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter your Password"), {
      target: { value: "Password1!" },
    });
    fireEvent.change(screen.getByPlaceholderText("Confirm New Password"), {
      target: { value: "Password1!" },
    });
 
    fireEvent.click(screen.getByRole("button", { name: "Create an Account" }));
 
    // Assert error toast
    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("Email already exists.")
    );
  });
});
