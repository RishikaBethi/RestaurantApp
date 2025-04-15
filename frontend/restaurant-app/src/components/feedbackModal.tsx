import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Star } from "lucide-react";

interface FeedbackModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function FeedbackModal({ isOpen, onClose }: FeedbackModalProps) {
  const [rating, setRating] = useState(4);
  const [isCulinaryModalOpen, setIsCulinaryModalOpen] = useState(false);

  const openCulinaryModal = () => {
    onClose(); // Close the main modal
    setIsCulinaryModalOpen(true); // Open the culinary modal
  };

  return (
    <>
      {/* Main Feedback Modal */}
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Give Feedback</DialogTitle>
            <p className="text-sm text-gray-500">Please rate your experience below</p>
          </DialogHeader>
          <div className="flex gap-4 my-4">
            <Button variant="outline" className="flex-1">
              Service
            </Button>
            <Button
              variant="outline"
              className="flex-1"
              onClick={openCulinaryModal}
            >
              Culinary Experience
            </Button>
          </div>
          <div className="flex flex-col items-center gap-4">
            <div className="text-center">
              <img
                src="https://via.placeholder.com/80"
                alt="Reviewer"
                className="rounded-full mb-2"
              />
              <p className="font-semibold">Mario Jast</p>
              <p className="text-sm text-gray-500">Waiter</p>
            </div>
            <div className="flex gap-1">
              {[...Array(5)].map((_, i) => (
                <Star
                  key={i}
                  className={`w-6 h-6 cursor-pointer ${i < rating ? "text-yellow-500 fill-yellow-500" : "text-gray-300"}`}
                  onClick={() => setRating(i + 1)}
                />
              ))}
            </div>
            <p className="text-sm text-gray-500">{rating}/5 stars</p>
            <textarea
              placeholder="Add your comments"
              className="w-full border rounded-lg p-2 mt-2"
            />
          </div>
          <DialogFooter>
            <Button onClick={onClose} className="bg-green-600 hover:bg-green-700 w-full">
              Submit Feedback
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Culinary Experience Modal */}
      <Dialog open={isCulinaryModalOpen} onOpenChange={() => setIsCulinaryModalOpen(false)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Give Feedback</DialogTitle>
            <p className="text-sm text-gray-500">Please rate your experience below</p>
          </DialogHeader>
          <div className="flex flex-col items-center gap-4">
            <div className="flex gap-1">
              {[...Array(5)].map((_, i) => (
                <Star
                  key={i}
                  className={`w-6 h-6 cursor-pointer ${i < rating ? "text-yellow-500 fill-yellow-500" : "text-gray-300"}`}
                  onClick={() => setRating(i + 1)}
                />
              ))}
            </div>
            <p className="text-sm text-gray-500">{rating}/5 stars</p>
            <textarea
              placeholder="Add your comments"
              className="w-full border rounded-lg p-2 mt-2"
            />
          </div>
          <DialogFooter>
            <Button
              onClick={() => {
                setIsCulinaryModalOpen(false);
                onClose();
              }}
              className="bg-green-600 hover:bg-green-700 w-full"
            >
              Submit Culinary Feedback
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
