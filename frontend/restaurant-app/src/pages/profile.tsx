import React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const MyProfile: React.FC = () => {
  const user=JSON.parse(localStorage.getItem("user")|| '""');
  const email=localStorage.getItem("email");
  const role=localStorage.getItem("role");
  const initials = user
  .split(" ")
  .map((word: string) => word.charAt(0))
  .join("");
  return (
    <div className="min-h-screen bg-gray-100">
        <h1 className="text-xl font-bold text-white bg-green-700 p-4 pl-9">My Profile</h1>
      {/* Main Content */}
      <div className="container mx-auto px-4 py-6 grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar */}
        <aside className="bg-transparent p-4 rounded-lg h-full">
          <ul className="space-y-2">
            <li>
              <a href="#" className="text-green-600 font-medium">General Information</a>
            </li>
            <li>
              <a href="#" className="text-gray-600 hover:text-green-600">Change Password</a>
            </li>
          </ul>
        </aside>

        {/* Profile Form */}
        <main className="col-span-1 md:col-span-3">
  <Card className="p-6 shadow-lg rounded-lg bg-white w-8/12">
    <CardContent>
      <div className="flex flex-col items-center md:items-start ">
        {/* Profile Section */}
        <div className="flex items-center gap-6">
          <Avatar className="h-24 w-24">
            <AvatarImage src="/default-avatar.png" alt="Profile Picture" />
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
          <div>
            <h2 className="text-lg font-bold">{user} ({role})</h2>
            <p className="text-gray-600">{email}</p>
          </div>
        </div>

        {/* Upload Photo Button */}
        <Button variant="outline" className="mt-4 text-gray-700">
          <span className="flex items-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-4 mr-2"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M12 4v16m8-8H4"
              />
            </svg>
            Upload Photo
          </span>
        </Button>
      </div>

      {/* Form Section */}
      <form className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <Label htmlFor="firstName">First Name</Label>
          <Input id="firstName" placeholder="e.g., Jonson" className="mt-1 w-10/12" />
        </div>
        <div>
          <Label htmlFor="lastName">Last Name</Label>
          <Input id="lastName" placeholder="e.g., Doe" className="mt-1 w-10/12" />
        </div>
        <div className="md:col-span-2">
          <Button className="bg-green-600 hover:bg-green-700 text-white">
            Save Changes
          </Button>
        </div>
      </form>
    </CardContent>
  </Card>
</main>

      </div>
    </div>
  );
};

export default MyProfile;
