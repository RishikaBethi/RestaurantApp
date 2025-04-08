import { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import image from "../assets/image.png";
 
export default function Navbar({ isLoggedIn, setIsLoggedIn }: { isLoggedIn: boolean; setIsLoggedIn: (value: boolean) => void }) {
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const user=JSON.parse(localStorage.getItem("user")|| '""');
  const email=localStorage.getItem("email");
  const role=localStorage.getItem("role");
  const initials = user
  .split(" ")
  .map((word: string) => word.charAt(0))
  .join("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      setIsLoggedIn(true);
    } else {
      setIsLoggedIn(false);
    }
  }, [setIsLoggedIn]);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);
 
  return (
    <nav className="flex justify-between items-center bg-white shadow p-2">
      {/* Logo */}
      <Link to="/">
        <img src={image} alt="Green & Tasty" />
      </Link>
 
      {/* Menu Links */}
      <div className="flex items-center gap-6 font-semibold">
        <Link to="/" className="text-gray-600 hover:text-green-600 text-lg">
          Main Page
        </Link>
        <Link to="/book-table" className="text-gray-600 hover:text-green-600 text-lg">
          Book a Table
        </Link>
        {isLoggedIn && (
          <Link to="/reservations" className="text-gray-600 hover:text-green-600 text-lg">
            Reservations
          </Link>
        )}
      </div>
 
      {/* Right Section */}
      <div className="flex items-center">
        {isLoggedIn ? (
          // Logged-in View (User Dropdown)
          <div className="relative" ref={dropdownRef}>
            <Avatar className="cursor-pointer" onClick={() => setDropdownOpen(!dropdownOpen)}>
              <AvatarImage src="https://via.placeholder.com/150" alt="User Avatar" />
              <AvatarFallback className="text-xs">{initials}</AvatarFallback>
            </Avatar>
            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-40 bg-white shadow rounded z-50">
                <div className="p-1 m-1">
                  <p className="text-xs">{user} ({role})</p>
                  <p className="text-xs">{email}</p>
                </div>
                <hr className="border-t-1 border-gray-400" />
                <Link to="/profile" className="block px-4 py-2 text-sm hover:bg-gray-100">
                  My Profile
                </Link>
                <button
                  onClick={() => {
                    localStorage.removeItem("user");
                    localStorage.removeItem("token");
                    localStorage.removeItem("role");
                    localStorage.removeItem("email");
                    setIsLoggedIn(false);
                    setDropdownOpen(false);
                    navigate("/login");
                  }}
                  className="block w-full px-4 py-2 text-sm text-left hover:bg-gray-100"
                >
                  Sign Out
                </button>
              </div>
            )}
          </div>
        ) : (
          // Logged-out View (Sign In Button)
          <Button
            onClick={() => navigate("/login")}
            className="bg-green-600 hover:bg-green-700"
          >
            Sign In
          </Button>
        )}
      </div>
    </nav>
  );
}