import { Outlet } from "react-router-dom";
import Navbar from "./navbar";

interface LayoutProps {
  isLoggedIn: boolean;
  setIsLoggedIn: (value: boolean) => void;
}

export default function Layout({ isLoggedIn, setIsLoggedIn }: LayoutProps) {
  return (
    <>
      <Navbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
      <main data-testid="layout">
        <Outlet /> {/* This will render the nested child components */}
      </main>
    </>
  );
}
