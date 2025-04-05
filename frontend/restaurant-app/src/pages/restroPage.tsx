import { useParams } from "react-router-dom";
import { useLocationDetails } from "@/hooks/useLocationDetails";
import { Button } from "@/components/ui/button";
import { Star, MapPin } from "lucide-react";
import { useEffect, useState } from "react";
import SpecialtyDishCard from "@/components/specialtyDishCard";
import axios from "axios";
import ShimmerDishes from "@/components/shimmer/shimmerDishes";

interface SpecialtyDish {
  id: number;
  name: string;
  price: string;
  weight: string;
  imageUrl: string;
}

const reviews = [
  {
    id: 1,
    name: "David",
    date: "Aug 5, 2023",
    rating: 5,
    text: "Absolutely loved this restaurant! The outdoor terrace was perfect for a relaxing evening.",
  },
  {
    id: 2,
    name: "User1785",
    date: "Jul 11, 2023",
    rating: 5,
    text: "The best dining experience I’ve had in Tbilisi. The vegan options were fantastic.",
  },
  {
    id: 3,
    name: "Giorgi",
    date: "Jul 4, 2023",
    rating: 5,
    text: "Great food and an excellent vibe! The place has a lively atmosphere.",
  },
  {
    id: 4,
    name: "Anna",
    date: "Jun 18, 2023",
    rating: 5,
    text: "I visited with friends and was blown away by the creativity of the menu.",
  },
];

export default function RestroPage() {
  const { locationId } = useParams<{ locationId: string }>();
  const { location, loading: locationLoading } = useLocationDetails(locationId);
  const [sortOption, setSortOption] = useState("Top rated first");
  const [specialtyDishes, setSpecialtyDishes] = useState<SpecialtyDish[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    if (!locationId) return;
    
    setLoading(true);
    setError("");

    axios.get(`https://ig8csmv3m6.execute-api.ap-southeast-2.amazonaws.com/devss/locations/${locationId}/speciality-dishes`)
      .then((response) => {
        setSpecialtyDishes(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error loading dishes:", error);
        setError("Failed to load specialty dishes.");
        setLoading(false);
      });
  }, [locationId]);
  
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
          <Button variant="ghost" className="text-green-600 font-semibold border-b-2 border-green-600">
            Service
          </Button>
          <Button variant="ghost" className="text-gray-500 font-semibold">Cuisine Experience</Button>
        </div>
        <select
          className="border border-green-600 p-2 rounded"
          value={sortOption}
          onChange={(e) => setSortOption(e.target.value)}
        >
          <option>Top rated first</option>
          <option>Low rated first</option>
          <option>Newest first</option>
          <option>Oldest first</option>
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
        {reviews.map((review) => (
          <div key={review.id} className="bg-white shadow rounded-lg p-4">
            <div className="flex items-center gap-3">
              <div className="bg-gray-300 rounded-full w-10 h-10"></div>
              <div>
                <h3 className="font-semibold">{review.name}</h3>
                <p className="text-xs text-gray-500">{review.date}</p>
              </div>
              <div className="flex"><Star className="w-3 h-3 text-yellow-500" fill="gold"/>
              <Star className="w-3 h-3 text-yellow-500" fill="gold" />
              <Star className="w-3 h-3 text-yellow-500" fill="gold" />
              <Star className="w-3 h-3 text-yellow-500" fill="gold" />
              <Star className="w-3 h-3 text-yellow-500" fill="gold"/>
              </div>
            </div>
            <p className="mt-2 text-gray-700">{review.text}</p>
          </div>
        ))}
      </div>
      </>
    )}
    </div>
  );
}
