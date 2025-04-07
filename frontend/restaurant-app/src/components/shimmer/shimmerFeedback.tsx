export default function ShimmerFeedback() {
  return (
    <div className="border rounded-2xl shadow-sm p-4 animate-pulse space-y-4 bg-white">
      <div className="flex items-center gap-4">
        <div className="w-10 h-10 rounded-full bg-gray-200" />
        <div className="flex-1 space-y-2">
          <div className="w-3/4 h-4 bg-gray-200 rounded" />
          <div className="w-1/3 h-3 bg-gray-200 rounded" />
        </div>
      </div>
      <div className="w-full h-3 bg-gray-200 rounded" />
      <div className="w-5/6 h-3 bg-gray-200 rounded" />
      <div className="w-2/3 h-3 bg-gray-200 rounded" />
    </div>
  );
}
