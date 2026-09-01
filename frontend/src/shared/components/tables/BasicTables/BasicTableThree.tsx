import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import Badge from "../../ui/badge/Badge";

interface Transaction {
  id: string;
  name: string;
  logo: React.ReactNode;
  date: string;
  price: string;
  category: string;
  status: "Success" | "Pending" | "Failed";
}

const transactions: Transaction[] = [
  {
    id: "1",
    name: "Bought PYPL",
    logo: (
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#00457C] text-white">
        <svg className="size-4" fill="currentColor" viewBox="0 0 24 24"><path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.01 1.15 1.304 2.42 1.012 4.287-.023.143-.047.288-.077.437-.983 5.05-4.349 6.797-8.647 6.797h-2.19c-.524 0-.968.382-1.05.9l-1.12 7.106z"/></svg>
      </div>
    ),
    date: "Nov 23, 01:00 PM",
    price: "$2,567.88",
    category: "Finance",
    status: "Success",
  },
  {
    id: "2",
    name: "Bought AAPL",
    logo: (
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gray-900 text-white">
        <svg className="size-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12.152 6.896c-.948 0-2.415-1.078-3.96-1.04-2.04.027-3.91 1.183-4.961 3.014-2.117 3.675-.546 9.103 1.519 12.09 1.013 1.454 2.208 3.09 3.792 3.039 1.52-.065 2.09-.987 3.935-.987 1.831 0 2.35.987 3.96.948 1.634-.038 2.653-1.487 3.65-2.943 1.156-1.674 1.636-3.305 1.658-3.385-.034-.016-3.178-1.22-3.197-4.85-.02-3.035 2.476-4.492 2.593-4.566-1.424-2.083-3.627-2.357-4.417-2.433-1.921-.212-3.76 1.115-4.572 1.115zm4.015-3.671c.84-1.016 1.405-2.427 1.25-3.836-1.206.049-2.673.805-3.54 1.815-.776.885-1.455 2.316-1.267 3.697 1.348.104 2.715-.658 3.557-1.676z"/></svg>
      </div>
    ),
    date: "Nov 23, 01:00 PM",
    price: "$2,567.88",
    category: "Finance",
    status: "Pending",
  },
  {
    id: "3",
    name: "Sell KKST",
    logo: (
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-500 text-white font-bold text-sm">
        K
      </div>
    ),
    date: "Nov 23, 01:00 PM",
    price: "$2,567.88",
    category: "Finance",
    status: "Success",
  },
  {
    id: "4",
    name: "Bought FB",
    logo: (
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-500 text-white font-bold text-lg">
        f
      </div>
    ),
    date: "Nov 23, 01:00 PM",
    price: "$2,567.88",
    category: "Finance",
    status: "Success",
  },
  {
    id: "5",
    name: "Sell AMZN",
    logo: (
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-orange-400 text-white font-bold text-lg">
        a
      </div>
    ),
    date: "Nov 23, 01:00 PM",
    price: "$2,567.88",
    category: "Finance",
    status: "Failed",
  },
];

export default function BasicTableThree() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 px-6 pt-6 pb-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-xl font-bold text-gray-900 dark:text-white">
            Latest Transactions
          </h3>
        </div>
        <div className="relative w-full sm:max-w-xs">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            <svg
              className="size-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </span>
          <input
            type="text"
            placeholder="Search..."
            className="w-full rounded-lg border border-gray-200 py-2.5 pl-10 pr-4 text-sm text-gray-800 focus:border-brand-500 focus:outline-none dark:border-gray-800 dark:bg-white/[0.03] dark:text-white/90"
          />
        </div>
      </div>

      <div className="max-w-full overflow-x-auto">
        <Table>
          {/* Table Header */}
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-6 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                Name
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                Date
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                Price
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                Category
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                Status
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-5 font-medium text-gray-500 text-start text-sm dark:text-gray-400"
              >
                <span className="sr-only">Actions</span>
              </TableCell>
            </TableRow>
          </TableHeader>

          {/* Table Body */}
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {transactions.map((tx) => (
              <TableRow key={tx.id}>
                <TableCell className="px-6 py-5 text-start">
                  <div className="flex items-center gap-3">
                    {tx.logo}
                    <span className="font-medium text-gray-800 text-sm dark:text-white/90">
                      {tx.name}
                    </span>
                  </div>
                </TableCell>
                <TableCell className="px-4 py-5 text-gray-500 text-start text-sm dark:text-gray-400">
                  {tx.date}
                </TableCell>
                <TableCell className="px-4 py-5 text-gray-800 font-medium text-start text-sm dark:text-white/90">
                  {tx.price}
                </TableCell>
                <TableCell className="px-4 py-5 text-gray-500 text-start text-sm dark:text-gray-400">
                  {tx.category}
                </TableCell>
                <TableCell className="px-4 py-5 text-start">
                  <Badge
                    size="sm"
                    variant="light"
                    color={
                      tx.status === "Success"
                        ? "success"
                        : tx.status === "Pending"
                        ? "warning"
                        : "error"
                    }
                  >
                    {tx.status}
                  </Badge>
                </TableCell>
                <TableCell className="px-4 py-5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                  <button className="flex items-center justify-center p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                    <svg className="size-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
                    </svg>
                  </button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      <div className="flex flex-col gap-4 border-t border-gray-100 p-5 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:hover:bg-gray-800 dark:text-gray-400">
          <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Previous
        </button>

        <div className="flex items-center gap-2">
          <button className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-medium text-white">
            1
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800">
            2
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800">
            3
          </button>
        </div>

        <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-800 hover:bg-gray-50 dark:border-gray-800 dark:hover:bg-gray-800 dark:text-white/90">
          Next
          <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
          </svg>
        </button>
      </div>
    </div>
  );
}
