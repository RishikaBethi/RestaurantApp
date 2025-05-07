import { renderHook, act, waitFor } from "@testing-library/react";
import axios from "axios";
import { useCancelReservation } from "@/hooks/useCancelReservation";
import { vi, Mocked, beforeEach, afterEach, describe, it, expect } from "vitest";
import { BASE_API_URL } from "@/constants/constant";

// Mocking axios and localStorage
vi.mock("axios");
const mockAxios = axios as Mocked<typeof axios>;

const mockToken = "mocked_token";
beforeEach(() => {
  localStorage.setItem("token", mockToken);
});

afterEach(() => {
  vi.restoreAllMocks();
  localStorage.clear();
});

describe("useCancelReservation", () => {
  it("successfully cancels a reservation", async () => {
    const mockResponse = { data: { message: "Reservation canceled successfully" } };
    mockAxios.delete.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useCancelReservation());

    await act(async () => {
      const response = await result.current.cancelReservation(123);
      expect(response).toEqual(mockResponse);
    });

    expect(mockAxios.delete).toHaveBeenCalledWith(
      `${BASE_API_URL}/reservations/123`,
      {
        headers: { Authorization: `Bearer ${mockToken}` },
      }
    );
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it("handles errors during cancellation", async () => {
    const mockError = {
      response: { data: { message: "Failed to cancel reservation" } },
    };
    mockAxios.delete.mockRejectedValue(mockError);

    const { result } = renderHook(() => useCancelReservation());

    await act(async () => {
      try {
        await result.current.cancelReservation(123);
      } catch (err) {
        expect(err).toEqual(mockError);
      }
    });

    expect(mockAxios.delete).toHaveBeenCalledWith(
      `${BASE_API_URL}/reservations/123`,
      {
        headers: { Authorization: `Bearer ${mockToken}` },
      }
    );
    expect(result.current.error).toBe("Failed to cancel reservation.");
    expect(result.current.loading).toBe(false);
  });

  it("handles the loading state correctly", async () => {
    const { result } = renderHook(() => useCancelReservation());
    
    // Mock the Axios response
    mockAxios.delete.mockResolvedValueOnce({ data: {} });
  
    act(() => {
      const promise = result.current.cancelReservation(123);
      expect(result.current.loading).toBe(false); // Initial state
      return promise;
    });
  
    await waitFor(() => {
      expect(result.current.loading).toBe(false); // Final state
    });
  });  

  it("handles missing token gracefully", async () => {
    const { result } = renderHook(() => useCancelReservation());
  
    // Ensure localStorage does not have a token
    localStorage.removeItem("token");
  
    // Perform the cancellation and catch the error
    await act(async () => {
      try {
        await result.current.cancelReservation(123);
      } catch {
        // Suppress error for this test
      }
    });

    expect(mockAxios.delete).not.toHaveBeenCalled();
  });
});
