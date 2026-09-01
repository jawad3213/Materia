import React from 'react';

interface PaginationProps {
  currentPage: number; // 0-indexed
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  // Generate page numbers to show
  const getPageNumbers = () => {
    const pages = [];
    if (totalPages <= 7) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        pages.push(0, 1, 2, 3, 4, '...', totalPages - 1);
      } else if (currentPage >= totalPages - 4) {
        pages.push(0, '...', totalPages - 5, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1);
      } else {
        pages.push(0, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages - 1);
      }
    }
    return pages;
  };

  const pageNumbers = getPageNumbers();

  return (
    <nav className="flex items-center gap-2">
      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition rounded-lg border ${
          currentPage === 0
            ? 'text-gray-400 bg-white border-gray-200 cursor-not-allowed dark:bg-gray-900 dark:border-gray-800 dark:text-gray-600'
            : 'text-gray-700 bg-white border-gray-200 hover:bg-gray-50 dark:bg-gray-900 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800'
        }`}
      >
        <svg className="w-4 h-4 fill-current" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fillRule="evenodd" clipRule="evenodd" d="M15.8333 10C15.8333 10.2301 15.6468 10.4167 15.4167 10.4167H5.97334L9.80008 14.1539C9.9664 14.3163 9.96962 14.5828 9.80721 14.7491C9.6448 14.9154 9.37836 14.9187 9.21204 14.7562L4.31505 9.97384C4.28828 9.94828 4.26425 9.91974 4.24354 9.88871C4.19539 9.8166 4.16667 9.72898 4.16667 9.63584V9.63583V9.63582C4.16667 9.54267 4.19539 9.45506 4.24354 9.38294C4.26425 9.35191 4.28828 9.32337 4.31505 9.29782L9.21204 4.51543C9.37836 4.35299 9.6448 4.35622 9.80721 4.52254C9.96962 4.68886 9.9664 4.95531 9.80008 5.11772L5.97334 8.85493H15.4167C15.6468 8.85493 15.8333 9.04148 15.8333 9.27159V10Z" />
        </svg>
        Previous
      </button>
      
      <ul className="flex items-center gap-2 hidden sm:flex">
        {pageNumbers.map((page, index) => {
          if (page === '...') {
            return (
              <li key={`ellipsis-${index}`}>
                <span className="flex items-center justify-center w-10 h-10 text-sm font-medium text-gray-500 dark:text-gray-400">
                  ...
                </span>
              </li>
            );
          }
          
          const isCurrent = page === currentPage;
          return (
            <li key={`page-${page}`}>
              <button
                onClick={() => onPageChange(page as number)}
                className={`flex items-center justify-center w-10 h-10 text-sm font-medium transition rounded-lg ${
                  isCurrent
                    ? 'text-white bg-brand-500 hover:bg-brand-600'
                    : 'text-gray-700 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800'
                }`}
              >
                {(page as number) + 1}
              </button>
            </li>
          );
        })}
      </ul>
      
      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage >= totalPages - 1}
        className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition rounded-lg border ${
          currentPage >= totalPages - 1
            ? 'text-gray-400 bg-white border-gray-200 cursor-not-allowed dark:bg-gray-900 dark:border-gray-800 dark:text-gray-600'
            : 'text-gray-700 bg-white border-gray-200 hover:bg-gray-50 dark:bg-gray-900 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800'
        }`}
      >
        Next
        <svg className="w-4 h-4 fill-current" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fillRule="evenodd" clipRule="evenodd" d="M4.16667 10C4.16667 9.76993 4.35322 9.58337 4.58333 9.58337H14.0267L10.1999 5.84616C10.0336 5.68375 10.0304 5.4173 10.1928 5.25098C10.3552 5.08466 10.6216 5.08143 10.788 5.24384L15.6849 10.0262C15.7117 10.0518 15.7358 10.0803 15.7565 10.1114C15.8046 10.1835 15.8333 10.2711 15.8333 10.3642V10.3643V10.3643C15.8333 10.4574 15.8046 10.545 15.7565 10.6172C15.7358 10.6482 15.7117 10.6767 15.6849 10.7023L10.788 15.4847C10.6216 15.6471 10.3552 15.6439 10.1928 15.4775C10.0304 15.3112 10.0336 15.0448 10.1999 14.8824L14.0267 11.1451H4.58333C4.35322 11.1451 4.16667 10.9586 4.16667 10.7285V10Z" />
        </svg>
      </button>
    </nav>
  );
}
