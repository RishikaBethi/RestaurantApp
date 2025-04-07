import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, MapPin, Users } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useCancelReservation } from "@/hooks/useCancelReservation";
import { toast } from "sonner";

interface Reservation {
  id: number;
  locationAddress: string;
  date: string;
  timeSlot: string;
  guestsNumber: number;
  status: "Reserved" | "In Progress" | "Finished" | "Canceled";
}

const statusColors: Record<string, string> = {
  Reserved: "bg-yellow-100 text-yellow-600",
  "In Progress": "bg-blue-100 text-blue-600",
  Finished: "bg-green-100 text-green-600",
  Cancelled: "bg-red-100 text-red-600",
};

export default function ReservationsPage() {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const isAuthenticated = Boolean(localStorage.getItem("user")); // Mock authentication check
  const user=JSON.parse(localStorage.getItem("user")|| '""');
  const role=localStorage.getItem("role");
  const { cancelReservation, loading: cancelling } = useCancelReservation();

  const handleCancelReservation = async (id: number) => {
    try {
      const res = await cancelReservation(id);
      const message = res?.data?.message || "Reservation cancelled!";
      toast.success(message);
      fetchReservations(); // Refresh the list
    // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
    } catch (error:any) {
      const errorMsg = error?.response?.data?.message || "Failed to cancel the reservation.";
      toast.error(errorMsg);
    }
  };
  
  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    } else {
      fetchReservations();
    }
  }, [isAuthenticated, navigate]);

  const fetchReservations = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await axios.get<Reservation[]>("https://6txdnfn1ga.execute-api.ap-southeast-2.amazonaws.com/dev/reservations",
        {
          headers: {
            Authorization: `Bearer ${token}`, 
          },}
      );
      setReservations(response.data);
    } catch (error) {
      console.error("Failed to fetch reservations:", error);
    }
  };

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
                  <span className="font-semibold">{res.locationAddress}</span>
                </div>
                <span className={`px-3 py-1 text-sm rounded-full ${statusColors[res.status]}`}>{res.status}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Calendar className="w-5 h-5 text-green-600" />
                <span>{res.date}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Clock className="w-5 h-5 text-green-600" />
                <span>{res.timeSlot}</span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <Users className="w-5 h-5 text-green-600" />
                <span>{res.guestsNumber} Guests</span>
              </div>
              {res.status === "Reserved" && (
                <div className="flex justify-between mt-4">
                  <Button variant="outline" onClick={() => handleCancelReservation(res.id)} disabled={cancelling}>
                    {cancelling ? "Cancelling..." : "Cancel"}
                  </Button>
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
