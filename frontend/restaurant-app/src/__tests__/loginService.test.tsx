import { describe, it, expect, vi, beforeEach } from "vitest";
import api from "../services/api";
import { loginUser } from "../services/loginService"; // Update this path if different

vi.mock("./api");

const mockedApi = api as unknown as {
  post: ReturnType<typeof vi.fn>;
};

describe("loginUser", () => {
  const payload = {
    email: "test@example.com",
    password: "securepassword",
  };

  const mockResponse = {
    token: "mock-token",
    user: {
      id: "123",
      name: "John Doe",
      email: "test@example.com",
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should send login request and return user data", async () => {
    mockedApi.post = vi.fn().mockResolvedValueOnce({ data: mockResponse });

    const result = await loginUser(payload);

    expect(mockedApi.post).toHaveBeenCalledWith("/auth/sign-in", payload);
    expect(result).toEqual(mockResponse);
  });

  it("should throw an error if the request fails", async () => {
    mockedApi.post = vi.fn().mockRejectedValueOnce(new Error("Invalid credentials"));

    await expect(loginUser(payload)).rejects.toThrow("Invalid credentials");
    expect(mockedApi.post).toHaveBeenCalledWith("/auth/sign-in", payload);
  });
});
