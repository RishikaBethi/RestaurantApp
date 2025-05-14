import { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { MapPin, User, CalendarIcon, PlusIcon, SearchIcon } from "lucide-react";
import { BASE_API_URL } from "@/constants/constant";
import Spinner from "@/components/shimmerUI/spinner";

interface Report {
  name: string;
  locationId: string;
  fromDate: string;
  toDate: string;
  waiterId: string;
  downloadLink: string;
  id: string;
  description: string;
}

interface Waiter {
  waiterId: string;
  waiterName: string;
}

export default function ReportsPage() {
  const [location, setLocation] = useState<string>("");
  const [waiter, setWaiter] = useState<string>("");
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [reports, setReports] = useState<Report[]>([]);
  const [waiterDetails, setWaiterDetails] = useState<Waiter[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const user=JSON.parse(localStorage.getItem("user")|| '""');
  const role=localStorage.getItem("role");

  const locationMap: Record<string, string> = {
    LOC001: "48 Rustaveli Avenue",
    LOC002: "14 Baratashvili Street",
    LOC003: "9 Abashidze Street",
  };

  useEffect(() => {
    // Fetch waiter details when the component mounts
    const fetchWaiterDetails = async () => {
      const token = localStorage.getItem("token");
      try {
        const response = await axios.get<Waiter[]>(
          `${BASE_API_URL}/waiters/details`, {
            headers: {
              Authorization: `Bearer ${token}`, 
            },}
        );
        setWaiterDetails(response.data);
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (error) {
        toast.error("Failed to fetch waiter details.");
      }
    };

    fetchWaiterDetails();
  }, []);

  const fetchReports = async () => {
    setIsLoading(true);
    try {
      const response = await axios.get<Report[]>(
        "https://zc5gjo3j9d.execute-api.ap-southeast-2.amazonaws.com/dev/reports",
        {
          params: {
            fromDate: startDate || undefined,
            toDate: endDate || undefined,
            locationId: location || undefined,
            waiterId: waiter || undefined,
          },
        }
      );
      setReports(response.data);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
        // Show backend error message or a generic message
        const errorMessage = error?.response?.data?.error || "Failed to fetch reports. Please try again.";
        toast.error(errorMessage);
      }finally {
        setIsLoading(false); // End loading
      }
  };

  const createReport = async () => {
    try {
      const response=await axios.post(
        "https://zc5gjo3j9d.execute-api.ap-southeast-2.amazonaws.com/dev/reports",
        {
          locationId: location,
          waiterId: waiter,
          startDate,
          endDate,
        }
      );
      if (response.data?.message) {
        toast.success(response.data.message);
      } else {
        toast.success("Report created successfully!");
      }
    await fetchReports();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
        // Show backend error message or a generic error
        const errorMessage = error?.response?.data?.error || "Failed to create report. Please try again.";
        toast.error(errorMessage);
        console.error("Failed to create report:", errorMessage);
      }
  };

  const getWaiterName = (waiterId: string) => {
    const waiter = waiterDetails.find((w) => w.waiterId === waiterId);
    return waiter ? waiter.waiterName : "Unknown Waiter";
  };

  return (
    <div>
      {/* Header */}
      <div className="bg-green-600 p-6 text-white mb-6">
        <h1 className="text-2xl font-bold">Hello, {user} ({role})</h1>
      </div>

      {/* Filters */}
      <div className="flex items-center justify-between p-6">
        <div className="flex justify-between items-center gap-12">
          <Select value={location} onValueChange={setLocation}>
            <SelectTrigger className="w-40 border border-gray-300">
              <MapPin className="w-4 h-4 mr-1" />
              <SelectValue placeholder="Location" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="LOC001">48 Rustaveli Avenue</SelectItem>
              <SelectItem value="LOC002">14 Baratashvili Street</SelectItem>
              <SelectItem value="LOC003">9 Abashidze Street</SelectItem>
            </SelectContent>
          </Select>

          <Select value={waiter} onValueChange={setWaiter}>
            <SelectTrigger className="w-40 border border-gray-300">
              <User className="w-4 h-4 mr-1" />
              <SelectValue placeholder="Waiter" />
            </SelectTrigger>
            <SelectContent>
            {waiterDetails.map((waiter) => (
                <SelectItem key={waiter.waiterId} value={waiter.waiterId}>
                  {waiter.waiterName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <div className="flex items-center gap-2">
            Start -
            <Input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              placeholder="Start"
              className="w-40"
            />
          </div>

          <div className="flex items-center gap-2">
            End -
            <Input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              placeholder="End"
              className="w-40"
            />
          </div>

          <Button variant="outline" className="bg-green-600 text-white" onClick={fetchReports} aria-label="Search">
            <SearchIcon className="w-5 h-5" />
          </Button>
        </div>

        {/* Create Report Dialog */}
        <Dialog>
          <DialogTrigger asChild>
            <Button className="bg-green-600 text-white flex items-center">
              <PlusIcon className="w-5 h-5 mr-2" />
              Create Report
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create Report</DialogTitle>
            </DialogHeader>
            <div className="space-y-8">
              <Select value={location} onValueChange={setLocation}>
                <SelectTrigger className="w-full border border-gray-300">
                  <MapPin className="w-4 h-4 mr-1" />
                  <SelectValue placeholder="Location" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LOC001">48 Rustaveli Avenue</SelectItem>
                  <SelectItem value="LOC002">14 Baratashvili Street</SelectItem>
                  <SelectItem value="LOC003">9 Abashidze Street</SelectItem>
                </SelectContent>
              </Select>

              <Select value={waiter} onValueChange={setWaiter}>
                <SelectTrigger className="w-full border border-gray-300">
                  <User className="w-4 h-4 mr-1" />
                  <SelectValue placeholder="Waiter" />
                </SelectTrigger>
                <SelectContent>
                {waiterDetails.map((waiter) => (
                    <SelectItem key={waiter.waiterId} value={waiter.waiterId}>
                      {waiter.waiterName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <div className="flex gap-4">
                <label className="flex items-center">Start</label>
                <Input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  placeholder="Start"
                  className="w-1/2"
                />
                <label className="flex items-center">End</label>
                <Input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  placeholder="End"
                  className="w-1/2"
                />
              </div>

              <Button className="w-full bg-green-600 text-white" onClick={createReport}>
                Create Report
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Report Table */}
      <div className="p-6">
      {isLoading ? (
          <Spinner /> 
        ) : reports.length > 0 ? (
        <>
        <h2 className="text-lg font-medium mb-4">You have {reports.length} reports</h2><div className="space-y-4">
              {reports.map((report) => (
                <Card key={report.id} className="p-4 flex justify-between">
                  <div className="flex justify-between">
                    <h3 className="flex items-center text-lg font-semibold mb-1">{report.name} ({report.description})</h3>
                    <p className="flex items-center text-sm text-gray-800">
                      <CalendarIcon className="w-5 h-5 text-green-600 pr-1" />
                      {report.fromDate} - {report.toDate}
                    </p>
                    {report.locationId && (
                      <p className="flex items-center text-sm text-gray-800">
                        <MapPin className="w-4 h-4 mr-1 text-green-600" />
                        {locationMap[report.locationId] || report.locationId}
                      </p>
                    )}
                    {report.waiterId && (
                      <p className="flex items-center text-sm text-gray-800">
                        <User className="w-4 h-4 mr-1 text-green-600" />
                        {getWaiterName(report.waiterId)}
                      </p>
                    )}
                    {/* Styled Dropdown */}
                    <div className="relative">
                      <button
                        className="bg-green-600 text-white px-3 py-1 text-sm rounded flex items-center space-x-1 hover:bg-green-700"
                        onClick={() => {
                          const dropdown = document.getElementById(`dropdown-${report.id}`);
                          if (dropdown) dropdown.classList.toggle("hidden");
                        } }
                      >
                        <span>Download</span>
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-3 w-3"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                      <div
                        id={`dropdown-${report.id}`}
                        className="absolute right-0 mt-1 bg-white border border-gray-200 rounded-md shadow-lg w-36 hidden z-10"
                      >
                        <ul className="py-1">
                          <li className="px-3 py-1 text-sm hover:bg-green-100 cursor-pointer">
                            <a
                              href={`${report.downloadLink}?format=pdf`}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="block"
                            >
                              Download in PDF
                            </a>
                          </li>
                          <li className="px-3 py-1 text-sm hover:bg-green-100 cursor-pointer">
                            <a
                              href={`${report.downloadLink}?format=excel`}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="block"
                            >
                              Download in Excel
                            </a>
                          </li>
                          <li className="px-3 py-1 text-sm hover:bg-green-100 cursor-pointer">
                            <a
                              href={`${report.downloadLink}?format=csv`}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="block"
                            >
                              Download in CSV
                            </a>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </>
         ) : (
          <h1 className="font-semibold">No reports available.</h1>
        )}
      </div>
    </div>
  );
}
