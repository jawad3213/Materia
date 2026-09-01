import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import { TrashBinIcon, PencilIcon } from "../../../icons";

const data = [
  {
    id: 1,
    name: "Abram Schleifer",
    position: "Sales Assistant",
    office: "Edinburgh",
    age: 57,
    startDate: "25 Apr, 2027",
    salary: "$89,500",
  },
  {
    id: 2,
    name: "Charlotte Anderson",
    position: "Marketing Manager",
    office: "London",
    age: 42,
    startDate: "12 Mar, 2025",
    salary: "$105,000",
  },
  {
    id: 3,
    name: "Ethan Brown",
    position: "Software Engineer",
    office: "San Francisco",
    age: 30,
    startDate: "01 Jan, 2024",
    salary: "$120,000",
  },
  {
    id: 4,
    name: "Isabella Davis",
    position: "UI/UX Designer",
    office: "Austin",
    age: 29,
    startDate: "18 Jul, 2025",
    salary: "$92,000",
  },
  {
    id: 5,
    name: "James Wilson",
    position: "Data Analyst",
    office: "Chicago",
    age: 28,
    startDate: "20 Sep, 2025",
    salary: "$80,000",
  },
  {
    id: 6,
    name: "Liam Moore",
    position: "DevOps Engineer",
    office: "Boston",
    age: 33,
    startDate: "30 Oct, 2024",
    salary: "$115,000",
  },
  {
    id: 7,
    name: "Mia Garcia",
    position: "Content Strategist",
    office: "Denver",
    age: 27,
    startDate: "12 Dec, 2027",
    salary: "$70,000",
  },
  {
    id: 8,
    name: "Olivia Johnson",
    position: "HR Specialist",
    office: "Los Angeles",
    age: 40,
    startDate: "08 Nov, 2026",
    salary: "$75,000",
  },
  {
    id: 9,
    name: "Sophia Martinez",
    position: "Product Manager",
    office: "New York",
    age: 35,
    startDate: "15 Jun, 2026",
    salary: "$95,000",
  },
  {
    id: 10,
    name: "William Smith",
    position: "Financial Analyst",
    office: "Seattle",
    age: 38,
    startDate: "03 Feb, 2026",
    salary: "$88,000",
  },
];

export default function DataTableTwo() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Top Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-6 py-5 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 text-sm font-medium text-gray-500 dark:text-gray-400">
          Show
          <select className="rounded-lg border border-gray-200 bg-transparent px-3 py-1.5 text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300">
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
          entries
        </div>

        <div className="relative">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400"
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
          <input
            type="text"
            placeholder="Search..."
            className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-4 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
          />
        </div>
      </div>

      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="bg-transparent border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              {[
                { name: "User", sortable: true },
                { name: "Position", sortable: true },
                { name: "Office", sortable: true },
                { name: "Age", sortable: true },
                { name: "Start Date", sortable: true },
                { name: "Salary", sortable: true },
                { name: "Action", sortable: false },
              ].map((header, i) => (
                <TableCell
                  key={i}
                  isHeader
                  className="px-6 py-4 font-medium text-gray-900 text-start text-xs dark:text-white"
                >
                  <div className="flex items-center justify-between gap-2">
                    {header.name}
                    {header.sortable && (
                      <button className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                        <svg
                          className="size-3.5"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M7 15l5 5 5-5M7 9l5-5 5 5"
                          />
                        </svg>
                      </button>
                    )}
                  </div>
                </TableCell>
              ))}
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {data.map((row) => (
              <TableRow key={row.id}>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                    {row.name}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.position}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.office}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.age}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.startDate}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                    {row.salary}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start">
                  <div className="flex items-center gap-3">
                    <button className="text-gray-400 hover:text-red-500">
                      <TrashBinIcon className="size-5" />
                    </button>
                    <button className="text-gray-400 hover:text-brand-500">
                      <PencilIcon className="size-5" />
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Bottom Pagination */}
      <div className="flex flex-col gap-4 border-t border-gray-100 px-6 py-5 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2">
          <button className="flex h-9 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            Previous
          </button>
          <button className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-500 text-white">
            1
          </button>
          <button className="flex h-9 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            Next
          </button>
        </div>

        <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
          Showing 1 to 10 of 10 entries
        </p>
      </div>
    </div>
  );
}
