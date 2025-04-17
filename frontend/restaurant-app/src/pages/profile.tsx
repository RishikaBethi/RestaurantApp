import React, { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Eye, EyeOff, Plus } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
 
const MyProfile: React.FC = () => {
  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role");
  const token = localStorage.getItem("token");
 
  const [profileData, setProfileData] = useState({
    firstName: "",
    lastName: "",
    imageUrl: "",
  });
 
  const [editedData, setEditedData] = useState({
    firstName: "",
    lastName: "",
    base64Image: "",
  });
 
  const [activeTab, setActiveTab] = useState("general");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
 
 
  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = (reader.result as string).split(",")[1];
        setEditedData((prev) => ({
          ...prev,
          base64Image: base64String,
        }));
      };
      reader.readAsDataURL(file);
    }
  };
 
  const fetchProfile = async () => {
    if (!token) {
      window.location.href = "/login";
      return;
    }
 
    try {
      const response = await axios.get(
        "https://lhvsg7xyl6.execute-api.ap-southeast-2.amazonaws.com/dev/users/profile",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
 
      const { firstName, lastName, imageUrl } = response.data;
 
      setProfileData({ firstName, lastName, imageUrl });
      setEditedData({ firstName, lastName, base64Image: imageUrl }); // initialize edits
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      if (error.response?.status === 401) {
        toast.error("Session expired. Please login again.");
        window.location.href = "/login";
      } else {
        toast.error("Error fetching profile");
      }
    }
  };
 
  useEffect(() => {
    fetchProfile();
  }, [token]);
 
  const handleSaveChanges = async () => {
    try {
      const response=await axios.put(
        "https://lhvsg7xyl6.execute-api.ap-southeast-2.amazonaws.com/dev/users/profile",
        {
          firstName: editedData.firstName,
          lastName: editedData.lastName,
          base64encodedImage: editedData.base64Image,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
 
      toast.success(response?.data?.message);
      fetchProfile(); // Refresh data
      const name=editedData.firstName+" "+editedData.lastName;
      localStorage.setItem("user", JSON.stringify(name));
    // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
    } catch (error: any) {
      toast.error("Failed to update profile.");
    }
  };
 
  const handlePasswordChange = async () => {
    if (!oldPassword || !newPassword || !confirmPassword) {
      toast.error("All password fields are required.");
      return;
    }
 
    if (newPassword !== confirmPassword) {
      toast.error("New password and confirmation do not match.");
      return;
    }
 
    try {
      await axios.put(
        "https://lhvsg7xyl6.execute-api.ap-southeast-2.amazonaws.com/dev/users/profile/password",
        {
          oldPassword,
          newPassword,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
 
      toast.success("Password changed successfully!");
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      toast.error(error.response?.data?.error || "Failed to change password.");
    }
  };
 
 
  const initials = `${profileData.firstName.charAt(0)}${profileData.lastName.charAt(0)}`;
 
  return (
    <div className="min-h-screen bg-gray-100">
      <h1 className="text-xl font-bold text-white bg-green-700 p-4 pl-6 sm:pl-9">
        My Profile
      </h1>
      <div className="container mx-auto px-4 py-6 grid grid-cols-1 md:grid-cols-4 gap-6">
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
 
        <main className="col-span-1 md:col-span-3">
          <Card className="p-4 sm:p-6 shadow-lg rounded-lg bg-white w-full md:w-11/12 lg:w-10/12">
            <CardContent>
              {activeTab === "general" ? (
                <div>
                  <div className="flex flex-col md:flex-row items-center md:items-start gap-4 md:gap-6">
                    <Avatar className="h-24 w-24">
                      <AvatarImage
                        src={
                          editedData.base64Image
                            ? `data:image/png;base64,${editedData.base64Image}`
                            : "/default-avatar.png"
                        }
                        alt="Profile Picture"
                      />
                      <AvatarFallback>{initials}</AvatarFallback>
                    </Avatar>
                    <div className="text-center md:text-left">
                      <h2 className="text-lg font-bold">
                        {profileData.firstName} {profileData.lastName} ({role})
                      </h2>
                      <p className="text-gray-600">{email}</p>
                    </div>
                  </div>
 
                  <div className="mt-4">
                    <label className="cursor-pointer inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
                      <Plus className="w-4 h-4 mr-2" />
                      Upload Photo
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                        className="hidden"
                      />
                    </label>
                  </div>
 
                  <form className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="firstName">First Name</Label>
                      <Input
                        id="firstName"
                        value={editedData.firstName}
                        onChange={(e) =>
                          setEditedData((prev) => ({
                            ...prev,
                            firstName: e.target.value,
                          }))
                        }
                        className="mt-1 w-full"
                      />
                    </div>
                    <div>
                      <Label htmlFor="lastName">Last Name</Label>
                      <Input
                        id="lastName"
                        value={editedData.lastName}
                        onChange={(e) =>
                          setEditedData((prev) => ({
                            ...prev,
                            lastName: e.target.value,
                          }))
                        }
                        className="mt-1 w-full"
                      />
                    </div>
                    <div className="sm:col-span-2">
                      <Button
                        onClick={(e) => {
                          e.preventDefault();
                          handleSaveChanges();
                        }}
                        className="bg-green-600 hover:bg-green-700 text-white w-full sm:w-auto"
                      >
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
                    <Input
  type="password"
  value={oldPassword}
  onChange={(e) => setOldPassword(e.target.value)}
  className="pr-10 w-full"
/>
 
                      <Eye className="absolute right-2 top-2.5 w-5 h-5 text-gray-500" />
                    </div>
                  </div>
 
                  <div>
                    <Label className="p-2">New Password</Label>
                    <div className="relative">
                    <Input
  type={passwordVisible ? "text" : "password"}
  value={newPassword}
  onChange={(e) => setNewPassword(e.target.value)}
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
                    <ul className="text-sm list-disc list-inside mt-2 space-y-1">
  <li className={/[A-Z]/.test(newPassword) ? "text-green-600" : "text-red-600"}>
    At least one uppercase letter required
  </li>
  <li className={/[a-z]/.test(newPassword) ? "text-green-600" : "text-red-600"}>
    At least one lowercase letter required
  </li>
  <li className={/\d/.test(newPassword) ? "text-green-600" : "text-red-600"}>
    At least one number required
  </li>
  <li className={/[!@#$%^&*(),.?":{}|<>]/.test(newPassword) ? "text-green-600" : "text-red-600"}>
    At least one special character required
  </li>
  <li className={newPassword.length >= 8 && newPassword.length <= 16 ? "text-green-600" : "text-red-600"}>
    Password must be 8-16 characters long
  </li>
  <li className={oldPassword && newPassword !== oldPassword ? "text-green-600" : "text-red-600"}>
    New password should not match old password
  </li>
</ul>
 
                  </div>
 
                  <div>
                    <Label className="p-2">Confirm New Password</Label>
                    <div className="relative">
                    <Input
  type={confirmVisible ? "text" : "password"}
  value={confirmPassword}
  onChange={(e) => setConfirmPassword(e.target.value)}
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
 
                  <Button
  onClick={(e) => {
    e.preventDefault();
    handlePasswordChange();
  }}
  className="bg-green-600 hover:bg-green-700 text-white w-full sm:w-auto"
>
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
 