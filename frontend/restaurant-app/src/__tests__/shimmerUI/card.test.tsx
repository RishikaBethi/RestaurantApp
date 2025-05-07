import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import {
  Card,
  CardHeader,
  CardFooter,
  CardTitle,
  CardAction,
  CardDescription,
  CardContent,
} from "@/components/ui/card";

const testText = {
  card: "Test Card",
  header: "Test Header",
  title: "Test Title",
  description: "Test Description",
  content: "Test Content",
  action: "Test Action",
  footer: "Test Footer",
};

describe("Card components", () => {
  it("renders Card with children", () => {
    render(<Card>{testText.card}</Card>);
    expect(screen.getByText(testText.card)).toBeInTheDocument();
  });

  it("renders CardHeader with children", () => {
    render(<CardHeader>{testText.header}</CardHeader>);
    expect(screen.getByText(testText.header)).toBeInTheDocument();
  });

  it("renders CardTitle with children", () => {
    render(<CardTitle>{testText.title}</CardTitle>);
    expect(screen.getByText(testText.title)).toBeInTheDocument();
  });

  it("renders CardDescription with children", () => {
    render(<CardDescription>{testText.description}</CardDescription>);
    expect(screen.getByText(testText.description)).toBeInTheDocument();
  });

  it("renders CardContent with children", () => {
    render(<CardContent>{testText.content}</CardContent>);
    expect(screen.getByText(testText.content)).toBeInTheDocument();
  });

  it("renders CardAction with children", () => {
    render(<CardAction>{testText.action}</CardAction>);
    expect(screen.getByText(testText.action)).toBeInTheDocument();
  });

  it("renders CardFooter with children", () => {
    render(<CardFooter>{testText.footer}</CardFooter>);
    expect(screen.getByText(testText.footer)).toBeInTheDocument();
  });
});
