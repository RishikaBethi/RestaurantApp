import { render, screen, fireEvent } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import AvailableSlotsModal from "../components/availableSlotsModal";

const mockOnClose = vi.fn();
const mockOnSlotClick = vi.fn();

const mockTable = {
  locationId: "1",
  locationAddress: "123 Main Street",
  availableSlots: ["10:00-11:00", "12:00-13:00"],
  tableNumber: "5",
  capacity: "4",
};

const mockDate = "2025-04-30";

describe("AvailableSlotsModal Component", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should not render anything if table is null", () => {
    render(
      <AvailableSlotsModal
        isOpen={true}
        onClose={mockOnClose}
        table={null}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );
    expect(screen.queryByText("Available slots")).not.toBeInTheDocument();
  });

   it("should display modal with correct details when table is provided", () => {
    render(
      <AvailableSlotsModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );

    expect(screen.getByText("Available slots")).toBeInTheDocument();
    expect(screen.getByText("123 Main Street")).toBeInTheDocument();
    expect(screen.getByText("10:00-11:00")).toBeInTheDocument();
    expect(screen.getByText("12:00-13:00")).toBeInTheDocument();
  });
  it("should render all available slots as buttons", () => {
    render(
      <AvailableSlotsModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );

    const slotButtons = screen.getAllByRole("button", { name: /10:00-11:00|12:00-13:00/ });
    expect(slotButtons).toHaveLength(2);
    expect(slotButtons[0]).toHaveTextContent("10:00-11:00");
    expect(slotButtons[1]).toHaveTextContent("12:00-13:00");
  });

  it("should call onSlotClick and onClose when a slot button is clicked", () => {
    render(
      <AvailableSlotsModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );

    const firstSlotButton = screen.getByText("10:00-11:00");
    fireEvent.click(firstSlotButton);

    expect(mockOnSlotClick).toHaveBeenCalledWith({ fromTime: "10:00", toTime: "11:00" }, 1);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("should close the modal when the Dialog's onOpenChange is triggered", () => {
    render(
      <AvailableSlotsModal
        isOpen={true}
        onClose={mockOnClose}
        table={mockTable}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );

    const dialog = screen.getByRole("dialog");
    fireEvent.keyDown(dialog, { key: "Escape" }); // Simulates closing the dialog

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("should not render when isOpen is false", () => {
    render(
      <AvailableSlotsModal
        isOpen={false}
        onClose={mockOnClose}
        table={mockTable}
        date={mockDate}
        onSlotClick={mockOnSlotClick}
      />
    );

    expect(screen.queryByText("Available slots")).not.toBeInTheDocument();
  });
});
