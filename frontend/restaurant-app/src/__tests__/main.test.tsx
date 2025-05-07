import { vi, test, expect, beforeEach } from "vitest";

// ✅ Hoist this mock above imports so it's used before index.tsx runs
const renderMock = vi.fn();

vi.mock("react-dom/client", () => ({
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  createRoot: vi.fn((container: Element) => ({
    render: renderMock,
  })),
}));

beforeEach(() => {
  // Reset the DOM for each test
  document.body.innerHTML = '<div id="root"></div>';
  renderMock.mockClear();
});

test("main.tsx > calls createRoot and renders App", async () => {
  // 🧠 Delay index.tsx import until after mocking
  const { createRoot } = await import("react-dom/client");
  await import("../main"); // Adjust path if needed

  const rootElement = document.getElementById("root");
  expect(rootElement).not.toBeNull();

  // ✅ Check if createRoot was called with #root
  expect(createRoot).toHaveBeenCalledWith(rootElement);
  // ✅ Check if render was called
  expect(renderMock).toHaveBeenCalled();
});
