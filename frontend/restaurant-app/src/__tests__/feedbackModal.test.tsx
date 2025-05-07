// FeedbackModal.test.tsx
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import FeedbackModal from "../components/feedbackModal";
import { describe, it, vi, beforeEach, afterEach, expect, Mocked } from "vitest";
import axios from "axios";

// Mock axios
vi.mock("axios");
const mockedAxios = axios as Mocked<typeof axios>;

// Mock toast
vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));
import { toast } from "sonner";

describe("FeedbackModal", () => {
  const onClose = vi.fn();

  beforeEach(() => {
    localStorage.setItem("token", "mock-token");
  });

  afterEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it("renders modal and shows Service tab by default", () => {
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={1} />);
    expect(screen.getByText("Give Feedback")).toBeInTheDocument();
    expect(screen.getByText("Service")).toBeInTheDocument();
    expect(screen.getByText("Culinary Experience")).toBeInTheDocument();
  });

  it("switches between tabs and retains state", async () => {
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={1} />);
    const culinaryTab = screen.getByText("Culinary Experience");
    fireEvent.click(culinaryTab);
    expect(screen.getByText("Culinary Experience")).toHaveClass("text-green-600");
  });

  it("changes rating and comment correctly", async () => {
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={1} />);
    const stars = screen.getAllByRole("img"); // Stars are rendered as SVGs
    fireEvent.click(stars[0]); 
    expect(screen.getByText("0/5 stars")).toBeInTheDocument();

    const textarea = screen.getByPlaceholderText("Add your comments") as HTMLTextAreaElement;
    fireEvent.change(textarea, { target: { value: "Good service!" } });
    expect(textarea.value).toBe("Good service!");
  });

  it("fetches existing feedback and populates fields", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        serviceRating: 4,
        serviceComment: "Nice",
        waiterName: "Alice",
        waiterImageBase64: "iVBOR",
      },
    });
  
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={123} />);
  
    await waitFor(() => {
      expect(mockedAxios.get).toHaveBeenCalledWith(
        expect.stringContaining("/getPreviousFeedback/123"),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer mock-token",
          }),
        })
      );
    });
  
    expect(screen.getByText(/Alice/)).toBeInTheDocument();
    expect(screen.getByText("4/5 stars")).toBeInTheDocument();
  });  

  it("handles feedback submit successfully", async () => {
    mockedAxios.get.mockResolvedValueOnce({ data: {} }); // fetch
    mockedAxios.post.mockResolvedValueOnce({ data: { message: "Thanks!" } }); // submit
  
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={22} />);
  
    await waitFor(() => expect(mockedAxios.get).toHaveBeenCalled());
  
    const button = screen.getByRole("button", {
      name: /submit feedback|update feedback/i,
    });
    fireEvent.click(button);
  
    await waitFor(() => {
      expect(mockedAxios.post).toHaveBeenCalledTimes(1);
      expect(toast.success).toHaveBeenCalledWith("Thanks!");
      expect(onClose).toHaveBeenCalled();
    });
  });  

  it("handles feedback submission error", async () => {
    mockedAxios.get.mockResolvedValueOnce({ data: {} }); // fetch
    mockedAxios.post.mockRejectedValueOnce({
      response: { data: { error: "Failed to submit feedback." } },
    });
  
    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={22} />);
  
    await waitFor(() => expect(mockedAxios.get).toHaveBeenCalled());
  
    const button = screen.getByRole("button", {
      name: /submit feedback|update feedback/i,
    });
    fireEvent.click(button);
  
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Failed to submit feedback.");
    });
  });
  
  it("shows default fallback profile picture if image is missing", async () => {
    mockedAxios.post.mockResolvedValueOnce({
      data: {
        waiterName: "Bob",
        serviceRating: 5,
        serviceComment: "Perfect",
        waiterImageBase64: null,
      },
    });

    render(<FeedbackModal isOpen={true} onClose={onClose} reservationId={42} />);

    await waitFor(() => {
      const img = screen.getByRole("img") as HTMLImageElement;
      expect(img.src).toMatch(/blank-profile-picture/);
    });
  });
});
