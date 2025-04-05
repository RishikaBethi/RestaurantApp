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
}

export default function AvailableSlotsModal({ isOpen, onClose, table }: AvailableSlotsModalProps) {
  if (!table) return null;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md p-4 rounded-lg bg-white shadow-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Available slots</DialogTitle>
          <p className="text-gray-600 mt-1 text-sm">
            There are <strong>{table.availableSlots.length} slots</strong> available at{" "}
            <strong>{table.locationAddress || "Unknown"}</strong>, Table{" "}
            {table.tableNumber}, for <strong>October 14, 2024</strong>.
          </p>
        </DialogHeader>
        <div className="grid grid-cols-2 gap-2 mt-2">
          {table.availableSlots.map((slot, index) => (
            <Button key={index} variant="outline" className="text-green-600 border-green-500 hover:text-green-600 hover:bg-green-100">
              <FaClock className="text-green-600 ml-2" />
              {slot}
            </Button>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}
