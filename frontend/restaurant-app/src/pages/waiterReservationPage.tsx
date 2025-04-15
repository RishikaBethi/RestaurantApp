import  { useEffect, useState } from "react";
import CreateReservationModal from "@/components/createReservationModal";
 
import {
  CalendarDays,
  Clock,
  MapPin,
  Search,
  User,
  Users,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { format } from "date-fns";
import axios from "axios";

const timeOptions = [
  "08:00 a.m.",
  "09:00 a.m.",
  "10:30 a.m.",
  "12:00 p.m.",
  "01:30 p.m.",
  "03:00 p.m.",
  "06:00 p.m.",
  "08:00 p.m.",
];
 
const GradientHeader = ({ name, role }: { name: string; role: string }) => (
  <div>
    <div className="bg-green-600 text-white p-6 flex items-center justify-between">
      <h2 className="text-xl font-semibold">
        Hello, {name} ({role})
      </h2>
    </div>
  </div>
);
 
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const ReservationCard = ({ reservation }: { reservation: any }) => (
  <div className="bg-white p-4 rounded-xl shadow-sm border w-full md:w-[300px]">
    <div className="flex justify-between items-center mb-2">
      <div className="flex items-center text-green-600 text-sm">
        <MapPin size={16} className="mr-1" /> {reservation.address}
      </div>
      <div className="text-sm text-gray-600">Table {reservation.tableNumber}</div>
    </div>
    <div className="text-sm text-gray-800 mb-1">
      <CalendarDays
        size={16}
        className="inline-block mr-1 text-green-600"
      />
      {format(new Date(reservation.date), "MMM dd, yyyy")}
    </div>
    <div className="text-sm text-gray-800 mb-1">
      <Clock size={16} className="inline-block mr-1 text-green-600" />
      {reservation.timeFrom} - {reservation.timeTo}
    </div>
    <div className="text-sm text-gray-800 mb-1">
      <User size={16} className="inline-block mr-1 text-green-600" />
      {reservation.name}
    </div>
    <div className="text-sm text-gray-800 mb-4">
      <Users size={16} className="inline-block mr-1 text-green-600" />
      {reservation.guestsNumber} Guests
    </div>
    <div className="flex justify-between items-center">
      <Button variant="ghost" className="text-sm underline">
        Cancel
      </Button>
      <Button
        variant="outline"
        className="border-green-600 text-green-600 hover:bg-green-50 text-sm"
      >
        Postpone
      </Button>
    </div>
  </div>
);
 
const WaiterReservations = () => {
  const [date, setDate] = useState("2024-10-12");
  const [time, setTime] = useState("10:30 a.m.");
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [table,] = useState("Any table");
  const [reservations, setReservations] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    const fetchReservations = async () => {
      const token = localStorage.getItem("token"); // Retrieve token from localStorage
      if (!token) return;

      try {
        const response = await axios.get(`https://9ey5ttv0oh.execute-api.ap-southeast-2.amazonaws.com/dev/reservations/waiter`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setReservations(response.data);
      } catch (error) {
        console.error("Failed to fetch reservations:", error);
      }
    };

    fetchReservations();
  }, []);
 
  return (
    <div className="min-h-screen bg-gray-50">
      <GradientHeader name="Alex Caper" role="Waiter" />
 
      <div className="max-w-7xl mx-auto px-6 pt-6">
        <div className="flex flex-wrap justify-center gap-4 mb-6">
          {/* Date Picker */}
          <Input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-[160px]"
          />
 
          {/* Time Dropdown */}
          <select
            value={time}
            onChange={(e) => setTime(e.target.value)}
            className="border border-gray-300 rounded-md p-2 text-sm"
          >
            {timeOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
 
          {/* Table Placeholder */}
          <Button variant="outline">{table}</Button>
 
          {/* Search Button */}
          <Button variant="outline" className="p-3 border-green-600 border-2 text-green-600 hover:bg-green-50">
            <Search size={18} />
          </Button>
        </div>
 
        <div className="flex justify-between items-center mb-6">
          <p className="text-l font-semibold text-gray-700">
            You have 6 reservations for{" "}
            {format(new Date(date), "MMM dd, yyyy")}, {time}
          </p>
          <Button
              className="bg-green-600 text-white hover:bg-green-700"
              onClick={() => setIsModalOpen(true)}
            >
              + Create New Reservation
            </Button>
            <CreateReservationModal open={isModalOpen} onClose={() => setIsModalOpen(false)} />
 
        </div>
 
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {reservations.map((r, idx) => (
            <ReservationCard key={idx} reservation={r} />
          ))}
        </div>
      </div>
    </div>
   
 
  );
};
 
export default WaiterReservations;
 
 