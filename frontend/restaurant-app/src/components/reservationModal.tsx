import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FaUser, FaClock } from "react-icons/fa";
import ConfirmationModal from "./confirmationModal";
import axios from "axios";
import { BASE_API_URL } from "@/constants/constant";

interface Table {
  locationId: string;
  locationAddress: string | null;
  availableSlots: string[];
  tableNumber: string;
  capacity: string;
}

interface ReservationModalProps {
  isOpen: boolean;
  onClose: () => void;
  table: Table | null;
  selectedDate: string;
}

export default function ReservationModal({ isOpen, onClose, table, selectedDate }: ReservationModalProps) {
  const [guests, setGuests] = useState(1);
  const [fromTime, setFromTime] = useState("");
  const [toTime, setToTime] = useState("");
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [bookingData, setBookingData] = useState<any | null>(null);

  const getTodayDate = (): string => {
    const today = new Date();
    const year = today.getFullYear();
    const month = `${today.getMonth() + 1}`.padStart(2, '0');
    const day = `${today.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  };  

  if (!table) return null;

  // Handle reservation confirmation
  const handleReservation = async() => {
     // Validation: Check if all required fields are selected
     if (!fromTime || !toTime) {
      setError("Please select both 'From' and 'To' time.");
      return;
    }
    if (guests < 1) {
      setError("Please select the number of guests.");
      return;
    }

    const token = localStorage.getItem("token");

    // Clear error if all fields are valid
    try {
      const response = await axios.post(`${BASE_API_URL}/bookings/client`, {
        locationId: table.locationId,
        tableNumber: table.tableNumber,
        date: selectedDate || getTodayDate(),
        guestsNumber: guests.toString(),
        timeFrom: fromTime,
        timeTo: toTime,
      },{
        headers: {
          Authorization: `Bearer ${token}`, 
        },});

      setBookingData(response.data);
      onClose();
      setTimeout(() => setShowConfirmation(true), 100);
    } catch (err) {
      console.error("Error making reservation:", err);
      setError("Failed to make reservation. Please try again or Login to make a reservation");
    }
  };
  return (
    <>
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md pl-4 pr-4 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Make a Reservation</DialogTitle>
          <p className="text-gray-600 mt-1 text-sm">
            You are making a reservation at <strong>{table.locationAddress}</strong>, Table {table.tableNumber}, <br />
            for <strong>{selectedDate}</strong>.
          </p>
        </DialogHeader>

        {/* Guests Selector */}
        <div className="mt-1">
          <h1 className="font-semibold text-sm">Guests</h1>
          <p className="text-sm text-gray-600">Please specify the number of guests.</p>
          <p className="text-sm text-gray-600 pb-2">Table seating capacity: {table.capacity} people</p>
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
                onClick={() => setGuests((prev) => Math.min(parseInt(table.capacity), prev + 1))}
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
              <div className="flex items-center border rounded-lg p-2 gap-2 bg-white">
                <FaClock className="text-gray-500" />
                <input
                  type="time"
                  className="w-full outline-none"
                  value={fromTime}
                  onChange={(e) => {
                    setFromTime(e.target.value);
                    setToTime(""); // reset toTime
                  }}
                />
              </div>
              </div>
            </div>

            <div className="w-1/2 pl-2">
              <p className="text-xs text-gray-500 mb-1">To</p>
              <div className="flex items-center border rounded-lg p-2 gap-2 bg-white">
                <FaClock className="text-gray-500" />
                <input
                  type="time"
                  className="w-full outline-none"
                  value={toTime}
                  onChange={(e) => setToTime(e.target.value)}
                  disabled={!fromTime}
                />
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
        bookingData={bookingData}
      />
    </>
  );
}
