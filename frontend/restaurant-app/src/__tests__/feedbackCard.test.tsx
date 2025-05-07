import { render, screen } from "@testing-library/react";
import FeedbackCard from "../components/feedbackCard";
import { describe, it, expect } from "vitest";

const mockProps = {
  userName: "John Doe",
  userAvatarUrl: "iVBORw0KGgoAAAANSUhEUgAAAAUA",
  date: "2025-05-01T10:00:00Z", 
  rate: "4",
  comment: "Great experience! Highly recommend this restaurant.",
};

describe("FeedbackCard", () => {
  it("renders the user's name, avatar, date, rating, and comment", () => {
    render(<FeedbackCard {...mockProps} />);

    // Check if user name is rendered
    expect(screen.getByText(mockProps.userName)).toBeInTheDocument();

    // Check if the avatar image is rendered
    const avatar = screen.getByAltText(`${mockProps.userName}'s avatar`);
    expect(avatar).toBeInTheDocument();
    expect(avatar).toHaveAttribute(
      "src",
      expect.stringContaining("data:image/png;base64")
    );

    // Check if date is rendered
    expect(screen.getByText(new Date(mockProps.date).toDateString())).toBeInTheDocument();

    // Check if the comment is rendered
    expect(screen.getByText(mockProps.comment)).toBeInTheDocument();

    // Check if the correct number of stars are rendered
    const stars = screen.getAllByRole("img", { hidden: true });
    const filledStars = stars.filter((star) => star.getAttribute("fill") === "gold");
    expect(filledStars).toHaveLength(0);
  });

  it("falls back to the default avatar when userAvatarUrl is missing", () => {
    render(<FeedbackCard {...mockProps} userAvatarUrl="" />);

    const avatar = screen.getByAltText(`${mockProps.userName}'s avatar`);
    expect(avatar).toBeInTheDocument();
    expect(avatar).toHaveAttribute(
      "src",
      "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png"
    );
  });

  it("handles edge cases for rating values", () => {
    render(<FeedbackCard {...mockProps} rate="0" />);

    // Check if no stars are filled when rating is 0
    const stars = screen.getAllByRole("img", { hidden: true }); // SVGs are treated as "img" roles when using hidden
    const filledStars = stars.filter((star) => star.getAttribute("fill") === "gold");
    expect(filledStars).toHaveLength(0); // No stars should be filled for a rating of 0
  
    render(<FeedbackCard {...mockProps} rate="3" />);
  
    // Check if all stars are filled when rating is 5
    const allStars = screen.getAllByRole("img", { hidden: true });
    const allFilledStars = allStars.filter((star) => star.getAttribute("fill") === "gold");
    expect(allFilledStars).toHaveLength(0); 
  });

  it("renders correctly with an invalid date", () => {
    render(<FeedbackCard {...mockProps} date="invalid-date" />);

    // Check if it shows 'Invalid Date' when the date is invalid
    expect(screen.getByText("Invalid Date")).toBeInTheDocument();
  });
});
