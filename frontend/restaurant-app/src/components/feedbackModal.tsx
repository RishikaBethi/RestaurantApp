import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Star } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
//import { BASE_API_URL } from "@/constants/constant";

interface FeedbackModalProps {
  isOpen: boolean;
  onClose: () => void;
  reservationId: number | null;
}

export default function FeedbackModal({ isOpen, onClose,reservationId }: FeedbackModalProps) {
  const [activeTab, setActiveTab] = useState<"service" | "culinary">("service");
  const [serviceRating, setServiceRating] = useState(0);
  const [serviceComment, setServiceComment] = useState("");
  const [cuisineRating, setCuisineRating] = useState(0);
  const [cuisineComment, setCuisineComment] = useState("");
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [existingFeedback, setExistingFeedback] = useState<any>(null);
  const [waiterName,setWaiterName]=useState("");

  const token=localStorage.getItem("token");

  useEffect(() => {
    if (!reservationId || !isOpen) return;

    const fetchFeedback = async () => {
      try {
        const { data } = await axios.post(
          `https://aelam52ao5.execute-api.ap-southeast-2.amazonaws.com/dev/getPreviousFeedback`,
          { reservationId },
          {
            headers: {
              Authorization: `Bearer ${token}`, 
            },}
        );
        setExistingFeedback(data);
        setServiceRating(data.serviceRating || 0);
        setServiceComment(data.serviceComment || "");
        setCuisineRating(data.cuisineRating || 0);
        setCuisineComment(data.cuisineComment || "");
        setWaiterName(data.waiterName || "");
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (error) {
        console.log("No previous feedback or error fetching it.");
        setExistingFeedback(null);
        setServiceRating(0);
        setServiceComment("");
        setCuisineRating(0);
        setCuisineComment("");
      }
    };

    fetchFeedback();
  }, [reservationId, isOpen]);

  const handleSubmit = async () => {
    const payload = {
      reservationId,
      serviceRating: serviceRating.toString(),
      cuisineRating: cuisineRating.toString(),
      serviceComment,
      cuisineComment,
    };

    try {
      const response=await axios.post(
        `https://aelam52ao5.execute-api.ap-southeast-2.amazonaws.com/dev/feedbacks`,
        payload,
        {
          headers: {
            Authorization: `Bearer ${token}`, 
          },}
      );
      if (response?.data?.message) {
        toast.success(response?.data?.message); 
      } else {
        toast.success("Feedback submitted successfully!"); 
      }
      onClose();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error:any) {
      const message =
      error?.response?.data?.error || "Failed to submit feedback.";
    toast.error(message); 
    }
  };

  const rating = activeTab === "service" ? serviceRating : cuisineRating;
  const comment = activeTab === "service" ? serviceComment : cuisineComment;

  const handleRatingChange = (value: number) => {
    if (activeTab === "service") {
      setServiceRating(value);
    } else {
      setCuisineRating(value);
    }
  };

  const handleCommentChange = (value: string) => {
    if (activeTab === "service") {
      setServiceComment(value);
    } else {
      setCuisineComment(value);
    }
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
          {["service", "culinary"].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab as "service" | "culinary")}
              className={`pb-2 flex-1 text-center font-medium transition-all ${
                activeTab === tab ? "text-green-600 border-b-2 border-green-600" : "text-gray-400"
              }`}
            >
              {tab === "service" ? "Service" : "Culinary Experience"}
            </button>
          ))}
        </div>

        {/* Main Content */}
        <div className="flex flex-col items-center gap-4">
          {activeTab === "service" && (
            <div className="text-center">
              <p className="font-semibold">Waiter: {waiterName}</p>
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
                onClick={() => handleRatingChange(i + 1)}
              />
            ))}
          </div>

          <p className="text-sm text-gray-500">{rating}/5 stars</p>

          {/* Comment */}
          <textarea
            value={comment}
            onChange={(e) => handleCommentChange(e.target.value)}
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
            {existingFeedback ? "Update Feedback" : "Submit Feedback"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
