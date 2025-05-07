import { useState,useEffect } from "react";
import { FaCalendarAlt, FaClock,FaUser, FaMapMarkerAlt } from "react-icons/fa";
import axios from "axios";
import locationImage from '../assets/homepage.png';
import ReservationModal from "@/components/reservationModal";
import AvailableSlotsModal from "@/components/availableSlotsModal";
import ShimmerTables from "@/components/shimmerUI/shimmerTables";
import { BASE_API_URL } from "@/constants/constant";
import { toast } from "sonner";
import { useNavigate } from "react-router";

type Table = {
  locationId: string;
  locationAddress: string | null;
  availableSlots: string[];
  tableNumber: string; 
  capacity: string;
};

type LocationOption = { id: string; address: string };

const getTodayDate = (): string => {
  const today = new Date();
  const year = today.getFullYear();
  const month = `${today.getMonth() + 1}`.padStart(2, '0');
  const day = `${today.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}; 

export default function BookTable() {
  const [guests, setGuests] = useState(1);
  const [selectedTable, setSelectedTable] = useState<Table | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSlotsModalOpen, setIsSlotsModalOpen] = useState(false);
  const [selectedTableForSlots, setSelectedTableForSlots] = useState<Table | null>(null);
  const [locations, setLocations] = useState<LocationOption[]>([]);
  const [, setAllTables] = useState<Table[]>([]);
  const [filteredTables, setFilteredTables] = useState<Table[]>([]);
  const [loadingLocations, setLoadingLocations] = useState(true);
  const [loadingTables, setLoadingTables] = useState(true);
  const [error, setError] = useState("");
  const [selectedLocation, setSelectedLocation] = useState("LOC001");
  const [selectedDate, setSelectedDate] = useState(getTodayDate());
  const [selectedTime, setSelectedTime] = useState("10:30");
  const [loadingFilteredTables, setLoadingFilteredTables] = useState(false);
  const [searchClicked, setSearchClicked] = useState(false);
  const navigate=useNavigate();
  const [selectedSlot, setSelectedSlot] = useState<{ fromTime: string; toTime: string }>({ fromTime: '', toTime: '' });

  const handleSlotClick = (
    table: Table,
    slot: { fromTime: string; toTime: string }  ) => {
    setSelectedTable(table);
    setSelectedSlot(slot);
    setIsModalOpen(true);
  };
  
  const predefinedTimeOptions: string[] = [
    "10:30",
    "12:15",
    "14:00",
    "15:45",
    "17:30",
    "19:15",
    "21:00",
  ];

  const openModal = (table: Table) => {
    const token = localStorage.getItem("token");
    if (!token) {
      toast.error("Please sign in or sign up to make a reservation.");
      // Redirect to the sign-in page
      navigate("/login");
      return;
  }
    setSelectedTable(table);
    setIsModalOpen(true);
  };
  
  const openSlotsModal = (table: Table) => {
    setSelectedTableForSlots(table);
    setIsSlotsModalOpen(true);
  };  

  useEffect(() => {
    axios.get(`${BASE_API_URL}/bookings/tables`) 
      .then(res => {
        setAllTables(res.data);
        setFilteredTables(res.data);
      })
      .catch(err => {
        console.error("Error fetching tables:", err);
        const errorMessage = err?.response?.data?.error || "Failed to load tables.";
        toast.error(errorMessage);
      })
      .finally(() => setLoadingTables(false));
  }, []);

  useEffect(() => {
    axios.get(`${BASE_API_URL}/locations/select-options`)
      .then(response => {
        setLocations(response.data);
      })
      .catch(error => {
        console.error("Error fetching locations:", error);
        setError("Failed to load locations.");
      })
      .finally(() => {
        setLoadingLocations(false);
      });
  }, []);   

  const handleFindTable = async () => {
    setSearchClicked(true);
    if (guests < 1) { // Adjust the maximum guest limit as needed
      toast.error("Please enter a valid number of guests (minimum 1).");
      setFilteredTables([]); // Clear previously displayed tables
      return;
    }
    try {
      setLoadingFilteredTables(true);
      const response = await axios.get(`${BASE_API_URL}/bookings/tables`, {
        params: {
          locationId: selectedLocation || "LOC001",
          date: selectedDate || getTodayDate(),
          time: selectedTime || "10:30",
          guests,
        },
      });
      if (response.data.length === 0) {
        // Show error if no tables are found
        toast.error("No tables available for the selected number of guests.");
        setFilteredTables([]); // Clear previously displayed tables
      } else {
        setFilteredTables(response.data); // Update with valid tables
      }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setFilteredTables([]);
      const errorMessage = err?.response?.data?.error || "Failed to find tables.";
      toast.error(errorMessage);
    }finally {
      setLoadingFilteredTables(false);
    }
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
            <select className="w-full outline-none" 
            value={selectedLocation}
            onChange={(e)=>setSelectedLocation(e.target.value)}>
              <option value="" disabled hidden>
                {loadingLocations ? "Loading locations..." : "Select Location"}
                </option>
                {error ? (
                  <option value="" disabled>{error}</option>
                ) : (
                  locations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.address}
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaCalendarAlt className="text-gray-500"/>
            <input type="date" className="w-full outline-none"
            value={selectedDate}
            onChange={(e) => {
              const value = e.target.value;
              // Restrict year to 4 digits
              const [year, month, day] = value.split("-");
              if (year.length > 4 || !month || !day) return; // Ignore invalid dates
              setSelectedDate(value);
            }}
            />
          </div>

          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaClock className="text-gray-500"/>
            <select
              className="w-full outline-none"
              value={selectedTime}
              onChange={(e) => setSelectedTime(e.target.value)}
            >
              <option value="">Select Time</option>
              {predefinedTimeOptions.map((time) => (
                <option key={time} value={time}>
                  {time}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center border rounded-lg p-2 flex-1 gap-2 bg-white">
            <FaUser className="text-gray-500" />
            <span>Guests</span>
            <input
              type="number"
              value={guests}
              min="1"
              onChange={(e) => {
                const value = e.target.value;
                if (value.length > 2) return; // Allow only up to 2 digits
                setGuests(Number(value));
              }}
              className="w-12 text-center outline-none"
            />
          </div>

          <button onClick={handleFindTable} className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700">Find a Table</button>
        </div>
      </section>
 
      {/* Available Tables */}
      <section className="p-10 max-w-6xl mx-auto">
        <h3 className="text-xl font-semibold mb-4">Avaliable Tables</h3>
        {loadingTables || loadingFilteredTables ? (
          <ShimmerTables />
        ) : !searchClicked ? (
        <p className="text-gray-600 text-center mt-4">
          Please select a location, date, time, and number of guests to view available tables.
        </p>
        ) : filteredTables.length === 0 ? (
        <p className="text-gray-600 text-center mt-4">No available tables found for your selection.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredTables.map((table,index) => (
            <div key={`${table.locationId}-${table.tableNumber}-${index}`} className="bg-white shadow-lg rounded-lg p-4 flex cursor-pointer hover:shadow-xl transition"
            onClick={() => openModal(table)}>
              <img src={locationImage} alt="Location" className="w-32 h-32 object-cover rounded-lg" />
              <div className="ml-4">
                <h4 className="text-lg font-bold flex items-center gap-2">
                  <FaMapMarkerAlt className="text-green-600" />  {table.locationAddress || "Unknown Location"}
                </h4>
                <p className="text-gray-600 font-semibold">Table #{table.tableNumber}, Capacity: {table.capacity} people</p>
                <p className="mt-2 text-sm text-gray-500">{table.availableSlots.length} slots available:</p>
                <div className="grid grid-cols-2 gap-2 mt-2">
                  {table.availableSlots.slice(0, 4).map((slot, index) => (
                    <button key={index} className="text-sm border border-green-500 text-green-600 px-2 py-1 rounded-lg hover:bg-green-100"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSlotClick(table, {
                        fromTime: slot.split("-")[0],
                        toTime: slot.split("-")[1]
                      });
                    }}
                    >
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
        )}
      </section>

      {/* Reservation Modal */}
      <ReservationModal isOpen={isModalOpen} 
      onClose={() => {
        setIsModalOpen(false);
        //setSelectedTable(null); // Reset selected table
        //setSelectedSlot({ fromTime: "", toTime: "" });
      }} 
      table={selectedTable} selectedDate={selectedDate}
      selectedSlot={selectedSlot}  // pass selected slot times
      guests={guests}  
       />
      <AvailableSlotsModal 
      isOpen={isSlotsModalOpen} 
      onClose={() => setIsSlotsModalOpen(false)} 
      table={selectedTableForSlots}
      date={selectedDate} 
      onSlotClick={(slot) => {
        if (selectedTableForSlots) {
          handleSlotClick(selectedTableForSlots, slot);
        }
      }}
    />
    </div>
  );
}
 
 