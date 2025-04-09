import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FaUser, FaClock } from "react-icons/fa";
import ConfirmationModal from "./confirmationModal";
import axios from "axios";
import { BASE_API_URL } from "@/constants/constant";
import { toast } from "sonner";

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
  const [, setError] = useState<string | null>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [bookingData, setBookingData] = useState<any | null>(null);

  const getTodayDate = (): string => {
    const today = new Date();
    const year = today.getFullYear();
    const month = `${today.getMonth() + 1}`.padStart(2, '0');
    const day = `${today.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  };  

  const formatDateToWords = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };  

  if (!table) return null;

  // Handle reservation confirmation
  const handleReservation = async() => {
     // Validation: Check if all required fields are selected
     if (!fromTime || !toTime) {
      setError("Please select both 'From' and 'To' time.");
      toast.error("Please select both 'From' and 'To' time.");
      return;
    }
    if (guests < 1) {
      setError("Please select the number of guests.");
      toast.error("Please select the number of guests.");
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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      const errorMessage = err?.response?.data?.error || "Something went wrong!";
      setError(errorMessage);
      toast.error(errorMessage);
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
            for <strong>{formatDateToWords(selectedDate || getTodayDate())}</strong>.
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
                <select
          className="w-full outline-none"
          value={fromTime}
          onChange={(e) => {
            setFromTime(e.target.value);
            setToTime(""); // reset To when From changes
          }}
        >
          <option value="">Select</option>
          {table.availableSlots.map((slot, index) => {
            const from = slot.split("-")[0];
            return (
              <option key={index} value={from}>
                {from}
              </option>
            );
          })}
        </select>
              </div>
              </div>
            </div>

            <div className="w-1/2 pl-2">
              <p className="text-xs text-gray-500 mb-1">To</p>
              <div className="flex items-center border rounded-lg p-2 gap-2 bg-white">
                <FaClock className="text-gray-500" />
                <select
          className="w-full outline-none"
          value={toTime}
          onChange={(e) => setToTime(e.target.value)}
          disabled={!fromTime}
        >
          <option value="">Select</option>
          {table.availableSlots
            .filter((slot) => slot.startsWith(fromTime))
            .map((slot, index) => {
              const to = slot.split("-")[1];
              return (
                <option key={index} value={to}>
                  {to}
                </option>
              );
            })}
        </select>
              </div>
            </div>
        </div>

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
