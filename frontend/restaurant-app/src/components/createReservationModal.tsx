import React, { useState } from "react";
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
 
interface Props {
  open: boolean;
  onClose: () => void;
}

const CreateReservationModal: React.FC<Props> = ({ open, onClose }) => {
  const [location, setLocation] = useState("");
  const [customerType, setCustomerType] = useState("visitor");
  const [guests, setGuests] = useState(1);
  const [fromTime, setFromTime] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [toTime, setToTime] = useState("");
  const [table, setTable] = useState("");
  // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
  const [reservationResponse, setReservationResponse] = useState<any>(null);
 
  const handleIncrement = () => setGuests((prev) => prev + 1);
  const handleDecrement = () => setGuests((prev) => (prev > 1 ? prev - 1 : 1));
 
  const handleSubmit = async () => {
    const token = localStorage.getItem("token");
    const email=localStorage.getItem("email");
    const requestData = {
      clientType: customerType === "visitor" ? "VISITOR" : "EXISTING_CUSTOMER",
      customerEmail: email,
      date: "2025-12-12", // Replace with a dynamic date input if needed
      guestsNumber: guests.toString(),
      locationId: location, // Ensure location is mapped to locationId
      tableNumber: table.replace("Table ", ""),
      timeFrom: fromTime,
      timeTo: toTime,
    };

    try {
      const response = await axios.post("https://9ey5ttv0oh.execute-api.ap-southeast-2.amazonaws.com/dev/bookings/waiter", requestData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setReservationResponse(response.data); // Save the response to state
      console.log("Reservation created successfully:", response.data);
      onClose(); // Close modal after successful reservation
    } catch (error) {
      console.error("Error creating reservation:", error);
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
          <Select onValueChange={setLocation}>
            <SelectTrigger>
              <SelectValue placeholder="Select a location" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="LOC002">14 Baratashvili Street</SelectItem>
              <SelectItem value="LOC001">48 Rustaveli Avenue</SelectItem>
              <SelectItem value="LOC003">9 Abashidze Street</SelectItem>
            </SelectContent>
          </Select>
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
                  <SelectItem value="12:15">12:15 p.m.</SelectItem>
                  <SelectItem value="1:00">1:00 p.m.</SelectItem>
                  <SelectItem value="1:30">1:30 p.m.</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1">
              <Label className="flex items-center gap-1 text-sm"><Clock size={16} /> To</Label>
              <Select onValueChange={setToTime}>
                <SelectTrigger>
                  <SelectValue placeholder="To" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1:45">1:45 p.m.</SelectItem>
                  <SelectItem value="2:00">2:00 p.m.</SelectItem>
                  <SelectItem value="2:30">2:30 p.m.</SelectItem>
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
 
 