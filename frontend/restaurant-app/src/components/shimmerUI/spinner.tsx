import React from "react";

const Spinner: React.FC = () => {
  return (
    <div className="flex justify-items-center h-96">
      <h1 className="font-semibold text-center text-green-500 p-4">Loading...</h1>
      <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-green-500 border-opacity-50"></div>
    </div>
  );
};

export default Spinner;
