import { describe, it, expect, vi, beforeEach } from "vitest";
import api from "@/services/api";
import { registerUser } from "../services/registerService"; // adjust path if needed

vi.mock("@/services/api");

const mockedApi = api as unknown as {
  post: ReturnType<typeof vi.fn>;
};

describe("registerUser", () => {
  const mockUserData = {
    firstName: "Alice",
    lastName: "Smith",
    email: "alice@example.com",
    password: "Password123!",
    confirmPassword: "Password123!",
  };

  const mockResponseData = {
    message: "User registered successfully",
    userId: "abc123",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should send registration request and return response data", async () => {
    mockedApi.post = vi.fn().mockResolvedValueOnce({ data: mockResponseData });

    const result = await registerUser(mockUserData);

    expect(mockedApi.post).toHaveBeenCalledWith("/auth/sign-up", mockUserData);
    expect(result).toEqual(mockResponseData);
  });

  it("should throw an error if the request fails", async () => {
    const mockError = new Error("Email already in use");
    mockedApi.post = vi.fn().mockRejectedValueOnce(mockError);

    await expect(registerUser(mockUserData)).rejects.toThrow("Email already in use");
    expect(mockedApi.post).toHaveBeenCalledWith("/auth/sign-up", mockUserData);
  });
});
