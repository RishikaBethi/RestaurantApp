import { Card, CardContent } from "@/components/ui/card";
import { MapPin } from "lucide-react"; // Icon for location
 
interface LocationCardProps {
  image: string;
  address: string;
  totalCapacity: number;
  averageOccupancy: number;
}
 
export default function LocationCard({image,address,totalCapacity,averageOccupancy}: LocationCardProps) {
  return (
    <Card className="w-full shadow-lg rounded-lg overflow-hidden">
      <img src={image} alt={address} className="w-full h-40 object-cover" />
      <CardContent className="p-4">
        <div className="flex items-center text-green-600 font-semibold">
          <MapPin size={16} className="mr-2" />
          {address}
        </div>
        <div className="flex justify-between text-sm text-black-600 mt-2">
          <span>Total capacity: {totalCapacity} tables</span>
          <span>Average occupancy: {averageOccupancy}%</span>
        </div>
      </CardContent>
    </Card>
  );
}