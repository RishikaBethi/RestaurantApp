import { useEffect, useState } from "react";
import axios from "axios";
import { BASE_API_URL } from "@/constants/constant";

export type FeedbackType = "SERVICE" | "CUISINE_EXPERIENCE";
export type SortOption = "Top rated first" | "Low rated first" | "Newest first" | "Oldest first";

export interface Feedback {
  id: string;
  rate: string;
  comment: string;
  userName: string;
  userAvatarUrl: string;
  date: string;
}

export function useFeedbacks(
  locationId: string | undefined,
  type: FeedbackType,
  sortOption: SortOption,
) {
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const getSortQuery = () => {
    switch (sortOption) {
      case "Top rated first":
        return "rating,desc";
      case "Low rated first":
        return "rating,asc";
      case "Newest first":
        return "date,desc";
      case "Oldest first":
        return "date,asc";
      default:
        return "rating,desc";
    }
  };

  useEffect(() => {
    if (!locationId) return;

    setLoading(true);
    const query = getSortQuery();
    const endpoint = `${BASE_API_URL}/locations/${locationId}/feedbacks?type=${type}&sort=${query}`;

    axios
      .get(endpoint)
      .then((res) => {
        setFeedbacks(res.data.content || []);
      })
      .catch((err) => {
        console.error("Error fetching feedbacks:", err);
      })
      .finally(() => setLoading(false));
  }, [locationId, type, sortOption]);

  return { feedbacks, loading };
}
