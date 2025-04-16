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
import {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
  AlertDialogDescription,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { format } from "date-fns";
import axios from "axios";

const timeOptions = [
  "10:30",
  "12:15",
  "14:00",
  "15:45",
  "17:30",
  "19:15",
  "21:00",
];

type Reservation = {
  id: string;
  date: string;
  timeFrom: string;
  timeTo: string;
  address: string;
  name: string;
  guestsNumber: string;
  tableNumber: string;
};

const GradientHeader = ({ name, role }: { name: string | null; role: string | null }) => (
  <div>
    <div className="bg-green-600 text-white p-6 flex items-center justify-between">
      <h2 className="text-xl font-semibold">
        Hello, {name} ({role})
      </h2>
    </div>
  </div>
);
 
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const ReservationCard = ({ reservation,onCancel }: { reservation: Reservation;onCancel:(id:string)=>void; }) => (
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
    <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button variant="ghost" className="text-sm underline">
            Cancel
          </Button>
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel Reservation?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to cancel this reservation? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>No</AlertDialogCancel>
            <AlertDialogAction onClick={()=>onCancel(reservation.id)}>Yes, Cancel</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
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
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [table,setTable] = useState("");
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [allReservations, setAllReservations] = useState<Reservation[]>([]);

  const userName = JSON.parse(localStorage.getItem("user") || "");
  const role=localStorage.getItem("role");

  const fetchReservations = async () => {
    const token = localStorage.getItem("token"); // Retrieve token from localStorage
    if (!token) return;

    try {
      const response = await axios.get(`https://9ey5ttv0oh.execute-api.ap-southeast-2.amazonaws.com/dev/reservations/waiter`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      setReservations(response.data.map((r: any) => ({
        ...r,
        id: r.reservationId,
      })));
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const mapped = response.data.map((r: any) => ({
        ...r,
        id: r.reservationId,
      }));
      setAllReservations(mapped);
      setReservations(mapped);      
    } catch (error) {
      console.error("Failed to fetch reservations:", error);
    }
  };

  useEffect(() => {
    fetchReservations();
  }, []);

  const handleCancel = async (id: string) => {
    const token = localStorage.getItem("token");
    if (!token) return;
  
    try {
      await axios.delete(`https://9ey5ttv0oh.execute-api.ap-southeast-2.amazonaws.com/dev/reservations/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
  
      setReservations((prev) => prev.filter((r) => r.id !== id));
      toast.success("Reservation cancelled successfully");
    } catch (error) {
      console.error("Failed to cancel reservation:", error);
      toast.error("Failed to cancel reservation.");
    }
  };
  
  const handleSearch = () => {
    const filtered = allReservations.filter((res) => {
      const matchesDate = date ? res.date === date : true;
      const matchesTime = time ? res.timeFrom === time : true;
      const matchesTable = table && table !== "Table" ? res.tableNumber === table.split(" ")[1] : true;
  
      return matchesDate && matchesTime && matchesTable;
    });
  
    setReservations(filtered);
  };  
 
  return (
    <div className="min-h-screen bg-gray-50">
      <GradientHeader name={userName} role={role} />
 
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
          <select
  value={table}
  onChange={(e) => setTable(e.target.value)}
  className="border border-gray-300 rounded-md p-2 text-sm"
>
  <option value="Table">Table</option>
  <option value="Table 1">Table 1</option>
  <option value="Table 2">Table 2</option>
  <option value="Table 3">Table 3</option>
</select>
 
          {/* Search Button */}
          <Button
  variant="outline"
  className="p-3 border-green-600 border-2 text-green-600 hover:bg-green-50"
  onClick={handleSearch}
>
  <Search size={18} />
</Button>
        </div>
 
        <div className="flex justify-between items-center mb-6">
          <p className="text-l font-semibold text-gray-700">
            You have {reservations.length} reservations for{" "}
            {format(new Date(), "MMM dd, yyyy")}
          </p>
          <Button
              className="bg-green-600 text-white hover:bg-green-700"
              onClick={() => setIsModalOpen(true)}
            >
              + Create New Reservation
            </Button>
            <CreateReservationModal open={isModalOpen} 
            onClose={() =>  {
              setIsModalOpen(false);
              fetchReservations(); // Refresh after closing
            }}
            onReservationSuccess={fetchReservations} />
        </div>
 
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {reservations.map((r) => (
            <ReservationCard key={r.id} reservation={r} onCancel={handleCancel}/>
          ))}
        </div>
      </div>
    </div>
   
 
  );
};
 
export default WaiterReservations;
 
 