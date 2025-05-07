import { renderHook, waitFor } from "@testing-library/react";
import axios from "axios";
import { vi, describe, it, expect, beforeEach } from "vitest";
import { useLocationDetails } from "@/hooks/useLocationDetails"; // Adjust path
import { BASE_API_URL } from "@/constants/constant";

vi.mock("axios");
const mockedAxios = axios as unknown as { get: ReturnType<typeof vi.fn> };

const mockLocation = {
  id: "loc-1",
  address: "123 Street",
  description: "Test location",
  totalCapacity: "100",
  averageOccupancy: "75",
  imageUrl: "image.jpg",
  rating: "4.5",
};

describe("useLocationDetails", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should return initial state correctly", async () => {
    const { result } = renderHook(() => useLocationDetails("loc-1"));
    expect(result.current.location).toBe(null);
    expect(result.current.loading).toBe(true);
    expect(result.current.error).toBe(null);
  });

  it("should fetch location details successfully", async () => {
    mockedAxios.get = vi.fn().mockResolvedValueOnce({ data: [mockLocation] });

    const { result } = renderHook(() => useLocationDetails("loc-1"));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.location).toEqual(mockLocation);
      expect(result.current.error).toBe(null);
    });

    expect(mockedAxios.get).toHaveBeenCalledWith(`${BASE_API_URL}/locations`);
    expect(mockedAxios.get).toHaveBeenCalledTimes(1);
  });

  it("should not fetch if locationId is undefined", async () => {
    const { result } = renderHook(() => useLocationDetails(undefined));

    await waitFor(() => {
      expect(result.current.location).toBe(null);
      expect(result.current.loading).toBe(true); // stays true because no fetch is triggered
      expect(result.current.error).toBe(null);
    });

    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it("should handle case when location is not found", async () => {
    mockedAxios.get = vi.fn().mockResolvedValueOnce({ data: [{ id: "other-id" }] });

    const { result } = renderHook(() => useLocationDetails("loc-1"));

    await waitFor(() => {
      expect(result.current.location).toBe(null);
      expect(result.current.error).toBe("Location not found");
      expect(result.current.loading).toBe(false);
    });

    expect(mockedAxios.get).toHaveBeenCalledTimes(1);
  });

  it("should handle axios request error", async () => {
    mockedAxios.get = vi.fn().mockRejectedValueOnce(new Error("Network error"));

    const { result } = renderHook(() => useLocationDetails("loc-1"));

    await waitFor(() => {
      expect(result.current.location).toBe(null);
      expect(result.current.error).toBe("Network error");
      expect(result.current.loading).toBe(false);
    });

    expect(mockedAxios.get).toHaveBeenCalledTimes(1);
  });
});
