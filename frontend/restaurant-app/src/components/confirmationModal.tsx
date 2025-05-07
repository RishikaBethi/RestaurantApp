import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useCancelReservation } from "@/hooks/useCancelReservation";
import { useState } from "react";
import EditReservationDialog from "./editReservation";
import { toast } from "sonner";

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onReservationCancel?: () => void;
  bookingData: {
    date: string;
    timeSlot: string;
    guestsNumber: string;
    locationAddress: string;
    status: string;
    id: number;
  } | null;
}

export default function ConfirmationModal({ isOpen, onClose, bookingData, onReservationCancel }: ConfirmationModalProps) {
  const { cancelReservation, loading } = useCancelReservation();
  const [isEditOpen, setIsEditOpen] = useState(false);

  if (!bookingData) return null;
  const handleCancel = async () => {
    try {
      await cancelReservation(bookingData.id);
      toast.success("Reservation cancelled!");
      onReservationCancel?.(); // Optional callback to update parent state
      onClose();
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (err) {
      toast.error("Failed to cancel the reservation.");
    }
  };

  const handleEditOpen = () => {
    setIsEditOpen(true); // Open the EditReservationDialog
  };

  const handleEditClose = () => {
    setIsEditOpen(false); // Close the EditReservationDialog
  };

  const handleReservationUpdate = () => {
    setIsEditOpen(false); // Close the EditReservationDialog after updating
    toast.success("Reservation updated successfully.");
  };

  return (
    <>
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md p-6 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Reservation Confirmed!</DialogTitle>
        </DialogHeader>
        <p className="text-gray-700">
          Your table reservation at <strong>Green & Tasty</strong> for <strong>{bookingData.guestsNumber} people</strong>{" "}
          on <strong>{bookingData.date}</strong>, from <strong>{bookingData.timeSlot}</strong> has been successfully made.
        </p>
        <p className="text-gray-700">
          We look forward to welcoming you at <strong>{bookingData.locationAddress}</strong>.
        </p>
        <p className="text-gray-700 text-sm mt-2">
          Reservation Status: <strong>{bookingData.status}</strong>
        </p>
        <p className="text-gray-700 text-sm">
          If you need to modify or cancel your reservation, you can do so up to 30 minutes before the reservation time.
        </p>
        <div className="flex justify-between mt-1">
          <Button variant="outline" onClick={handleCancel} disabled={loading}>
            {loading ? "Cancelling..." : "Cancel Reservation"}
          </Button>
          <Button className="bg-green-600 hover:bg-green-700 text-white" onClick={handleEditOpen}>Edit Reservation</Button>
        </div>
      </DialogContent>
    </Dialog>

    {/* EditReservationDialog */}
    <EditReservationDialog
    isOpen={isEditOpen}
    reservation={{
      id: bookingData.id,
      locationAddress: bookingData.locationAddress,
      date: bookingData.date,
      timeSlot: bookingData.timeSlot,
      guestsNumber: parseInt(bookingData.guestsNumber),
    }}
    onClose={handleEditClose}
    onUpdate={handleReservationUpdate}
  />
  </>
  );
}
