import { Button } from "./ui/button";

interface PaginationControlsProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationControlsProps) {
    if (totalPages <= 1) return null;
  return (
    <div className="flex justify-center gap-2 mt-6 flex-wrap">
      {Array.from({ length: totalPages }, (_, i) => {
        const pageNumber = i + 1;
        return (
          <Button
            key={pageNumber}
            variant={currentPage === pageNumber ? "default" : "outline"}
            onClick={() => onPageChange(pageNumber)}
            className={currentPage === pageNumber ? "bg-green-600 text-white" : ""}
          >
            {pageNumber}
          </Button>
        );
      })}
    </div>
  );
}
