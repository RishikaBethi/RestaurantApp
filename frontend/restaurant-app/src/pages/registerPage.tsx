import React, { useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import logo from "@/assets/logo.png";
import { useNavigate } from "react-router";
import { Eye, EyeOff } from "lucide-react";
import {toast} from "sonner";
import { registerUser } from "@/services/registerService";

interface FormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

interface Errors {
  firstName: string;
  lastName: string;
  email: string;
  password: string[];
  confirmPassword: string;
}

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<FormData>({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [errors, setErrors] = useState<Errors>({
    firstName: "",
    lastName: "",
    email: "",
    password: [] as string[],
    confirmPassword: "",
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [, setLoading] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setFormData({ ...formData, [id]: value });

    // Validate input
    switch (id) {
      case "firstName":
        if (!/^[A-Za-z\-']{1,50}$/.test(value)) {
          setErrors((prev) => ({
            ...prev,
            firstName: "First name must be up to 50 characters. Only Latin letters, hyphens, and apostrophes are allowed.",
          }));
        } else {
          setErrors((prev) => ({ ...prev, firstName: "" }));
        }
        break;
      case "lastName":
        if (!/^[A-Za-z\-']{1,50}$/.test(value)) {
          setErrors((prev) => ({
            ...prev,
            lastName: "Last name must be up to 50 characters. Only Latin letters, hyphens, and apostrophes are allowed.",
          }));
        } else {
          setErrors((prev) => ({ ...prev, lastName: "" }));
        }
        break;
      case "email":
        if (!/^\S+@\S+\.\S+$/.test(value)) {
          setErrors((prev) => ({
            ...prev,
            email: "Invalid email address. Please ensure it follows the format: username@domain.com.",
          }));
        } else {
          setErrors((prev) => ({ ...prev, email: "" }));
        }
        break;
      case "password": {
        const passwordErrors: string[] = [];
        if (!/[A-Z]/.test(value)) passwordErrors.push("At least one uppercase letter required");
        if (!/[a-z]/.test(value)) passwordErrors.push("At least one lowercase letter required");
        if (!/\d/.test(value)) passwordErrors.push("At least one number required");
        if (!/[!@#$%^&*]/.test(value)) passwordErrors.push("At least one special character required");
        if (value.length < 8) passwordErrors.push("Password must be at least 8 characters long");
        setErrors((prev) => ({ ...prev, password: passwordErrors }));
        break;
      }
      case "confirmPassword":
        if (value !== formData.password) {
          setErrors((prev) => ({ ...prev, confirmPassword: "Confirm password must match new password" }));
        } else {
          setErrors((prev) => ({ ...prev, confirmPassword: "" }));
        }
        break;
      default:
        break;
    }
  };

  const handleSubmit = async() => {
    const newErrors:Errors = { ...errors };

    // Check for empty fields
    if (!formData.firstName) newErrors.firstName = "First name is required.";
    if (!formData.lastName) newErrors.lastName = "Last name is required.";
    if (!formData.email) newErrors.email = "Email is required.";
    if (!formData.password) newErrors.password = ["Password is required."];
    if (!formData.confirmPassword) newErrors.confirmPassword = "Confirm password is required.";

    setErrors(newErrors);

    // Prevent submission if there are any errors
    if (Object.values(newErrors).some((error) => (Array.isArray(error) ? error.length : error))) {
      console.error("Validation errors exist.");
      return;
    }

    console.log("Form submitted:", formData);
    setLoading(true);
    try {
      const response = await registerUser({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });
      console.log("Registration successful:", response);
      navigate("/login");
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || error.response?.data?.message || "Registration failed. Please try again.";
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const toggleShowPassword = () => setShowPassword((prev) => !prev);
  const toggleShowConfirmPassword = () => setShowConfirmPassword((prev) => !prev);

  return (
    <div className="min-h-screen bg-gray-100 flex justify-center items-center">
      <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Form Section */}
        <Card className="p-8 shadow-lg">
          <CardContent>
            <p className="text-gray-500 mb-2">LET'S GET YOU STARTED</p>
            <h3 className="text-lg font-bold text-gray-900 mb-6">Create an Account</h3>

            {/* First Name and Last Name */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div>
                <Label htmlFor="firstName" className="text-sm font-medium">
                  First Name
                </Label>
                <Input
                  id="firstName"
                  type="text"
                  value={formData.firstName}
                  onChange={handleChange}
                  placeholder="Enter your First Name"
                  className={`mt-1 ${errors.firstName ? "border-red-500" : "border-green-600"} bg-white text-gray-900`}
                />
                <p className="text-xs text-gray-500">e.g. Jonson</p>
                {errors.firstName && <p className="text-xs text-red-500">{errors.firstName}</p>}
              </div>
              <div>
                <Label htmlFor="lastName" className="text-sm font-medium">
                  Last Name
                </Label>
                <Input
                  id="lastName"
                  type="text"
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Enter your Last Name"
                  className={`mt-1 ${errors.lastName ? "border-red-500" : "border-green-600"} text-gray-900`}
                />
                <p className="text-xs text-gray-500">e.g. Doe</p>
                {errors.lastName && <p className="text-xs text-red-500">{errors.lastName}</p>}
              </div>
            </div>

            {/* Email */}
            <div className="mb-4">
              <Label htmlFor="email" className="text-sm font-medium">
                Email
              </Label>
              <Input
                id="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Enter your Email"
                className={`mt-1 ${errors.email ? "border-red-500" : "border-green-600"}`}
              />
              <p className="text-xs text-gray-500">e.g. username@domain.com</p>
              {errors.email && <p className="text-xs text-red-500">{errors.email}</p>}
            </div>

            {/* Password */}
            <div className="mb-4">
              <Label htmlFor="password" className="text-sm font-medium">
                Password
              </Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your Password"
                  className={`mt-1 ${errors.password.length ? "border-red-500" : "border-green-600"}`}
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-3 flex items-center"
                  onClick={toggleShowPassword}
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
              <ul className="text-xs mt-2">
                {["At least one uppercase letter required", "At least one lowercase letter required", "At least one number required", "At least one special character required", "Password must be at least 8 characters long"].map((rule, index) => (
                  <li
                    key={index}
                    className={errors.password.includes(rule) ? "text-red-500" : "text-gray-500"}
                  >
                    {rule}
                  </li>
                ))}
              </ul>
              {errors.password.length === 1 && errors.password[0] === "Password is required." && (
                <p className="text-xs text-red-500">{errors.password[0]}</p>
              )}
            </div>

            {/* Confirm Password */}
            <div className="mb-4">
              <Label htmlFor="confirmPassword" className="text-sm font-medium">
                Confirm New Password
              </Label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="Confirm New Password"
                  className={`mt-1 ${errors.confirmPassword ? "border-red-500" : "border-green-600"} text-gray-900`}
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-3 flex items-center"
                  onClick={toggleShowConfirmPassword}
                >
                  {showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
              {errors.confirmPassword && <p className="text-xs text-red-500">{errors.confirmPassword}</p>}
            </div>

            <Button className="w-full bg-green-600 text-white mt-4" onClick={handleSubmit}>Create an Account</Button>
            <p className="text-sm text-gray-600 text-center mt-4">
              Already have an account? <a href="/login" className="text-blue-600 font-medium">Login</a> instead
            </p>
          </CardContent>
        </Card>

        {/* Illustration Section */}
        <div className="hidden md:flex justify-center items-center">
          <div className="flex flex-col items-center">
            <img src={logo} alt="Green & Tasty" className="w-full" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
