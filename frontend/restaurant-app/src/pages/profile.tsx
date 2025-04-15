import React, { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Eye, EyeOff, Plus } from "lucide-react";
import axios from "axios";
 
const MyProfile: React.FC = () => {
  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role");
  const token = localStorage.getItem("token");
 
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [activeTab, setActiveTab] = useState("general");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmVisible, setConfirmVisible] = useState(false);
 
  useEffect(() => {
    const fetchProfile = async () => {
      if (!token) {
        console.error("Token not found. Redirecting to login.");
        window.location.href = "/login";
        return;
      }
 
      try {
        const response = await axios.get(
          "https://1mp0zjlwj4.execute-api.ap-south-1.amazonaws.com/api/users/profile",
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
 
        const { firstName, lastName, imageUrl } = response.data.data;
        setFirstName(firstName);
        setLastName(lastName);
        setImageUrl(imageUrl);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (error: any) {
        console.error("Error fetching profile:", error);
        if (error.response?.status === 401) {
          alert("Session expired. Please login again.");
          window.location.href = "/login";
        }
      }
    };
 
    fetchProfile();
  }, [token]);
 
  const initials = `${firstName.charAt(0)}${lastName.charAt(0)}`;
 
  return (
    <div className="min-h-screen bg-gray-100">
      <h1 className="text-xl font-bold text-white bg-green-700 p-4 pl-6 sm:pl-9">
        My Profile
      </h1>
      <div className="container mx-auto px-4 py-6 grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar */}
        <aside className="bg-transparent p-2 sm:p-4 rounded-lg">
          <ul className="space-y-2">
            <li>
              <button
                onClick={() => setActiveTab("general")}
                className={`font-medium ${
                  activeTab === "general"
                    ? "text-green-600"
                    : "text-gray-600 hover:text-green-600"
                }`}
              >
                General Information
              </button>
            </li>
            <li>
              <button
                onClick={() => setActiveTab("password")}
                className={`font-medium ${
                  activeTab === "password"
                    ? "text-green-600"
                    : "text-gray-600 hover:text-green-600"
                }`}
              >
                Change Password
              </button>
            </li>
          </ul>
        </aside>
 
        {/* Main content */}
        <main className="col-span-1 md:col-span-3">
          <Card className="p-4 sm:p-6 shadow-lg rounded-lg bg-white w-full md:w-11/12 lg:w-10/12">
            <CardContent>
              {activeTab === "general" ? (
                <div>
                  <div className="flex flex-col md:flex-row items-center md:items-start gap-4 md:gap-6">
                    <Avatar className="h-24 w-24">
                      <AvatarImage
                        src={imageUrl ? `data:image/png;base64,${imageUrl}` : "/default-avatar.png"}
                        alt="Profile Picture"
                      />
                      <AvatarFallback>{initials}</AvatarFallback>
                    </Avatar>
                    <div className="text-center md:text-left">
                      <h2 className="text-lg font-bold">
                        {firstName} {lastName} ({role})
                      </h2>
                      <p className="text-gray-600">{email}</p>
                    </div>
                  </div>
                  <Button variant="outline" className="mt-4 text-gray-700">
                    <Plus className="w-4 h-4 mr-2" />
                    Upload Photo
                  </Button>
 
                  <form className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="firstName">First Name</Label>
                      <Input
                        id="firstName"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className="mt-1 w-full"
                      />
                    </div>
                    <div>
                      <Label htmlFor="lastName">Last Name</Label>
                      <Input
                        id="lastName"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className="mt-1 w-full"
                      />
                    </div>
                    <div className="sm:col-span-2">
                      <Button className="bg-green-600 hover:bg-green-700 text-white w-full sm:w-auto">
                        Save Changes
                      </Button>
                    </div>
                  </form>
                </div>
              ) : (
                <div className="space-y-4">
                  <div>
                    <Label className="p-2">Old Password</Label>
                    <div className="relative">
                      <Input type="password" className="pr-10 w-full" />
                      <Eye className="absolute right-2 top-2.5 w-5 h-5 text-gray-500" />
                    </div>
                  </div>
 
                  <div>
                    <Label className="p-2">Password</Label>
                    <div className="relative">
                      <Input
                        type={passwordVisible ? "text" : "password"}
                        className="pr-10 w-full"
                      />
                      <button
                        type="button"
                        onClick={() => setPasswordVisible(!passwordVisible)}
                        className="absolute right-2 top-2.5"
                      >
                        {passwordVisible ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                      </button>
                    </div>
                    <ul className="text-sm text-green-600 list-disc list-inside mt-2 space-y-1">
                      <li>At least one uppercase letter required</li>
                      <li>At least one lowercase letter required</li>
                      <li>At least one number required</li>
                      <li>At least one character required</li>
                      <li>Password must be 8-16 characters long</li>
                      <li>New password should not match old password</li>
                    </ul>
                  </div>
 
                  <div>
                    <Label className="p-2">Confirm New Password</Label>
                    <div className="relative">
                      <Input
                        type={confirmVisible ? "text" : "password"}
                        className="pr-10 w-full"
                      />
                      <button
                        type="button"
                        onClick={() => setConfirmVisible(!confirmVisible)}
                        className="absolute right-2 top-2.5"
                      >
                        {confirmVisible ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                      </button>
                    </div>
                    <p className="text-sm text-green-600 mt-1">
                      Confirm password must match new password
                    </p>
                  </div>
 
                  <Button className="bg-green-600 hover:bg-green-700 text-white w-full sm:w-auto">
                    Save Changes
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </main>
      </div>
    </div>
  );
};
 
export default MyProfile;
 
 