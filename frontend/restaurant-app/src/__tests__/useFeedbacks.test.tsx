import { renderHook, waitFor } from "@testing-library/react";
import axios from "axios";
import { vi, describe, it, expect, beforeEach } from "vitest";
import { useFeedbacks, FeedbackType, SortOption } from "@/hooks/useFeedbacks";
import { BASE_API_URL } from "@/constants/constant";

vi.mock("axios");
const mockedAxios = axios as unknown as { get: ReturnType<typeof vi.fn> };

describe("useFeedbacks", () => {
  const locationId = "loc-123";
  const type: FeedbackType = "SERVICE";

  const mockFeedback = [
    {
      id: "1",
      rate: "5",
      comment: "Great service!",
      userName: "John",
      userAvatarUrl: "avatar.jpg",
      date: "2025-01-01",
    },
  ];

  const mockResponse = {
    data: {
      content: mockFeedback,
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should return feedbacks for each sort option", async () => {
    const sortOptions: SortOption[] = [
      "Top rated first",
      "Low rated first",
      "Newest first",
      "Oldest first",
    ];

    for (const sortOption of sortOptions) {
      mockedAxios.get = vi.fn().mockResolvedValueOnce(mockResponse);

      const { result } = renderHook(() =>
        useFeedbacks(locationId, type, sortOption),
      );

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
        expect(result.current.feedbacks).toEqual(mockFeedback);
      });

      const expectedSortQuery = {
        "Top rated first": "rating,desc",
        "Low rated first": "rating,asc",
        "Newest first": "date,desc",
        "Oldest first": "date,asc",
      }[sortOption];

      expect(mockedAxios.get).toHaveBeenCalledWith(
        `${BASE_API_URL}/locations/${locationId}/feedbacks?type=${type}&sort=${expectedSortQuery}`,
      );
    }
  });

  it("should fallback to default sort query if unknown sort option is passed", async () => {
    mockedAxios.get = vi.fn().mockResolvedValueOnce(mockResponse);

    // Cast invalid string to SortOption to simulate unexpected input
    const invalidSort = "Invalid sort" as SortOption;

    const { result } = renderHook(() =>
      useFeedbacks(locationId, type, invalidSort),
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.feedbacks).toEqual(mockFeedback);
    });

    expect(mockedAxios.get).toHaveBeenCalledWith(
      `${BASE_API_URL}/locations/${locationId}/feedbacks?type=${type}&sort=rating,desc`,
    );
  });

  it("should handle case when locationId is undefined", async () => {
    const { result } = renderHook(() =>
      useFeedbacks(undefined, type, "Top rated first"),
    );

    expect(result.current.feedbacks).toEqual([]);
    expect(result.current.loading).toBe(true);

    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it("should handle error during fetching", async () => {
    mockedAxios.get = vi.fn().mockRejectedValueOnce(new Error("Network error"));

    const { result } = renderHook(() =>
      useFeedbacks(locationId, type, "Top rated first"),
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.feedbacks).toEqual([]); // fallback
    });

    expect(mockedAxios.get).toHaveBeenCalledTimes(1);
  });

  it("should handle empty content gracefully", async () => {
    mockedAxios.get = vi.fn().mockResolvedValueOnce({ data: {} });

    const { result } = renderHook(() =>
      useFeedbacks(locationId, type, "Top rated first"),
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.feedbacks).toEqual([]); // fallback from undefined content
    });
  });
});
