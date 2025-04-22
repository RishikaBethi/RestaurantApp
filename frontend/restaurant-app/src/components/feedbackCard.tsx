import { Star } from "lucide-react";

interface FeedbackCardProps {
  userName: string;
  userAvatarUrl: string;
  date: string;
  rate: string;
  comment: string;
}

const getImageMimeType = (base64: string) => {
  if (base64.startsWith("/9j")) return "image/jpeg";
  if (base64.startsWith("iVBOR")) return "image/png";
  if (base64.startsWith("R0lGOD")) return "image/gif";
  return "image/png"; // fallback
};

const FeedbackCard = ({
  userName,
  userAvatarUrl,
  date,
  rate,
  comment,
}: FeedbackCardProps) => {
  const fallbackAvatar =
    "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";

  const mimeType = userAvatarUrl ? getImageMimeType(userAvatarUrl) : null;
  const avatarSrc = userAvatarUrl
      ? `data:${mimeType};base64,${userAvatarUrl}`
      : fallbackAvatar;

  return (
    <div className="bg-white shadow rounded-lg p-4">
      <div className="flex items-center gap-3">
        <img
           src={avatarSrc}
           alt={`${userName}'s avatar`}
          className="rounded-full w-10 h-10 object-cover"
        />
        <div>
          <h3 className="font-semibold">{userName}</h3>
          <p className="text-xs text-gray-500">
            {new Date(date).toDateString()}
          </p>
        </div>
        <div className="ml-auto flex">
          {Array.from({ length: Math.min(5, Math.max(0, Math.round(Number(rate) || 0))) }).map((_, i) => (
            <Star key={i} className="w-3 h-3 text-yellow-500" fill={i < Math.round(Number(rate)) ? "gold" : "none"} />
          ))}
        </div>
      </div>
      <p className="mt-2 text-gray-700">{comment}</p>
    </div>
  );
};

export default FeedbackCard;
