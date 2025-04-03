import { useState } from "react";
import { FaCalendarAlt, FaClock,FaUser, FaMapMarkerAlt } from "react-icons/fa";
import locationImage from '../assets/homepage.png';
import ReservationModal from "@/components/reservationModal";
import AvailableSlotsModal from "@/components/availableSlotsModal";
 
const tables = [
  { id: 1, address: "48 Rustaveli Avenue", capacity: 4, slots: ["10:30 a.m. - 12:00 p.m.", "12:15 p.m. - 1:45 p.m.", "2:00 p.m. - 3:30 p.m.", "3:45 p.m. - 5:15 p.m.", "5:30 p.m. - 7:00 p.m."] },
  { id: 2, address: "48 Rustaveli Avenue", capacity: 4, slots: ["10:30 a.m. - 12:00 p.m.", "12:15 p.m. - 1:45 p.m.", "2:00 p.m. - 3:30 p.m.", "3:45 p.m. - 5:15 p.m.", "5:30 p.m. - 7:00 p.m."] },
  { id: 3, address: "48 Rustaveli Avenue", capacity: 4, slots: ["10:30 a.m. - 12:00 p.m.", "12:15 p.m. - 1:45 p.m.", "2:00 p.m. - 3:30 p.m.", "3:45 p.m. - 5:15 p.m.", "5:30 p.m. - 7:00 p.m."] },
  { id: 4, address: "48 Rustaveli Avenue", capacity: 4, slots: ["10:30 a.m. - 12:00 p.m.", "12:15 p.m. - 1:45 p.m.", "2:00 p.m. - 3:30 p.m.", "3:45 p.m. - 5:15 p.m.", "5:30 p.m. - 7:00 p.m."] },
];
 
export default function BookTable() {
  const [guests, setGuests] = useState(1);
  const [selectedTable, setSelectedTable] = useState<typeof tables[0] | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const openModal = (table: typeof tables[0]) => {
    setSelectedTable(table);
    setIsModalOpen(true);
  };
  const [isSlotsModalOpen, setIsSlotsModalOpen] = useState(false);
  const [selectedTableForSlots, setSelectedTableForSlots] = useState<typeof tables[0] | null>(null);
  const openSlotsModal = (table: typeof tables[0]) => {
    setSelectedTableForSlots(table);
    setIsSlotsModalOpen(true);
  };
 
  return (
    <div className="bg-gray-100 min-h-screen">
      {/* Header Section with black background overlay */}
      <header className="relative h-72 flex items-center justify-center text-white p-4 bg-cover bg-center" style={{ backgroundImage: `url(${locationImage})` }}>
        {/* Black background overlay with opacity */}
        <div className="absolute inset-0 bg-black opacity-50 "></div>
       
        <div className="relative text-center ">
          <h2 className="text-4xl font-bold">Green & Tasty Restaurants</h2>
          <p className="text-xl">Book a Table</p>
        </div>
      </header>
 
      {/* Search Section */}
      <section className=" shadow-md rounded-xl p-4 max-w-5xl mx-auto -mt-24 relative z-10">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaMapMarkerAlt className="text-gray-500"/>
            <select className="w-full outline-none" defaultValue="">
              Location
            <option value="" disabled hidden>
              Location
            </option>
            <option value="48 Rustaveli Avenue">48 Rustaveli Avenue</option>
            <option value="14 Baratashvili Street">14 Baratashvili Street</option>
            <option value="9 Abashidze Street">9 Abashidze Street</option>
          </select>
          </div>
          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaCalendarAlt className="text-gray-500"/>
            <input type="date" className="w-full outline-none" />
          </div>
          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaClock className="text-gray-500"/>
            <input type="time" className="w-full outline-none" />
          </div>
          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaUser className="text-gray-500" />
            <span>Guests</span>
            <input
              type="number"
              value={guests}
              min="1"
              onChange={(e) => setGuests(Number(e.target.value))}
              className="w-12 text-center outline-none"
            />
          </div>
          <button className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700">Find a Table</button>
        </div>
      </section>
 
      {/* Available Tables */}
      <section className="p-10 max-w-6xl mx-auto">
        <h3 className="text-xl font-semibold mb-4">Avaliable Tables</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {tables.map((table) => (
            <div key={table.id} className="bg-white shadow-lg rounded-lg p-4 flex cursor-pointer hover:shadow-xl transition"
            onClick={() => openModal(table)}>
              <img src={locationImage} alt="Location" className="w-32 h-32 object-cover rounded-lg" />
              <div className="ml-4">
                <h4 className="text-lg font-bold flex items-center gap-2">
                  <FaMapMarkerAlt className="text-green-600" /> {table.address}
                </h4>
                <p className="text-gray-600">Table seating capacity: {table.capacity} people</p>
                <p className="mt-2 text-sm text-gray-500">7 slots available for Oct 14, 2024:</p>
                <div className="grid grid-cols-2 gap-2 mt-2">
                  {table.slots.slice(0, 4).map((slot, index) => (
                    <button key={index} className="text-sm border border-green-500 text-green-600 px-2 py-1 rounded-lg hover:bg-green-100">
                      {slot}
                    </button>
                  ))}
                </div>
                <button className="mt-2 text-green-600 font-semibold"
                onClick={(e) => { 
                  e.stopPropagation(); 
                  openSlotsModal(table); 
                }}>
                  + Show all
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
      {/* Reservation Modal */}
      <ReservationModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} table={selectedTable} />
      <AvailableSlotsModal 
      isOpen={isSlotsModalOpen} 
      onClose={() => setIsSlotsModalOpen(false)} 
      table={selectedTableForSlots} />
    </div>
  );
}
 
 