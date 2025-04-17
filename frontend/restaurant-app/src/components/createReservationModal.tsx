import React, { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Clock, MapPin, User } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
 
interface Props {
  open: boolean;
  onClose: () => void;
  onReservationSuccess:()=>void;
  address:string ;
}

const CreateReservationModal: React.FC<Props> = ({ open, onClose,onReservationSuccess,address }) => {
  const [location, setLocation] = useState("");
  const [customerType, setCustomerType] = useState("visitor");
  const [guests, setGuests] = useState(1);
  const [fromTime, setFromTime] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [toTime, setToTime] = useState("");
  const [table, setTable] = useState("");
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [, setReservationResponse] = useState<any>(null);

  const fromTimeOptions = ["10:30", "12:15", "14:00", "15:45", "17:30", "19:15", "21:00"];
  const [toTimeOptions, setToTimeOptions] = useState<string[]>([]);
 
  const handleIncrement = () => setGuests((prev) => prev + 1);
  const handleDecrement = () => setGuests((prev) => (prev > 1 ? prev - 1 : 1));

   // Convert "HH:mm" to a Date object
   const parseTime = (timeStr: string): Date => {
    const [hours, minutes] = timeStr.split(":").map(Number);
    const date = new Date();
    date.setHours(hours, minutes, 0, 0);
    return date;
  };

  // Format Date to "HH:mm"
  const formatTime = (date: Date): string => {
    return date.toTimeString().slice(0, 5);
  };

  useEffect(() => {
    if (fromTime) {
      const from = parseTime(fromTime);
      const to = new Date(from.getTime() + 90 * 60 * 1000); // 90 minutes ahead
      const formatted = formatTime(to);
      setToTime(formatted);
      setToTimeOptions([formatted]);
    } else {
      setToTime("");
      setToTimeOptions([]);
    }
  }, [fromTime]);

  const addressToLocationIdMap: Record<string, string> = {
    "14 Baratashvili Street": "LOC002",
    "48 Rustaveli Avenue": "LOC001",
    "9 Abashidze Street": "LOC003",
  };
  
  useEffect(() => {
    const id = addressToLocationIdMap[address];
    if (id) {
      setLocation(id);
    }
  }, [address]);  

  const handleSubmit = async () => {
    const token = localStorage.getItem("token");
    const email=localStorage.getItem("email");
    const requestData = {
      clientType: customerType === "visitor" ? "VISITOR" : "EXISTING_CUSTOMER",
      customerEmail: email,
      date: new Date().toISOString().split("T")[0],
      guestsNumber: guests.toString(),
      locationId: location,
      tableNumber: table.replace("Table ", ""),
      timeFrom: fromTime,
      timeTo: toTime,
    };

    try {
      const response = await axios.post("https://dtspspshhf.execute-api.ap-southeast-2.amazonaws.com/dev/bookings/waiter", requestData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setReservationResponse(response.data); // Save the response to state
      toast.success("Reservation made successfully!");
      onReservationSuccess();
      onClose(); // Close modal after successful reservation
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error:any) {
      console.error("Error creating reservation:", error);
      const message = error?.response?.data?.error || "Something went wrong.";
      toast.error(`${message}`);
    }
  };
 
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md rounded-xl px-6 py-4">
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold">New Reservation</DialogTitle>
        </DialogHeader>
 
        {/* Location */}
        <div className="space-y-1">
        <Label className="flex items-center gap-2 text-sm">
    <MapPin size={16} /> Location
  </Label>
  <Input value={address} readOnly className="cursor-default bg-gray-100" />
</div>

        {/* Customer Type */}
        <RadioGroup value={customerType} onValueChange={setCustomerType} className="flex gap-3">
          <div className="flex items-center space-x-2 border rounded-md px-4 py-2 w-full cursor-pointer" onClick={() => setCustomerType("visitor")}>
            <RadioGroupItem value="visitor" id="visitor" />
            <Label htmlFor="visitor">Visitor</Label>
          </div>
          <div className="flex items-center space-x-2 border rounded-md px-4 py-2 w-full cursor-pointer" onClick={() => setCustomerType("existing")}>
            <RadioGroupItem value="existing" id="existing" />
            <Label htmlFor="existing">Existing Customer</Label>
          </div>
        </RadioGroup>
        {customerType === "existing" && (
  <div className="space-y-1">
    <Label className="text-sm">Customer's Name</Label>
    <Input
      placeholder="e.g. Janson Doe"
      value={customerName}
      onChange={(e) => setCustomerName(e.target.value)}
    />
  </div>
)}
        {/* Guests */}
        <div className="flex items-center justify-between border rounded-md px-4 py-2">
          <Label className="flex items-center gap-2 text-sm">
            <User size={16} /> Guests
          </Label>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" onClick={handleDecrement}>-</Button>
            <span>{guests}</span>
            <Button variant="outline" size="icon" onClick={handleIncrement}>+</Button>
          </div>
        </div>
 
        {/* Time */}
        <div className="space-y-1">
          <p className="text-sm text-muted-foreground">Please choose your preferred time from the dropdowns below</p>
          <div className="flex gap-2">
            <div className="flex-1">
              <Label className="flex items-center gap-1 text-sm"><Clock size={16} /> From</Label>
              <Select onValueChange={setFromTime}>
                <SelectTrigger>
                  <SelectValue placeholder="From" />
                </SelectTrigger>
                <SelectContent>
                {fromTimeOptions.map((time) => (
                    <SelectItem key={time} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1">
              <Label className="flex items-center gap-1 text-sm"><Clock size={16} /> To</Label>
              <Select onValueChange={setToTime} value={toTime}>
                <SelectTrigger>
                  <SelectValue placeholder="To" />
                </SelectTrigger>
                <SelectContent>
                {toTimeOptions.map((time) => (
                    <SelectItem key={time} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </div>
 
        {/* Table */}
        <div>
          <Label className="text-sm">Table</Label>
          <Select onValueChange={setTable}>
            <SelectTrigger>
              <SelectValue placeholder="Select a table" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="Table 1">Table 1</SelectItem>
              <SelectItem value="Table 2">Table 2</SelectItem>
              <SelectItem value="Table 3">Table 3</SelectItem>
            </SelectContent>
          </Select>
        </div>
 
        {/* Submit Button */}
        <Button className="w-full mt-4 bg-green-600 hover:bg-green-700 text-white" onClick={handleSubmit}>
          Make a Reservation
        </Button>
      </DialogContent>
    </Dialog>
  );
};
 
export default CreateReservationModal;
 
 