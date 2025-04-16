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
  const [activeTab, setActiveTab] = useState<"service" | "culinary">("service");
  const [comment, setComment] = useState("");

  const handleSubmit = () => {
    const feedback = {
      type: activeTab,
      rating,
      comment,
    };
    console.log("Submitted feedback:", feedback);
    // You can replace this with an API call
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-full max-w-md rounded-2xl p-6">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">Give Feedback</DialogTitle>
          <p className="text-sm text-gray-500">Please rate your experience below</p>
        </DialogHeader>

        {/* Tabs */}
        <div className="flex gap-4 mt-4 mb-6 border-b border-gray-200">
          <button
            onClick={() => setActiveTab("service")}
            className={`pb-2 flex-1 text-center font-medium transition-all ${
              activeTab === "service"
                ? "text-green-600 border-b-2 border-green-600"
                : "text-gray-400"
            }`}
          >
            Service
          </button>
          <button
            onClick={() => setActiveTab("culinary")}
            className={`pb-2 flex-1 text-center font-medium transition-all ${
              activeTab === "culinary"
                ? "text-green-600 border-b-2 border-green-600"
                : "text-gray-400"
            }`}
          >
            Culinary Experience
          </button>
        </div>

        {/* Main Content */}
        <div className="flex flex-col items-center gap-4">
          {activeTab === "service" && (
            <div className="text-center">
              <img
                src="https://via.placeholder.com/80"
                alt="Staff member"
                className="rounded-full mb-2 mx-auto"
              />
              <p className="font-semibold">Mario Jast</p>
              <p className="text-sm text-gray-500">Waiter</p>
              <p className="text-sm text-yellow-500 font-semibold mt-1">4.96 ★</p>
            </div>
          )}

          {/* Star Rating */}
          <div className="flex gap-1 mt-2">
            {[...Array(5)].map((_, i) => (
              <Star
                key={i}
                className={`w-6 h-6 cursor-pointer transition ${
                  i < rating ? "text-yellow-500 fill-yellow-500" : "text-gray-300"
                }`}
                onClick={() => setRating(i + 1)}
              />
            ))}
          </div>

          <p className="text-sm text-gray-500">{rating}/5 stars</p>

          {/* Comment */}
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Add your comments"
            className="w-full border rounded-lg p-2 mt-2 h-24 resize-none"
          />
        </div>

        {/* Footer Button */}
        <DialogFooter>
          <Button
            onClick={handleSubmit}
            className="bg-green-600 hover:bg-green-700 w-full mt-4"
          >
            Submit Feedback
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
