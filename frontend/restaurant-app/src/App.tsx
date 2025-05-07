import { useState } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "sonner";
import Layout from "./components/layout";
import Home from "./pages/homePage";
import Login from "./pages/loginPage";
import MyProfile from "./pages/profile";
import RegisterPage from "./pages/registerPage";
import RestroPage from "./pages/restroPage";
import ReservationsPage from "./pages/reservations";
import BookTable from "./pages/tablebooking";
import ReservationDashboard from './pages/waiterReservationPage';
import Menu from './components/viewMenu';

type AppProps = {
  initialIsLoggedIn?: boolean;
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function App({ initialIsLoggedIn = false }: AppProps) {
  const [isLoggedIn, setIsLoggedIn] = useState(initialIsLoggedIn);

  return (
    <BrowserRouter>
      <Toaster richColors position="top-right" />
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<Login setIsLoggedIn={setIsLoggedIn} />} />
        <Route element={<Layout isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />}>
          <Route path="/" element={<Home />} />
          <Route path="/profile" element={<MyProfile />} />
          <Route path="/restaurant/:locationId" element={<RestroPage />} />
          <Route path="/reservations" element={<ReservationsPage />} />
          <Route path="/book-table" element={<BookTable />} />
          <Route path="/waiter-reservations" element={<ReservationDashboard/>}/>
          <Route path="/menu" element={<Menu/>}/>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
