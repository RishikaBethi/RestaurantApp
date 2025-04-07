export default function ShimmerLocations() {
    return (
      <div className="grid grid-cols-3 gap-4 mt-4">
        {Array.from({ length: 3 }).map((_, i) => (
          <div
            key={i}
            className="animate-pulse bg-white rounded-lg shadow p-4 space-y-4"
          >
            <div className="h-32 bg-gray-300 rounded-lg w-full" />
            <div className="h-4 bg-gray-300 rounded w-3/4" />
            <div className="h-4 bg-gray-300 rounded w-1/2" />
          </div>
        ))}
      </div>
    );
  }
  