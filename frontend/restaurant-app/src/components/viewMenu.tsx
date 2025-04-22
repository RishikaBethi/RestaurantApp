import { useState, useEffect, useRef } from "react";
import saladImage from "@/assets/homepage.png";
import dishImage from "@/assets/dishImage.png";
import { Link, useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { Dish } from "@/components/dish";
import { BASE_API_URL } from "@/constants/constant";
import Spinner from "./shimmerUI/spinner";
import { toast } from "sonner";

// Categories for filtering
const categories = ["Appetizers", "Main Courses", "Desserts"];

type Dish = {
  id: string;
  title: string;
  image: string;
  price: string;
  weight: string;
  category: string;
  state: string;
};

const MenuPage = () => {
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [menuData, setMenuData] = useState<Dish[]>([]);
  const [sortOrder, setSortOrder] = useState("Popularity Descending");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const toastShownRef = useRef(false); // Track whether toast has been shown

  // Redirect to login if not logged in
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token && !toastShownRef.current) {
      toast.error("Please login to browse or view the menu.");
      toastShownRef.current = true; // Mark the toast as shown
      navigate("/login");
    }
  }, [navigate]);
  
  const getSortParam = () => {
    switch (sortOrder) {
      case "Price Low to High":
        return "price-asc";
      case "Price High to Low":
        return "price-desc";
      case "Popularity Ascending":
        return "popularity-asc";
      case "Popularity Descending":
        return "popularity-desc";
      default:
        return "";
    }
  };

  // Fetching menu data based on selected category
  useEffect(() => {
    const fetchMenuData = async () => {
      setIsLoading(true);
      try {
        const token = localStorage.getItem("token"); // Get Bearer token from localStorage
        const dishType = selectedCategory !== "All" ? selectedCategory.toUpperCase() : "";
        const sortBy = getSortParam();

        const response = await axios.get(
          `${BASE_API_URL}/dishes?dishType=${dishType}&sortBy=${sortBy}`,
          {
            headers: {
              Authorization: `Bearer ${token}`, // Adding token to request header
            },
          }
        );

        // Using correct structure from response data
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const dishes = response.data?.content?.map((dish: any) => ({
          id: dish.id,
          title: dish.name || "Unnamed Dish",
          image: dish.previewImageUrl || dishImage, // Fallback to dummy image if not available
          price: dish.price || "Price not available",
          weight: dish.weight || "Weight not specified",
          state: dish.state || "Unavailable",
          category: selectedCategory,
        })) || [];

        setMenuData(dishes);
      } catch (error) {
        console.error("Error fetching menu data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchMenuData();
  }, [selectedCategory,sortOrder]);

  // Handling category click and changing the selected category
  const handleMenuClick = () => {
    if (location.pathname === "/menu") {
      navigate("/menu", { replace: true });
    } else {
      navigate("/menu");
    }
  };

  const filteredMenu = menuData

  return (
    <div>
      {/* Breadcrumb */}
      <div className="text-sm text-gray-500 px-6 pt-4">
        <Link to="/" className="text-gray-600 hover:underline">Main page</Link> &gt;{" "}
        <button onClick={handleMenuClick} className="text-black font-medium">
          Menu
        </button>
      </div>

      {/* Header Section */}
      <div className="relative h-72 mt-2">
        <div
          className="absolute inset-0 bg-cover bg-center filter backdrop-blur-sm"
          style={{ backgroundImage: `url(${saladImage})` }}
        />
        <div className="relative z-10 flex flex-col justify-center h-full pl-6 md:pl-6">
          <h1 className="text-green-500 text-2xl md:text-2xl font-bold">Green & Tasty Restaurants</h1>
          <p className="text-green-600 text-lg md:text-3xl font-bold mt-1">Menu</p>
        </div>
      </div>

      {/* Category Filter and Sort */}
      <div className="flex flex-wrap items-center justify-between px-6 py-4 gap-4">
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setSelectedCategory("All")}
            className={`border px-4 py-1 rounded-full ${selectedCategory === "All" ? "bg-green-500 text-white" : "text-green-600 border-green-500"}`}
          >
            All
          </button>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`border px-4 py-1 rounded-full ${selectedCategory === cat ? "bg-green-500 text-white" : "text-green-600 border-green-500"}`}
            >
              {cat}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <label htmlFor="sort" className="text-sm text-gray-600 w-1/3 md:w-auto text-center md:text-left">Sort by:</label>
          <select
            id="sort"
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value)}
            className="border rounded-md px-2 py-1 text-sm w-2/3 md:w-auto"
          >
            <option>Popularity Descending</option>
            <option>Popularity Ascending</option>
            <option>Price Low to High</option>
            <option>Price High to Low</option>
          </select>
        </div>
      </div>

      {/* Menu Cards Section */}
      <div className="p-6 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {isLoading ? (
          <div className="flex justify-center text-xl"><Spinner/></div>
        ) : filteredMenu.length > 0 ? (
          filteredMenu.map((item) => (
            <Dish
            key={item.id}
            dishId={item.id}
            trigger={
              <div className={`relative bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition cursor-pointer 
                ${item.state.toLowerCase() === "on stop" ? "opacity-40 pointer-events-none" : ""}`}>
                  {/* On Stop badge */}
                  {item.state.toLowerCase() === "on stop" && (
                    <div className="absolute top-2 right-2 bg-red-200 text-red-600 text-xs px-2 py-0.5 rounded-full font-semibold">
                      On Stop
                      </div>
                    )}
                <img
                  src={item.image}
                  alt={item.title}
                  className="w-32 h-32 object-cover rounded-full mx-auto"
                />
                <h3 className="mt-4 font-semibold text-gray-800 text-center">{item.title}</h3>
                <div className="flex justify-between mt-2 text-sm text-gray-600">
                  <span>{item.price}</span>
                  <span>{item.weight}</span>
                </div>
              </div>
            }
          />
          ))
        ) : (
          <p>No dishes available for {selectedCategory}.</p>
        )}
      </div>
    </div>
  );
};

export default MenuPage;
