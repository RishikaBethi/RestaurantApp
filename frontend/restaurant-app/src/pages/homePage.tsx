import DishCard from "@/components/dishCard";
import LocationCard from "@/components/locationCard";
import saladImage from "@/assets/homepage.png";
 
// Import images for dishes
import strawberrySalad from "@/assets/dishImage.png";
import avocadoBowl from "@/assets/dishImage.png";
import lentilSalad from "@/assets/dishImage.png";
import springSalad from "@/assets/dishImage.png";
 
// Import images for locations
import location1 from "@/assets/locationImage.png";
import location2 from "@/assets/locationImage.png";
import location3 from "@/assets/locationImage.png";
import { Link } from "react-router-dom";
 
const dishes = [
  { image: strawberrySalad, name: "Fresh Strawberry Mint Salad", price: "17$", weight: "430g" },
  { image: avocadoBowl, name: "Avocado Pine Nut Bowl", price: "17$", weight: "430g" },
  { image: lentilSalad, name: "Roasted Potato & Lentil Salad", price: "17$", weight: "430g" },
  { image: springSalad, name: "Spring Salad", price: "17$", weight: "430g" }
];
 
const locations = [
  { image: location1, address: "48 Rustaveli Avenue", totalCapacity: 10, averageOccupancy: 90,id:1 },
  { image: location2, address: "14 Baratashvili Street", totalCapacity: 18, averageOccupancy: 78,id:2 },
  { image: location3, address: "9 Abashidze Street", totalCapacity: 20, averageOccupancy: 99,id:3 }
];
 
export default function Home() {
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
          <button className="mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 rounded text-white">View Menu</button>
        </div>
      </header>
 
      {/* Most Popular Dishes Section */}
      <section className="p-6">
        <h3 className="text-xl font-semibold">Most Popular Dishes</h3>
        <div className="grid grid-cols-4 gap-4 mt-4">
          {dishes.map((dish, index) => (
            <DishCard key={index} {...dish} />
          ))}
        </div>
      </section>
 
      {/* Locations Section */}
      <section className="p-6">
        <h3 className="text-xl font-semibold">Locations</h3>
        <div className="grid grid-cols-3 gap-4 mt-4">
          {locations.map((location) => (
             <Link key={location.id} to={`/restaurant/${location.id}`} className="block transform transition duration-300 hover:scale-105 hover:shadow-lg">
            <LocationCard {...location} />
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}