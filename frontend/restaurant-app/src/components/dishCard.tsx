import { Card, CardContent } from "@/components/ui/card";
 
interface DishCardProps {
  image: string;
  name: string;
  price: string;
  weight: string;
}
 
export default function DishCard({ image, name, price, weight }: DishCardProps) {
  return (
    <Card className="shadow-md p-3 text-center rounded-lg">
      <img src={image} alt={name} className=" h-32 object-cover rounded-full mx-auto" />
      <CardContent>
        <h4 className="text-sm font-semibold  ">{name}</h4>
        <div className="flex justify-between text-sm text-gray-600 mt-2">
          <span>{price}</span>
          <span>{weight}</span>
        </div>
      </CardContent>
    </Card>
  );
}