import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, MapPin, Users } from "lucide-react";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

interface Reservation {
  id: number;
  location: string;
  date: string;
  time: string;
  guests: number;
  status: "Reserved" | "In Progress" | "Finished" | "Canceled";
}

const reservations: Reservation[] = [
  {
    id: 1,
    location: "48 Rustaveli Avenue",
    date: "Oct 14, 2024",
    time: "12:15 p.m. - 1:45 p.m.",
    guests: 10,
    status: "Reserved",
  },
  {
    id: 2,
    location: "14 Baratashvili Street",
    date: "Oct 16, 2024",
    time: "10:30 a.m. - 12:00 p.m.",
    guests: 10,
    status: "Reserved",
  },
  {
    id: 3,
    location: "14 Baratashvili Street",
    date: "Sep 14, 2024",
    time: "10:30 a.m. - 11:30 a.m.",
    guests: 5,
    status: "In Progress",
  },
  {
    id: 4,
    location: "14 Baratashvili Street",
    date: "Jun 6, 2024",
    time: "10:30 a.m. - 11:30 a.m.",
    guests: 4,
    status: "Finished",
  },
  {
    id: 5,
    location: "14 Baratashvili Street",
    date: "Mar 28, 2024",
    time: "10:30 a.m. - 11:30 a.m.",
    guests: 2,
    status: "Canceled",
  },
];

const statusColors: Record<string, string> = {
  Reserved: "bg-yellow-100 text-yellow-600",
  "In Progress": "bg-blue-100 text-blue-600",
  Finished: "bg-green-100 text-green-600",
  Canceled: "bg-red-100 text-red-600",
};

export default function ReservationsPage() {
  const navigate = useNavigate();
  const isAuthenticated = Boolean(localStorage.getItem("user")); // Mock authentication check
  const user=JSON.parse(localStorage.getItem("user")|| '""');
  const role=localStorage.getItem("role");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated) return null; // Prevent rendering if not authenticated

  return (
    <div><h1 className="text-2xl font-bold text-white bg-green-700 p-4 pl-9">Hello, {user} ({role})</h1>
    <div className="container mx-auto p-2">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
        {reservations.map((res) => (
          <Card key={res.id} className="p-4 shadow-lg">
            <CardContent className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-gray-700">
                  <MapPin className="w-5 h-5 text-green-600" />
                  <span className="font-semibold">{res.location}</span>
                </div>
                <span className={`px-3 py-1 text-sm rounded-full ${statusColors[res.status]}`}>{res.status}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Calendar className="w-5 h-5 text-green-600" />
                <span>{res.date}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Clock className="w-5 h-5 text-green-600" />
                <span>{res.time}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Users className="w-5 h-5 text-green-600" />
                <span>{res.guests} Guests</span>
              </div>
              {res.status === "Reserved" && (
                <div className="flex justify-between mt-4">
                  <Button variant="outline">Cancel</Button>
                  <Button className="bg-green-600 hover:bg-green-700">Edit</Button>
                </div>
              )}
              {res.status === "In Progress" && (
                <Button className="bg-green-600 hover:bg-green-700 w-full">Leave Feedback</Button>
              )}
              {res.status === "Finished" && (
                <Button className="bg-green-600 hover:bg-green-700 w-full">Update Feedback</Button>
              )}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
    </div>
  );
}
