import { useEffect, useState } from "react";
import axios from "axios";
import {
  Dialog,
  DialogContent,
  DialogTrigger,
} from "@/components/ui/dialog";

interface DishProps {
  dishId: string;
  trigger: React.ReactNode;
}

interface DishData {
  name: string;
  imageUrl: string;
  description: string;
  calories: string;
  proteins: string;
  fats: string;
  carbohydrates: string;
  vitamins: string;
  price: string;
  weight: string;
}

export function Dish({ dishId, trigger }: DishProps) {
  const [open, setOpen] = useState(false);
  const [dishData, setDishData] = useState<DishData | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchDishData = async () => {
      if (!open || dishData) return; // prevent refetching
      setLoading(true);
      try {
        const token=localStorage.getItem("token");
        const response = await axios.get(
          `https://dc5t69k4t6.execute-api.ap-southeast-2.amazonaws.com/dev/dishes/${dishId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`, // Adding token to request header
            },
          }
        );
        const data = response.data.content;

      const formattedDish: DishData = {
        name: data.name,
        description: data.description,
        imageUrl: data.imageUrl,
        calories: data.calories,
        carbohydrates: data.carbohydrates,
        fats: data.fats,
        proteins: data.proteins,
        vitamins: data.vitamins,
        price: data.price,
        weight: data.weight,
      };

      setDishData(formattedDish);
      } catch (error) {
        console.error("Error fetching dish:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDishData();
    console.log(dishData);
  }, [open, dishId, dishData]);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="max-w-md md:max-w-xl">
        {loading ? (
          <div className="text-center py-10">Loading...</div>
        ) : dishData ? (
          <div className="space-y-4 text-sm">
            <div className="flex justify-center">
              <img
                src={dishData.imageUrl}
                alt={dishData.name}
                className="w-40 h-40 object-cover rounded-full"
              />
            </div>
            <h2 className="text-xl font-semibold text-center">
              {dishData.name}
            </h2>
            <p className="text-center text-gray-600">{dishData.description}</p>
            <div className="text-gray-700 space-y-1">
              <p><strong>Calories:</strong> {dishData.calories}</p>
              <p><strong>Protein:</strong> {dishData.proteins}</p>
              <p><strong>Fats:</strong> {dishData.fats} (mostly healthy fats)</p>
              <p><strong>Carbohydrates:</strong> {dishData.carbohydrates}</p>
              <p><strong>Vitamins and minerals:</strong> {dishData.vitamins}</p>
            </div>
            <div className="flex justify-between text-gray-900 font-medium pt-2">
              <span>{dishData.price}</span>
              <span>{dishData.weight}</span>
            </div>
          </div>
          ) : (
            <p>Something went wrong. Could not load dish info.</p>
        )}
      </DialogContent>
    </Dialog>
  );
}
