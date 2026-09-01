import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";
import Badge from "../ui/badge/Badge";

const invoicesData = [
  {
    serial: "#DF429",
    date: "April 28, 2016",
    user: "Jenny Wilson",
    amount: "$473.85",
    status: "Complete",
  },
  {
    serial: "#HTY274",
    date: "October 30, 2017",
    user: "Wade Warren",
    amount: "$293.01",
    status: "Complete",
  },
  {
    serial: "#LKE600",
    date: "May 29, 2017",
    user: "Darlene Robertson",
    amount: "$782.01",
    status: "Pending",
  },
  {
    serial: "#HRP447",
    date: "May 20, 2015",
    user: "Arlene McCoy",
    amount: "$202.87",
    status: "Cancelled",
  },
  {
    serial: "#WRH647",
    date: "March 13, 2014",
    user: "Bessie Cooper",
    amount: "$490.51",
    status: "Complete",
  },
];

export default function MarketingRecentInvoices() {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white px-4 pb-3 pt-4 dark:border-gray-800 dark:bg-white/[0.03] sm:px-6">
      <div className="flex flex-col gap-2 mb-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Recent Invoices
          </h3>
        </div>
      </div>
      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="border-gray-100 dark:border-gray-800 border-y">
            <TableRow>
              <TableCell
                isHeader
                className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Serial No:
              </TableCell>
              <TableCell
                isHeader
                className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Close Date
              </TableCell>
              <TableCell
                isHeader
                className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                User
              </TableCell>
              <TableCell
                isHeader
                className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Amount
              </TableCell>
              <TableCell
                isHeader
                className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Status
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-gray-800">
            {invoicesData.map((invoice, index) => (
              <TableRow key={index}>
                <TableCell className="py-4">
                  <p className="font-semibold text-gray-800 text-theme-sm dark:text-white/90">
                    {invoice.serial}
                  </p>
                </TableCell>
                <TableCell className="py-4 text-gray-500 text-theme-sm dark:text-gray-400">
                  {invoice.date}
                </TableCell>
                <TableCell className="py-4 text-gray-500 text-theme-sm dark:text-gray-400">
                  {invoice.user}
                </TableCell>
                <TableCell className="py-4 font-medium text-gray-800 text-theme-sm dark:text-white/90">
                  {invoice.amount}
                </TableCell>
                <TableCell className="py-4">
                  <Badge
                    size="sm"
                    color={
                      invoice.status === "Complete"
                        ? "success"
                        : invoice.status === "Pending"
                        ? "warning"
                        : "error"
                    }
                  >
                    {invoice.status}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
