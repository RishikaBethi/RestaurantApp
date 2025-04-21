import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FaClock } from "react-icons/fa";

interface Table {
  locationId: string;
  locationAddress: string | null;
  availableSlots: string[];
  tableNumber: string;
  capacity: string;
}

interface AvailableSlotsModalProps {
  isOpen: boolean;
  onClose: () => void;
  table: Table | null;
  date: string;
  onSlotClick: (slot: { fromTime: string; toTime: string },
  guests: number) => void;
}

export default function AvailableSlotsModal({ isOpen, onClose, table, date, onSlotClick }: AvailableSlotsModalProps) {
  if (!table) return null;
  const formatDateToWords = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };
  
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md p-4 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Available slots</DialogTitle>
          <p className="text-gray-600 mt-1 text-sm">
            There are <strong>{table.availableSlots.length} slots</strong> available at{" "}
            <strong>{table.locationAddress || "Unknown"}</strong>, Table{" "}
            {table.tableNumber}, for <strong>{formatDateToWords(date)}</strong>.
          </p>
        </DialogHeader>
        <div className="grid grid-cols-2 gap-2 mt-2">
        {table.availableSlots.map((slot, index) => {
  const [fromTime, toTime] = slot.split("-");
  return (
    <Button
      key={index}
      variant="outline"
      className="text-green-600 border-green-500 hover:text-green-600 hover:bg-green-100"
      onClick={() => {
        onSlotClick({ fromTime, toTime }, 1);
        onClose();
      }}
    >
      <FaClock className="text-green-600 ml-2" />
      {slot}
    </Button>
  );
})}
        </div>
      </DialogContent>
    </Dialog>
  );
}
