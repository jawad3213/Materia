import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";

const invoices = [
  {
    id: 1,
    name: "Invoice #012 - May 2024",
    date: "May 01, 2024",
    price: "$120.00",
    plan: "Starter Plan",
    status: "Paid",
  },
  {
    id: 2,
    name: "Invoice #013 - June 2024",
    date: "June 01, 2024",
    price: "$120.00",
    plan: "Starter Plan",
    status: "Paid",
  },
  {
    id: 3,
    name: "Invoice #014 - July 2024",
    date: "July 01, 2024",
    price: "$120.00",
    plan: "Starter Plan",
    status: "Unpaid",
  },
  {
    id: 4,
    name: "Invoice #015 - August 2024",
    date: "August 01, 2024",
    price: "$250.00",
    plan: "Pro Plan",
    status: "Paid",
  },
  {
    id: 5,
    name: "Invoice #016 - September 2024",
    date: "September 01, 2024",
    price: "$250.00",
    plan: "Pro Plan",
    status: "Paid",
  },
];

export default function BillingInvoices() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Invoices
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Access all your previous invoices.
          </p>
        </div>
        <button className="flex items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Download All
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <Table>
          <TableHeader className="border-b border-gray-100 dark:border-gray-800">
            <TableRow>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Name
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Date
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Price
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Plan
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Status
              </TableCell>
              <TableCell isHeader className="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 whitespace-nowrap text-right">
                Action
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-gray-800">
            {invoices.map((invoice) => (
              <TableRow key={invoice.id}>
                <TableCell className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded bg-gray-100 dark:bg-gray-800 shrink-0">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M7 2H13.5L20 8.5V20C20 20.5304 19.7893 21.0391 19.4142 21.4142C19.0391 21.7893 18.5304 22 18 22H6C5.46957 22 4.96086 21.7893 4.58579 21.4142C4.21071 21.0391 4 20.5304 4 20V5C4 4.20435 4.31607 3.44129 4.87868 2.87868C5.44129 2.31607 6.20435 2 7 2ZM13 3.5V9H18.5L13 3.5ZM6 20H18V11H11V3.5C9.67392 3.5 8.40215 4.02678 7.46447 4.96447C6.52678 5.90215 6 7.17392 6 8.5V20ZM10 13V18H8.5V13H10ZM13.5 13C14.163 13 14.7989 13.2634 15.2678 13.7322C15.7366 14.2011 16 14.837 16 15.5C16 16.163 15.7366 16.7989 15.2678 17.2678C14.7989 17.7366 14.163 18 13.5 18H11.5V13H13.5ZM13.5 16.5C13.7652 16.5 14.0196 16.3946 14.2071 16.2071C14.3946 16.0196 14.5 15.7652 14.5 15.5C14.5 15.2348 14.3946 14.9804 14.2071 14.7929C14.0196 14.6054 13.7652 14.5 13.5 14.5H13V16.5H13.5Z" fill="#F44336"/>
                      </svg>
                    </div>
                    <span className="text-sm font-medium text-gray-800 dark:text-white/90 whitespace-nowrap">
                      {invoice.name}
                    </span>
                  </div>
                </TableCell>
                <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {invoice.date}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm font-medium text-gray-800 dark:text-white/90 whitespace-nowrap">
                  {invoice.price}
                </TableCell>
                <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {invoice.plan}
                </TableCell>
                <TableCell className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      invoice.status === "Paid"
                        ? "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-500"
                        : "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500"
                    }`}
                  >
                    {invoice.status}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 whitespace-nowrap text-right">
                  <div className="flex items-center justify-end gap-2">
                    <button className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800">
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                    </button>
                    <button className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800">
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between border-t border-gray-200 px-6 py-4 dark:border-gray-800">
        <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Previous
        </button>
        <div className="flex items-center gap-2">
          <button className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-medium text-white hover:bg-brand-600">
            1
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800">
            2
          </button>
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800">
            3
          </button>
        </div>
        <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-gray-800">
          Next
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  );
}
