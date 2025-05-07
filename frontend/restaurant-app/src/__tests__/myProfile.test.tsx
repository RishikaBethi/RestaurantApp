import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import axios from "axios";
import { describe, it, expect, vi, beforeAll, Mocked } from "vitest";
import MyProfile from "../pages/profile";
import { toast } from "sonner";

// Mock modules
vi.mock("axios");
vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock localStorage
beforeAll(() => {
  const storageMock: Storage = {
    getItem: vi.fn((key) => {
      const mockData: Record<string, string> = {
        email: "test@example.com",
        role: "User",
        token: "mockToken",
      };
      return mockData[key] || null;
    }),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    key: vi.fn((index) => null), // Provide a basic implementation for `key`
    length: 0, // Add a dummy length property
  };

  Object.defineProperty(global, "localStorage", {
    value: storageMock,
    writable: true,
  });
});


describe("MyProfile Component", () => {
  const mockedAxios = axios as Mocked<typeof axios>;

  it("renders the MyProfile component", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        firstName: "John",
        lastName: "Doe",
        imageUrl: "",
      },
    });

    render(<MyProfile />);

    await waitFor(() => {
      expect(screen.getByText("My Profile")).toBeInTheDocument();
      expect(screen.getByText("General Information")).toBeInTheDocument();
      expect(screen.getByText("Change Password")).toBeInTheDocument();
    });
  });

  it("displays fetched profile data", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        firstName: "John",
        lastName: "Doe",
        imageUrl: "",
      },
    });

    render(<MyProfile />);

    await waitFor(() => {
      expect(screen.getByText("John Doe (User)")).toBeInTheDocument();
      expect(screen.getByText("test@example.com")).toBeInTheDocument();
    });
  });

  it("allows updating profile information", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        firstName: "John",
        lastName: "Doe",
        imageUrl: "",
      },
    });

    mockedAxios.put.mockResolvedValueOnce({
      data: {
        message: "Profile updated successfully",
      },
    });

    render(<MyProfile />);

    await waitFor(() => {
        expect(screen.getByText("My Profile")).toBeInTheDocument();
      });
    
      fireEvent.change(screen.getByLabelText(/First Name/i), {
        target: { value: "Jane" },
      });
      fireEvent.change(screen.getByLabelText(/Last Name/i), {
        target: { value: "Smith" },
      });

    fireEvent.click(screen.getByText(/Save Changes/i));

    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining("/users/profile"),
        {
          firstName: "Jane",
          lastName: "Smith",
          base64encodedImage: "",
        },
        {
            headers: {
              Authorization: "Bearer mockToken",
            },
          },
      );
      expect(toast.success).toHaveBeenCalledWith("Profile updated successfully");
    });
  });

  it("allows changing passwords", async () => {
    mockedAxios.put.mockResolvedValueOnce({
      data: {
        message: "Password changed successfully",
      },
    });

    render(<MyProfile />);

    fireEvent.click(screen.getByText(/Change Password/i));
    fireEvent.change(screen.getByLabelText(/Old Password/i), {
      target: { value: "oldPassword123" },
    });
    fireEvent.change(screen.queryAllByLabelText(/New Password/i)[0], {
      target: { value: "NewPassword123!" },
    });
    fireEvent.change(screen.getByLabelText(/Confirm New Password/i), {
      target: { value: "NewPassword123!" },
    });

    fireEvent.click(screen.getByText(/Save Changes/i));

    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining("/users/profile/password"),
        {
          oldPassword: "oldPassword123",
          newPassword: "NewPassword123!",
        },
        expect.any(Object)
      );
      expect(toast.success).toHaveBeenCalledWith("Password changed successfully");
    });
  });

  it("shows validation errors for invalid inputs", async () => {
    render(<MyProfile />);

    fireEvent.change(screen.getByLabelText(/First Name/i), {
      target: { value: "123Invalid" },
    }); 
    fireEvent.blur(screen.getByLabelText(/First Name/i));

    await waitFor(() => {
      expect(screen.getByText(/First name must start with a letter/i)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Last Name/i), {
      target: { value: "" },
    });
    fireEvent.blur(screen.getByLabelText(/Last Name/i));

    await waitFor(() => {
      expect(screen.getByText(/Last name can include/i)).toBeInTheDocument();
    });
  });
  it("shows error message on API failure when fetching profile data", async () => {
    mockedAxios.get.mockRejectedValueOnce({
      response: { data: { message: "Error fetching profile" } },
    });
  
    render(<MyProfile />);
  
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Error fetching profile");
    });
  });
  it("allows uploading a profile picture", async () => {
    const mockFile = new File(["image content"], "profile.png", {
      type: "image/png",
    });
  
    mockedAxios.put.mockResolvedValueOnce({
      data: {
        message: "Profile updated successfully",
      },
    });
  
    render(<MyProfile />);
  
    await waitFor(() => {
      expect(screen.getByText("My Profile")).toBeInTheDocument();
    });
  
    const fileInput = screen.getByLabelText(/Upload Photo/i);
    fireEvent.change(fileInput, { target: { files: [mockFile] } });
  
    fireEvent.click(screen.getByText(/Save Changes/i));
  
    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining("/users/profile"),
        expect.objectContaining({
          base64encodedImage: expect.any(String), // Verify encoded image is included
        }),
        expect.any(Object)
      );
      expect(toast.success).toHaveBeenCalledWith("Profile updated successfully");
    });
  });
  it("shows validation error when passwords do not match", async () => {
    render(<MyProfile />);
  
    fireEvent.click(screen.getByText(/Change Password/i));
    fireEvent.change(screen.queryAllByLabelText(/New Password/i)[0], {
      target: { value: "NewPassword123!" },
    });
    fireEvent.change(screen.getByLabelText(/Confirm New Password/i), {
      target: { value: "DifferentPassword123!" },
    });
  
    fireEvent.click(screen.getByText(/Save Changes/i));
  
    await waitFor(() => {
      expect(screen.getByText(/Confirm password must match new password/i)).toBeInTheDocument();
    });
  });
  it("displays the correct role from localStorage", async () => {
    global.localStorage.getItem = vi.fn((key) => {
      const mockData = {
        email: "test@example.com",
        role: "Admin",
        token: "mockToken",
      };
      return mockData[key] || null;
    });
  
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        firstName: "John",
        lastName: "Doe",
        imageUrl: "",
      },
    });
  
    render(<MyProfile />);
  
    await waitFor(() => {
      expect(screen.getByText("John Doe (Admin)")).toBeInTheDocument();
    });
  });  
});
