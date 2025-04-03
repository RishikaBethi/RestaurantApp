import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  reservationDetails: {
    guests: number;
    table: number;
    address: string;
    fromTime: string | null;
    toTime: string | null;
  };
}

export default function ConfirmationModal({ isOpen, onClose, reservationDetails }: ConfirmationModalProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md p-6 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Reservation Confirmed!</DialogTitle>
        </DialogHeader>
        <p className="text-gray-700">
          Your table reservation at <strong>Green & Tasty</strong> for <strong>{reservationDetails.guests} people</strong>{" "}
          on <strong>Oct 14, 2024</strong>, from <strong>{reservationDetails.fromTime}</strong> to <strong>{reservationDetails.toTime}</strong> at{" "} <strong>Table {reservationDetails.table}</strong> has been successfully made.
        </p>
        <p className="text-gray-700">
          We look forward to welcoming you at <strong>{reservationDetails.address}</strong>.
        </p>
        <p className="text-gray-700 text-sm">
          If you need to modify or cancel your reservation, you can do so up to 30 minutes before the reservation time.
        </p>

        <div className="flex justify-between mt-1">
          <Button className=" text-green-600 border border-green-600 bg-white hover:bg-white" onClick={onClose}>
            Cancel Reservation
          </Button>
          <Button className="bg-green-600 hover:bg-green-700 text-white">Edit Reservation</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
