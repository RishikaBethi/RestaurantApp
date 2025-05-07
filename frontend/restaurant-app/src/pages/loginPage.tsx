import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import logo from "@/assets/logo.png";
import { useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { toast } from "sonner";
import { loginUser } from "@/services/loginService";

export default function Login({ setIsLoggedIn }: { setIsLoggedIn: (value: boolean) => void }) {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({ email: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });

    if (name === "email") {
      if (!/^\S+@\S+\.\S+$/.test(value)) {
        setErrors((prev) => ({
          ...prev,
          email: "Invalid email address. Please use format: username@domain.com.",
        }));
      } else {
        setErrors((prev) => ({ ...prev, email: "" }));
      }
    } else if (name === "password") {
      setErrors((prev) => ({ ...prev, password: "" }));
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const newErrors = { email: "", password: "" };
    if (!formData.email.trim()) {
      newErrors.email = "Email is required.";
    } else if (!/^\S+@\S+\.\S+$/.test(formData.email)) {
      newErrors.email = "Invalid email address.";
    }
    if (!formData.password.trim()) {
      newErrors.password = "Password is required.";
    }

    setErrors(newErrors);

    if (newErrors.email || newErrors.password) return;

    try {
      setLoading(true);
      const response = await loginUser(formData);

      // Assuming the API returns a token & user data
      const { accessToken, username, role } = response;

      // Store token & user in localStorage
      localStorage.setItem("token", accessToken);
      localStorage.setItem("user", JSON.stringify(username));
      localStorage.setItem("role",role);
      localStorage.setItem("email",formData.email);

      setIsLoggedIn(true);

      toast.success(response?.data?.message || "Login successful!");

      if (role === "Waiter") {
        navigate("/waiter-reservations", { replace: true });
      } else {
        navigate("/", { replace: true }); // default fallback
      }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || error.response?.data?.message ||"Login failed. Please try again.";
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100 p-4">
      <Card className="max-w-md w-full shadow-lg">
        <CardContent className="p-6">
          <h3 className="text-sm text-gray-500">WELCOME BACK</h3>
          <h2 className="text-2xl font-bold mt-1">Sign In to Your Account</h2>

          <form onSubmit={handleSubmit} className="mt-6">
            <div>
              <label className="block text-sm font-medium">Email</label>
              <Input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Enter your Email"
                className={`mt-1 ${errors.email ? "border-red-500" : ""}`}
              />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
            </div>

            <div className="mt-4 relative">
              <label className="block text-sm font-medium">Password</label>
              <div className="relative">
                <Input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your Password"
                  className={`mt-1 pr-10 ${errors.password ? "border-red-500" : ""}`}
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-3 flex items-center text-gray-500"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="toggle visibility"
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
            </div>

            <Button type="submit" className="w-full mt-6 bg-green-600 hover:bg-green-700" disabled={loading}>
              {loading ? "Signing in..." : "Sign In"}
            </Button>
          </form>

          <p className="mt-4 text-sm text-gray-500">
            Don't have an account?{" "}
            <a href="/register" className="text-blue-600 font-medium">
              Create an Account
            </a>
          </p>
        </CardContent>
      </Card>
      <div className="hidden md:flex justify-center items-center ml-7">
        <div className="flex flex-col items-center">
          <img src={logo} alt="Green & Tasty" className="w-108" />
        </div>
      </div>
    </div>
  );
}
