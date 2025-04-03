import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FaUser, FaClock } from "react-icons/fa";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import ConfirmationModal from "./confirmationModal";

interface ReservationModalProps {
  isOpen: boolean;
  onClose: () => void;
  table: {
    id: number;
    address: string;
    capacity: number;
    slots: string[];
  } | null;
}

const extractStartTime = (slot: string) => slot.split(" - ")[0];

export default function ReservationModal({ isOpen, onClose, table }: ReservationModalProps) {
  const [guests, setGuests] = useState(1);
  const [fromTime, setFromTime] = useState<string | null>(null);
  const [toTime, setToTime] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!table) return null;
  const availableTimes = Array.from(new Set(table.slots.map(extractStartTime)));

  // Handle reservation confirmation
  const handleReservation = () => {
     // Validation: Check if all required fields are selected
     if (!fromTime || !toTime) {
      setError("Please select both 'From' and 'To' time.");
      return;
    }
    if (guests < 1) {
      setError("Please select the number of guests.");
      return;
    }
    // Clear error if all fields are valid
    setError(null);
    onClose(); // Close the Reservation Modal
    setTimeout(() => setShowConfirmation(true), 100); // Delay to ensure smooth transition
  };
  return (
    <>
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md pl-4 pr-4 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Make a Reservation</DialogTitle>
          <p className="text-gray-600 mt-1 text-sm">
            You are making a reservation at <strong>{table.address}</strong>, Table {table.id}, <br />
            for <strong>October 14, 2024</strong>.
          </p>
        </DialogHeader>

        {/* Guests Selector */}
        <div className="mt-1">
          <h1 className="font-semibold text-sm">Guests</h1>
          <p className="text-sm text-gray-600">Please specify the number of guests.</p>
          <p className="text-sm text-gray-600 pb-2">Table seating capacity: 10 people</p>
          <div className="flex items-center justify-between border border-gray-300 p-1 rounded-lg mt-1">
            <div className="flex items-center gap-2">
              <FaUser className="text-green-600" />
              <span className="font-medium text-gray-700">Guests</span>
            </div>
            <div className="flex items-center gap-2">
              <button
                className="px-1 border border-green-600 rounded text-lg text-green-600"
                onClick={() => setGuests((prev) => Math.max(1, prev - 1))}
              >
                -
              </button>
              <span className="font-semibold text-green-600">{guests}</span>
              <button
                className="px-1 border border-green-600 rounded text-lg text-green-600"
                onClick={() => setGuests((prev) => Math.min(table.capacity, prev + 1))}
              >
                +
              </button>
            </div>
          </div>
        </div>

        {/* Time Selector */}
        <div className="mt-1">
          <h1 className="font-semibold text-sm">Time</h1>
          <p className="text-sm text-gray-600">Please choose your preferred time from the dropdowns below</p>
          <div className="flex items-center justify-between mt-2">
            {/* From Time */}
            <div className="w-1/2">
              <p className="text-xs text-gray-500 mb-1">From</p>
              <Select onValueChange={setFromTime}>
                <SelectTrigger className="w-full border-gray-300">
                  <FaClock className="text-green-600 ml-2" />
                  <SelectValue placeholder="Select time" />
                </SelectTrigger>
                <SelectContent>
                  {availableTimes.map((time, index) => (
                    <SelectItem key={index} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="w-1/2 pl-2">
              <p className="text-xs text-gray-500 mb-1">To</p>
              <Select onValueChange={setToTime}>
                <SelectTrigger className="w-full border-gray-300">
                  <FaClock className="text-green-600 ml-2" />
                  <SelectValue placeholder="Select time" />
                </SelectTrigger>
                <SelectContent>
                  {availableTimes.map((time, index) => (
                    <SelectItem key={index} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && <p className="text-red-500 text-sm mt-2">{error}</p>}

        {/* Confirm Button */}
        <Button className="w-full mt-2 bg-green-600 hover:bg-green-700 text-white text-lg py-2"
        onClick={handleReservation} disabled={!fromTime || !toTime}>
          Make a Reservation
        </Button>
      </DialogContent>
    </Dialog>

    {/* Confirmation Modal */}
    <ConfirmationModal
        isOpen={showConfirmation}
        onClose={() => setShowConfirmation(false)}
        reservationDetails={{
          guests,
          table: table.id,
          address: table.address,
          fromTime: fromTime,
          toTime: toTime
        }}
      />
    </>
  );
}
