export default function ShimmerTables() {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {Array.from({ length: 4 }).map((_, index) => (
          <div
            key={index}
            className="bg-white shadow-lg rounded-lg p-4 flex animate-pulse"
          >
            <div className="w-32 h-32 bg-gray-300 rounded-lg"></div>
            <div className="ml-4 flex-1 space-y-3">
              <div className="h-5 bg-gray-300 rounded w-3/4"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              <div className="flex gap-2 flex-wrap mt-2">
                {Array.from({ length: 4 }).map((_, slotIndex) => (
                  <div
                    key={slotIndex}
                    className="w-16 h-6 bg-gray-200 rounded"
                  ></div>
                ))}
              </div>
              <div className="w-20 h-4 bg-gray-300 rounded mt-2"></div>
            </div>
          </div>
        ))}
      </div>
    );
  }
  