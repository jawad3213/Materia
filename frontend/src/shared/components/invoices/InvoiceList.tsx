import React, { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";

const invoiceData = [
  { id: 1, invoiceNumber: "#323534", customer: "Lindsey Curtis", creationDate: "August 7, 2028", dueDate: "February 28, 2028", total: "$999", status: "Paid" },
  { id: 2, invoiceNumber: "#323535", customer: "John Doe", creationDate: "July 1, 2028", dueDate: "January 1, 2029", total: "$1200", status: "Unpaid" },
  { id: 3, invoiceNumber: "#323536", customer: "Jane Smith", creationDate: "June 15, 2028", dueDate: "December 15, 2028", total: "$850", status: "Draft" },
  { id: 4, invoiceNumber: "#323537", customer: "Michael Brown", creationDate: "May 10, 2028", dueDate: "November 10, 2028", total: "$1500", status: "Paid" },
  { id: 5, invoiceNumber: "#323538", customer: "Emily Davis", creationDate: "April 5, 2028", dueDate: "October 5, 2028", total: "$700", status: "Unpaid" },
  { id: 6, invoiceNumber: "#323539", customer: "Chris Wilson", creationDate: "March 1, 2028", dueDate: "September 1, 2028", total: "$1100", status: "Paid" },
  { id: 7, invoiceNumber: "#323540", customer: "Jessica Lee", creationDate: "February 20, 2028", dueDate: "August 20, 2028", total: "$950", status: "Draft" },
  { id: 8, invoiceNumber: "#323541", customer: "David Kim", creationDate: "January 15, 2028", dueDate: "July 15, 2028", total: "$1300", status: "Paid" },
  { id: 9, invoiceNumber: "#323542", customer: "Sarah Clark", creationDate: "December 10, 2027", dueDate: "June 10, 2028", total: "$800", status: "Unpaid" },
  { id: 10, invoiceNumber: "#323543", customer: "Matthew Lewis", creationDate: "November 5, 2027", dueDate: "May 5, 2028", total: "$1400", status: "Paid" },
];

export default function InvoiceList() {
  const [activeTab, setActiveTab] = useState("All Invoices");

  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      {/* Header & Controls */}
      <div className="flex flex-col gap-4 border-b border-gray-200 px-6 py-5 xl:flex-row xl:items-center xl:justify-between dark:border-gray-800">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Invoices
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Your most recent invoices list
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          {/* Tabs */}
          <div className="flex items-center gap-1 rounded-lg border border-gray-200 p-1 dark:border-gray-800">
            {["All Invoices", "Unpaid", "Draft"].map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                  activeTab === tab
                    ? "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-white/90"
                    : "text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                }`}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* Search */}
          <div className="relative">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </span>
            <input
              type="text"
              placeholder="Search..."
              className="h-10 w-48 rounded-lg border border-gray-200 pl-10 pr-4 text-sm outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-gray-800 dark:bg-transparent dark:text-white/90 dark:focus:border-brand-500"
            />
          </div>

          {/* Filter & Export Buttons */}
          <button className="flex h-10 items-center gap-2 rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
            Filter
          </button>
          <button className="flex h-10 items-center gap-2 rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
            Export
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <Table>
          <TableHeader className="border-b border-gray-100 dark:border-gray-800">
            <TableRow>
              <TableCell isHeader className="w-12 px-6 py-4">
                <input type="checkbox" className="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900" />
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                Invoice Number
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                <div className="flex items-center gap-1 cursor-pointer">
                  Customer
                  <svg className="w-3 h-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
                  </svg>
                </div>
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                <div className="flex items-center gap-1 cursor-pointer">
                  Creation Date
                  <svg className="w-3 h-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
                  </svg>
                </div>
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                <div className="flex items-center gap-1 cursor-pointer">
                  Due Date
                  <svg className="w-3 h-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
                  </svg>
                </div>
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                Total
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap">
                Status
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 text-sm whitespace-nowrap text-right">
                {/* Empty header for action column */}
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-gray-800">
            {invoiceData.map((invoice) => (
              <TableRow key={invoice.id}>
                <TableCell className="w-12 px-6 py-4">
                  <input type="checkbox" className="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900" />
                </TableCell>
                <TableCell className="px-6 py-4 text-sm font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {invoice.invoiceNumber}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm font-medium text-gray-800 dark:text-white/90 whitespace-nowrap">
                  {invoice.customer}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {invoice.creationDate}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {invoice.dueDate}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm font-medium text-gray-800 dark:text-white/90 whitespace-nowrap">
                  {invoice.total}
                </TableCell>
                <TableCell className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      invoice.status === "Paid"
                        ? "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-500"
                        : invoice.status === "Unpaid"
                        ? "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500"
                        : "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400"
                    }`}
                  >
                    {invoice.status}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 whitespace-nowrap text-right">
                  <button className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z" />
                    </svg>
                  </button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Pagination Footer */}
      <div className="flex items-center justify-between border-t border-gray-200 px-6 py-4 dark:border-gray-800">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Showing <span className="font-medium text-gray-800 dark:text-white/90">1</span> to <span className="font-medium text-gray-800 dark:text-white/90">10</span> of <span className="font-medium text-gray-800 dark:text-white/90">25</span>
        </p>
        <div className="flex items-center gap-2">
          <button className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-medium text-white hover:bg-brand-600">
            1
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors">
            2
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors">
            3
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
}
