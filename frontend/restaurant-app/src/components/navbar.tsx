import { useState, useRef, useEffect } from "react";
import { Link, useNavigate,useLocation } from "react-router-dom";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import image from "../assets/image.png";
 
export default function Navbar({ isLoggedIn, setIsLoggedIn }: { isLoggedIn: boolean; setIsLoggedIn: (value: boolean) => void }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [avatarDropdownOpen, setAvatarDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
 
  const user = JSON.parse(localStorage.getItem("user") || '""');
  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role") || "";
  const initials = 
  typeof user === "string"
  ? user
      .split(" ")
      .map((word: string) => word.charAt(0))
      .join("")
  : "";
 
  useEffect(() => {
    const token = localStorage.getItem("token");
    setIsLoggedIn(!!token);
  }, [setIsLoggedIn]);
 
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setAvatarDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const isMainPageActive =
    location.pathname === "/" ||
    location.pathname.startsWith("/restaurant") ||
    location.pathname.startsWith("/menu");
 
  // Render menu items based on role
  const renderLinks = () => {
    const linkClasses = (isActive: boolean) =>
      `text-lg ${
        isActive ? "text-green-600 border-b-2 border-green-600" : "text-gray-600 hover:text-gray-500"
      }`;

    if (role === "Waiter" && isLoggedIn) {
      return (
        <>
          <Link to="/waiter-reservations" className={linkClasses(location.pathname === "/waiter-reservations")}>Reservations</Link>
          <Link to="/menu" className={linkClasses(location.pathname === "/menu")}>Menu</Link>
        </>
      );
    }
 
    // Public links or logged-in customer
    return (
      <>
        <Link to="/" className={linkClasses(isMainPageActive)}>Main Page</Link>
        <Link to="/book-table" className={linkClasses(location.pathname === "/book-table")}>Book a Table</Link>
        {isLoggedIn && (
          <Link to="/reservations" className={linkClasses(location.pathname === "/reservations")}>Reservations</Link>
        )}
      </>
    );
  };
 
 
  return (
    <nav className="flex items-center bg-white shadow p-2 relative">
      <Link to={role === "Waiter" ? "/waiter-reservations" : "/"}>
        <img src={image} alt="Green & Tasty" className="h-10 w-auto" />
      </Link>
 
      <div className="flex-1 hidden md:block" />
 
      {/* Desktop Menu */}
      <div className="hidden md:flex items-center gap-6 font-semibold">
        {renderLinks()}
      </div>
 
      <div className="flex-1 hidden md:block" />
 
      {/* Right Section */}
      <div className="hidden md:flex items-center">
        {isLoggedIn ? (
          <div className="relative" ref={dropdownRef}>
            <Avatar className="cursor-pointer" onClick={() => setAvatarDropdownOpen(!avatarDropdownOpen)}>
              <AvatarImage src="https://via.placeholder.com/150" alt="User Avatar" />
              <AvatarFallback className="text-xs">{initials}</AvatarFallback>
            </Avatar>
            {avatarDropdownOpen && (
              <div className="absolute right-0 mt-2 w-40 bg-white shadow rounded z-50">
                <div className="p-1 m-1 text-sm">
                  <p>{user} ({role})</p>
                  <p>{email}</p>
                </div>
                <hr className="border-t-1 border-gray-400" />
                <Link to="/profile" className="block px-4 py-2 text-sm hover:bg-gray-100">My Profile</Link>
                <button
                  onClick={() => {
                    localStorage.clear();
                    setIsLoggedIn(false);
                    setAvatarDropdownOpen(false);
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
          <Button onClick={() => navigate("/login")} className="bg-green-600 hover:bg-green-700">
            Sign In
          </Button>
        )}
      </div>
 
      {/* Mobile Hamburger */}
      <div className="md:hidden ml-auto">
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="text-gray-600 focus:outline-none"
          aria-label="Toggle mobile menu"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
      </div>
 
      {/* Mobile Dropdown */}
      {mobileMenuOpen && (
        <div className="absolute top-14 right-2 w-48 bg-white shadow rounded-md z-50 md:hidden" data-testid="mobile-menu">
          {role === "Waiter" ? (
            <>
              <Link
                to="/waiter-reservations"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Reservations
              </Link>
              <Link
                to="/menu"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Menu
              </Link>
            </>
          ) : (
            <>
              <Link
                to="/"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Main Page
              </Link>
              <Link
                to="/book-table"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Book a Table
              </Link>
              <Link
                to="/reservations"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Reservations
              </Link>
            </>
          )}
 
          {isLoggedIn ? (
            <>
              <Link
                to="/profile"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                My Profile
              </Link>
              <button
                onClick={() => {
                  localStorage.clear();
                  setIsLoggedIn(false);
                  setMobileMenuOpen(false);
                  navigate("/login");
                }}
                className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                Sign Out
              </button>
            </>
          ) : (
            <button
              onClick={() => {
                setMobileMenuOpen(false);
                navigate("/login");
              }}
              className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100"
            >
              Sign In
            </button>
          )}
        </div>
      )}
    </nav>
  );
}
 
 