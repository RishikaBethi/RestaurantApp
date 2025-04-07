import { useParams } from "react-router-dom";
import { useLocationDetails } from "@/hooks/useLocationDetails";
import { Button } from "@/components/ui/button";
import { Star, MapPin } from "lucide-react";
import { useEffect, useState } from "react";
import SpecialtyDishCard from "@/components/specialtyDishCard";
import axios from "axios";
import ShimmerDishes from "@/components/shimmer/shimmerDishes";
import FeedbackCard from "@/components/feedbackCard"
import ShimmerFeedback from "@/components/shimmer/shimmerFeedback";
import { useFeedbacks, Feedback, FeedbackType, SortOption } from "@/hooks/useFeedbacks";
import { BASE_API_URL } from "@/constants/constant";

interface SpecialtyDish {
  id: number;
  name: string;
  price: string;
  weight: string;
  imageUrl: string;
}

export default function RestroPage() {
  const { locationId } = useParams<{ locationId: string }>();
  const { location, loading: locationLoading } = useLocationDetails(locationId);
  const [sortOption, setSortOption] = useState<SortOption>("Top rated first");
  const [specialtyDishes, setSpecialtyDishes] = useState<SpecialtyDish[]>([]);
  const [selectedFeedbackType, setSelectedFeedbackType] = useState<FeedbackType>("SERVICE");
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // Pagination state
  const [page, setPage] = useState(0);
  const { feedbacks, loading: feedbackLoading, totalPages } = useFeedbacks(
    locationId,
    selectedFeedbackType,
    sortOption,
    page
  );
  const [allFeedbacks, setAllFeedbacks] = useState<Feedback[]>([]);

  useEffect(() => {
    if (page === 0) {
      setAllFeedbacks(feedbacks);
    } else {
      setAllFeedbacks((prev) => [...prev, ...feedbacks]);
    }
  }, [feedbacks, page]);

  // Reset feedback list when filter/sort changes
  useEffect(() => {
    setPage(0);
  }, [selectedFeedbackType, sortOption]);

  useEffect(() => {
    if (!locationId) return;
    setLoading(true);
    setError("");
    axios.get(`${BASE_API_URL}/locations/${locationId}/speciality-dishes`)
      .then((response) => {
        setSpecialtyDishes(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error loading dishes:", error);
        setError("Failed to load specialty dishes.");
      })
      .finally(() => {
        setLoading(false);
      });
  }, [locationId]);

  const loadMore = () => {
    if (page + 1 < totalPages) {
      setPage((prev) => prev + 1);
    }
  };
  
  return (
    <div className="container mx-auto p-6">
       {locationLoading ? (
        <p className="text-center font-semibold text-2xl">Loading...</p>
      ) : error || !location ? (
        <p className="text-red-500">{error || "Location not found."}</p>
      ) : (
        <>
      <div className="flex flex-col md:flex-row items-center gap-6">
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-green-700">Green & Tasty</h1>
          <div className="flex items-center gap-2 mt-2 text-gray-500">
            <MapPin className="w-5 h-5 text-green-600" />
            <p>{location?.address}</p>
            <Star className="w-5 h-5 text-yellow-500" fill="gold" />
            <p>4.73</p>
          </div>
          <p className="mt-4 text-gray-700">
            Located at {location?.address}, this branch offers a perfect mix of city energy and a cozy atmosphere.
          </p>
          <p className="text-gray-700 font-semibold">Total Capacity: {location?.totalCapacity} tables</p>
          <p className="text-gray-700 font-semibold">Average Occupancy: {location?.averageOccupancy}%</p>
          <Button className="mt-4 bg-green-600 hover:bg-green-700">Book a Table</Button>
        </div>
        <img src={location?.imageUrl} alt="Restaurant" className="rounded-lg h-64 w-auto object-cover md:w-1/2" />
      </div>

      <h2 className="text-2xl font-semibold mt-10">Specialty Dishes</h2>
      {loading ? <ShimmerDishes /> : error ? <p className="text-red-500">{error}</p> : (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
          {specialtyDishes.map((dish) => (
            <SpecialtyDishCard key={dish.id} {...dish} />
          ))}
        </div>
      )}

      <h2 className="text-2xl font-semibold mt-10">Customer Reviews</h2>
      <div className="flex justify-between items-center mt-4">
        <div className="flex gap-4">
          <Button variant="ghost" className={`font-semibold ${selectedFeedbackType === "SERVICE" ? "text-green-600 border-b-2 border-green-600" : "text-gray-500"}`}
                onClick={() => setSelectedFeedbackType("SERVICE")}>
            Service
          </Button>
          <Button variant="ghost" className={`font-semibold ${selectedFeedbackType === "CUISINE_EXPERIENCE" ? "text-green-600 border-b-2 border-green-600" : "text-gray-500"}`}
                onClick={() => setSelectedFeedbackType("CUISINE_EXPERIENCE")}>
            Cuisine Experience
          </Button>
        </div>
        <select
          className="border border-green-600 p-2 rounded"
          value={sortOption}
          onChange={(e) => setSortOption(e.target.value as SortOption)}
        >
          <option>Top rated first</option>
          <option>Low rated first</option>
          <option>Newest first</option>
          <option>Oldest first</option>
        </select>
      </div>

      {feedbackLoading && page === 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
              {[...Array(4)].map((_, i) => (
                <ShimmerFeedback key={i} />
              ))}
            </div>
          ) : allFeedbacks.length === 0 ? (
            <p className="mt-4 text-center text-gray-600 font-semibold">No reviews yet.</p>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
                {allFeedbacks.map((review) => (
                  <FeedbackCard key={review.id} {...review} />
                ))}
                {feedbackLoading &&
                  [...Array(4)].map((_, i) => <ShimmerFeedback key={`shimmer-${i}`} />)}
              </div>
              {page + 1 < totalPages && (
                <div className="text-center mt-6">
                  <Button onClick={loadMore} disabled={feedbackLoading}>
                    {feedbackLoading ? "Loading..." : "Load More"}
                  </Button>
                </div>
              )}
            </>
          )}
      </>
    )}
    </div>
  );
}
