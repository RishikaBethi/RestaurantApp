import { Card, CardContent } from "@/components/ui/card";

export default function ShimmerDishes() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <Card key={index} className="shadow-md p-3 text-center rounded-lg animate-pulse">
          <div className="h-32 w-36 bg-gray-300 rounded-full mx-auto"></div>
          <CardContent className="mt-3">
            <div className="h-4 bg-gray-300 rounded w-3/4 mx-auto"></div>
            <div className="flex justify-between text-sm text-gray-500 mt-2">
              <div className="h-4 bg-gray-300 rounded w-1/4"></div>
              <div className="h-4 bg-gray-300 rounded w-1/4"></div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
