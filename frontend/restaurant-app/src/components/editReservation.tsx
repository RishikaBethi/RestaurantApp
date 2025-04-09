import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import axios from "axios";
import { Input } from "./ui/input";
import { BASE_API_URL } from "@/constants/constant";

interface Reservation {
  id: number;
  locationAddress: string;
  date: string;
  timeSlot: string;
  guestsNumber: number;
}

interface EditReservationDialogProps {
  isOpen: boolean;
  reservation: Reservation | null;
  onClose: () => void;
  onUpdate: () => void;
}

const availableTimes = [
  "10:30",
  "12:15",
  "14:00",
  "15:45",
  "17:30",
  "19:15",
  "21:00",
];

const calculateToTimes = (fromTime: string): string[] => {
  const [fromHours, fromMinutes] = fromTime.split(":").map(Number);

  // Create a base UTC Date object for the "From" time
  const fromDateTime = new Date(Date.UTC(2000, 0, 1, fromHours, fromMinutes));

  // Add 90 minutes to calculate the minimum "To" time
  const minToDateTime = new Date(fromDateTime.getTime() + 90 * 60000);

  // Format the minimum "To" time as "HH:mm"
  const minToTime = `${String(minToDateTime.getUTCHours()).padStart(2, "0")}:${String(minToDateTime.getUTCMinutes()).padStart(2, "0")}`;

  // Filter "To" times to include only those starting from the calculated minimum
  return availableTimes.filter((time) => time >= minToTime);
};

export default function EditReservationDialog({
  isOpen,
  reservation,
  onClose,
  onUpdate,
}: EditReservationDialogProps) {
  const [formData, setFormData] = useState({
    date: "",
    from: "",
    to: "",
    guestsNumber: 0,
  });

  const [toOptions, setToOptions] = useState<string[]>([]);

  useEffect(() => {
    if (reservation) {
      const [from, to] = reservation.timeSlot.split("-");
      setFormData({
        date: reservation.date,
        from,
        to,
        guestsNumber: reservation.guestsNumber,
      });
      setToOptions(calculateToTimes(from));
    }
  }, [reservation]);

  const handleFromChange = (fromTime: string) => {
    const newToOptions = calculateToTimes(fromTime);
    setFormData((prev) => ({
      ...prev,
      from: fromTime,
      to: "",
    }));
    setToOptions(newToOptions)
  };

  const handleChange = (field: string, value: string | number) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleUpdate = async () => {
    if (!reservation) return;
    const token = localStorage.getItem("token");
    try {
      const updatedData = {
        date: formData.date,
        guestsNumber: formData.guestsNumber,
        timeFrom: formData.from,
        timeTo: formData.to,
      };
      const response=await axios.put(`${BASE_API_URL}/bookings/client/${reservation.id}`, updatedData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      toast.success(response.data.message || "Reservation updated successfully!");
      onUpdate();
      onClose();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error:any) {
      toast.error(error?.response?.data?.error || "Failed to update the reservation.");
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Reservation</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Date</label>
            <Input
              type="date"
              value={formData.date}
              onChange={(e) => handleChange("date", e.target.value)}
              className="mt-1"
            />
          </div>
          <div className="flex gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">From</label>
              <Select
                value={formData.from}
                onValueChange={(value) => handleFromChange(value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select time" />
                </SelectTrigger>
                <SelectContent>
                  {availableTimes.map((time) => (
                    <SelectItem key={time} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">To</label>
              <Select
                value={formData.to}
                onValueChange={(value) => handleChange("to", value)}
                disabled={!formData.from}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select time" />
                </SelectTrigger>
                <SelectContent>
                {toOptions.map((time) => (
                    <SelectItem key={time} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Guests</label>
            <Input
              type="number"
              min={1}
              value={formData.guestsNumber}
              onChange={(e) => handleChange("guestsNumber",e.target.value)}
              className="mt-1"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button className="bg-green-600 text-white" onClick={handleUpdate}  disabled={!formData.from || !formData.to}>
            Update
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
