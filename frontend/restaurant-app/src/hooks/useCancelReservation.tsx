import axios from "axios";
import { useState } from "react";

export function useCancelReservation() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cancelReservation = async (reservationId: string | number) => {
    setLoading(true);
    setError(null);
    const token = localStorage.getItem("token");
    try {
      const response=await axios.delete(`https://35cos3vxy6.execute-api.ap-southeast-2.amazonaws.com/dev/reservations/${reservationId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      return response;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error("Cancel error:", err?.response || err);
      setError("Failed to cancel reservation.");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { cancelReservation, loading, error };
}
