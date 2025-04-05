import { useEffect, useState } from "react";
import axios from "axios";

interface Location {
  id: string;
  address: string;
  description: string;
  totalCapacity: string;
  averageOccupancy: string;
  imageUrl: string;
  rating: string;
}

export const useLocationDetails = (locationId: string | undefined) => {
  const [location, setLocation] = useState<Location | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchLocation = async () => {
      if (!locationId) return;

      try {
        setLoading(true);
        const res = await axios.get<Location[]>("https://ig8csmv3m6.execute-api.ap-southeast-2.amazonaws.com/devss/locations");
        const matchedLocation = res.data.find((loc) => loc.id === locationId);
        if (!matchedLocation) {
          throw new Error("Location not found");
        }
        setLocation(matchedLocation);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        setError(err.message || "Error fetching location");
        setLocation(null);
      } finally {
        setLoading(false);
      }
    };

    fetchLocation();
  }, [locationId]);

  return { location, loading, error };
};
