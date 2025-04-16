import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useState } from "react";

const timeOptions = [
  "10:30", "12:15", "14:00", "15:45", "17:30", "19:15", "21:00",
];

const getTimeSlotLabel = (start: string) => {
  const [hours, minutes] = start.split(":").map(Number);
  const from = new Date();
  from.setHours(hours, minutes);

  const to = new Date(from.getTime() + 90 * 60 * 1000);
  const formatTime = (d: Date) =>
    d.getHours().toString().padStart(2, "0") + ":" + d.getMinutes().toString().padStart(2, "0");

  return `${start} - ${formatTime(to)}`;
};

type PostponeDialogProps = {
  open: boolean;
  onClose: () => void;
  onSubmit: (updated: { date: string; timeFrom: string; timeTo: string; tableNumber: string }) => void;
  initialValues: {
    date: string;
    timeFrom: string;
    tableNumber: string;
  };
};

export const EditWaiterReservation = ({ open, onClose, onSubmit, initialValues }: PostponeDialogProps) => {
  const [date, setDate] = useState(initialValues.date);
  const [timeFrom, setTimeFrom] = useState(initialValues.timeFrom);
  const [tableNumber, setTableNumber] = useState(initialValues.tableNumber);

  const handleConfirm = () => {
    const [hours, minutes] = timeFrom.split(":").map(Number);
    const from = new Date();
    from.setHours(hours, minutes);
    const to = new Date(from.getTime() + 90 * 60 * 1000);
    const timeTo = to.toTimeString().slice(0, 5);

    onSubmit({ date, timeFrom, timeTo, tableNumber });
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Postpone Reservation</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <label className="block text-sm">
            Date:
            <Input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </label>

          <label className="block text-sm">
            Time Slot:
            <select
              className="w-full border rounded-md p-2 mt-1"
              value={timeFrom}
              onChange={(e) => setTimeFrom(e.target.value)}
            >
              {timeOptions.map((startTime) => (
                <option key={startTime} value={startTime}>
                  {getTimeSlotLabel(startTime)}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm">
            Table:
            <select
              className="w-full border rounded-md p-2 mt-1"
              value={tableNumber}
              onChange={(e) => setTableNumber(e.target.value)}
            >
              <option value="1">Table 1</option>
              <option value="2">Table 2</option>
              <option value="3">Table 3</option>
            </select>
          </label>
        </div>

        <DialogFooter className="pt-4">
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button className="bg-green-600 text-white" onClick={handleConfirm}>
            Confirm
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
