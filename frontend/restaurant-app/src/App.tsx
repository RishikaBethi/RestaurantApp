import { useState } from 'react';
import './App.css';
import Navbar from './components/navbar';
import Home from './pages/homePage';
import Login from './pages/loginPage';
import MyProfile from './pages/profile';
import RegisterPage from './pages/registerPage';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import RestroPage from './pages/restroPage';
import ReservationsPage from './pages/reservations';
import { Toaster } from "sonner";
import BookTable from './pages/tablebooking';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false); // Manage authentication state

  return (
    <BrowserRouter>
    <Toaster richColors position="top-right" />
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<Login setIsLoggedIn={setIsLoggedIn} />} />
        <Route
          path="/"
          element={
            <>
              <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} /> {/* Pass authentication state to Navbar */}
              <main>
                <Home />
              </main>
            </>
          }
        />
        <Route
          path="/profile"
          element={
            <>
              <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
              <main>
                <MyProfile />
              </main>
            </>
          }
        />
        <Route
          path="/restaurant/:locationId"
          element={
            <>
              <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
              <main>
                <RestroPage/>
              </main>
            </>
          }
        />
        <Route
          path="/reservations"
          element={
            <>
              <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
              <main>
                <ReservationsPage/>
              </main>
            </>
          }
        />
        <Route
          path="/book-table"
          element={
            <>
              <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
              <main>
                <BookTable/>
              </main>
            </>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
