export default function ShimmerReservations() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6 animate-pulse">
      {[...Array(6)].map((_, i) => (
        <div key={i} className="bg-white p-4 rounded-lg shadow-lg">

          <div className="flex items-center gap-2 mb-3">
            <div className="w-5 h-5 bg-gray-300 rounded-full" />
            <div className="h-3 bg-gray-300 rounded w-3/4"></div>
          </div>

          <div className="flex items-center gap-2 mb-3">
            <div className="w-5 h-5 bg-gray-300 rounded-full" />
            <div className="h-3 bg-gray-300 rounded w-2/3"></div>
          </div>

          <div className="flex items-center gap-2 mb-3">
            <div className="w-5 h-5 bg-gray-300 rounded-full" />
            <div className="h-3 bg-gray-300 rounded w-1/2"></div>
          </div>

          <div className="flex items-center gap-2 mb-3">
            <div className="w-5 h-5 bg-gray-300 rounded-full" />
            <div className="h-3 bg-gray-300 rounded w-1/3"></div>
          </div>

          <div className="flex gap-2 mt-4">
            <div className="h-8 bg-gray-300 rounded w-1/2"></div>
            <div className="h-8 bg-gray-300 rounded w-1/2"></div>
          </div>
        </div>
      ))}
    </div>
  );
}
