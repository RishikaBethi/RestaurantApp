import PopularDishCard from "@/components/popularDishCard";
import LocationCard from "@/components/locationCard";
import saladImage from "@/assets/homepage.png";
import ShimmerDishes from "@/components/shimmerUI/shimmerDishes";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import ShimmerLocations from "@/components/shimmerUI/shimmerLocations";
import { BASE_API_URL } from "@/constants/constant";
import { useNavigate } from "react-router-dom";

interface Location {
  id: string;
  address: string;
  description: string;
  totalCapacity: string;
  averageOccupancy: string;
  imageUrl: string;
  rating: string;
}
interface PopularDish {
  id: string;
  name: string;
  price: string;
  weight: string;
  imageUrl: string;
}
 
export default function Home() {
  const [locations, setLocations] = useState<Location[]>([]);
  const [popularDishes, setPopularDishes] = useState<PopularDish[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate=useNavigate();

  useEffect(() => {
    setLoading(true);
    setError("");
    axios.get(`${BASE_API_URL}/locations`)
      .then(response => {
        setLocations(response.data);
      })
      .catch(error => {
        console.error("Error fetching locations:", error);
        setError("Failed to load locations!");
      });
 
   axios.get(`${BASE_API_URL}/dishes/popular`)
      .then((response) => {
        setPopularDishes(response.data);
      })
      .catch((error) => {
        console.error("Error fetching dishes:", error);
        setError("Failed to load dishes!");
      })
      .finally(() => {
        setLoading(false); 
      });
    }, []);

  return (
    <div className="bg-gray-100 min-h-screen">
      <header
        className="relative h-65 flex items-center justify-start text-white p-4 z-0"
        style={{
          backgroundImage: `url(${saladImage})`,
          backgroundSize: "100% auto",
          backgroundPosition: "left center",
          backgroundRepeat: "no-repeat"
        }}
      >
        <div className="text-center p-4">
          <h2 className="text-3xl font-bold">Green & Tasty</h2>
          <p className="text-lg">Fresh, healthy, and sustainable cuisine</p>
          <button className="mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 rounded text-white" onClick={()=>navigate("/menu")}>View Menu</button>
        </div>
      </header>
 
      {/* Most Popular Dishes Section */}
      <section className="p-6">
        <h3 className="text-xl font-semibold">Most Popular Dishes</h3>
        {loading ? <ShimmerDishes /> : error ? <p className="text-red-500">{error}</p> : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 mt-4">
           {Array.isArray(popularDishes) && popularDishes.length > 0 ? (
  popularDishes.map((dish, index) => (
    <PopularDishCard key={index} {...dish} />
  ))
) : (
  <p>No popular dishes available.</p>
)}
          </div>
        )}
      </section>
 
      {/* Locations Section */}
<section className="p-6">
  <h3 className="text-xl font-semibold">Locations</h3>
  {loading ? (
    <ShimmerLocations />
  ) : (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 gap-4 mt-4">
      {locations.map((location) => (
        <Link
          key={location.id}
          to={`/restaurant/${location.id}`}
          className="block transform transition duration-300 hover:scale-105 hover:shadow-lg"
        >
          <LocationCard
            image={location.imageUrl}
            address={location.address}
            totalCapacity={parseInt(location.totalCapacity)}
            averageOccupancy={parseInt(location.averageOccupancy)}
          />
        </Link>
      ))}
    </div>
  )}
</section>
    </div>
  );
}