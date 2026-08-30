import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import Badge from "../../ui/badge/Badge";
import Checkbox from "../../form/input/Checkbox";
import Button from "../../ui/button/Button";

interface Order {
  id: string;
  customer: {
    name: string;
    email: string;
    initials: string;
    color: "red" | "orange" | "purple" | "green";
  };
  product: string;
  value: string;
  closeDate: string;
  status: "Complete" | "Pending" | "Cancel";
}

const tableData: Order[] = [
  {
    id: "DE124321",
    customer: {
      name: "John Doe",
      email: "johndoe@gmail.com",
      initials: "JD",
      color: "red",
    },
    product: "Software License",
    value: "$18,50.34",
    closeDate: "2024-06-15",
    status: "Complete",
  },
  {
    id: "DE124322",
    customer: {
      name: "Jane Smith",
      email: "janesmith@gmail.com",
      initials: "JS",
      color: "orange",
    },
    product: "Cloud Hosting",
    value: "$12,99.00",
    closeDate: "2024-06-18",
    status: "Pending",
  },
  {
    id: "DE124323",
    customer: {
      name: "Michael Brown",
      email: "michaelbrown@gmail.com",
      initials: "MB",
      color: "orange",
    },
    product: "Web Domain",
    value: "$9,50.00",
    closeDate: "2024-06-20",
    status: "Cancel",
  },
  {
    id: "DE124324",
    customer: {
      name: "Alice Johnson",
      email: "alicejohnson@gmail.com",
      initials: "AJ",
      color: "purple",
    },
    product: "SSL Certificate",
    value: "$2,30.45",
    closeDate: "2024-06-25",
    status: "Pending",
  },
  {
    id: "DE124325",
    customer: {
      name: "Robert Lee",
      email: "robertlee@gmail.com",
      initials: "RL",
      color: "green",
    },
    product: "Premium Support",
    value: "$15,20.00",
    closeDate: "2024-06-30",
    status: "Complete",
  },
];

const colorClasses = {
  red: "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  orange: "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  purple: "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  green: "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
};

export default function BasicTableTwo() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Recent Orders
          </h3>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" size="sm">
            <span className="flex items-center gap-2">
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
                  d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                />
              </svg>
              Filter
            </span>
          </Button>
          <Button variant="outline" size="sm">
            See all
          </Button>
        </div>
      </div>

      <div className="max-w-full overflow-x-auto">
        <Table>
          {/* Table Header */}
          <TableHeader className="bg-gray-50/50 border-b border-gray-100 dark:bg-gray-900/50 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400 w-[50px]"
              >
                <Checkbox checked={false} onChange={() => {}} />
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Deal ID
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Customer
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Product/Service
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Deal Value
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Close Date
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Status
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Action
              </TableCell>
            </TableRow>
          </TableHeader>

          {/* Table Body */}
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {tableData.map((order) => (
              <TableRow key={order.id}>
                <TableCell className="px-5 py-4">
                  <Checkbox checked={false} onChange={() => {}} />
                </TableCell>
                <TableCell className="px-4 py-4 text-gray-800 text-theme-sm dark:text-white/90">
                  {order.id}
                </TableCell>
                <TableCell className="px-4 py-4 text-start">
                  <div className="flex items-center gap-3">
                    <div
                      className={`flex h-10 w-10 items-center justify-center rounded-full font-medium ${
                        colorClasses[order.customer.color]
                      }`}
                    >
                      {order.customer.initials}
                    </div>
                    <div>
                      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {order.customer.name}
                      </span>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        {order.customer.email}
                      </span>
                    </div>
                  </div>
                </TableCell>
                <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {order.product}
                </TableCell>
                <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {order.value}
                </TableCell>
                <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {order.closeDate}
                </TableCell>
                <TableCell className="px-4 py-4 text-start">
                  <Badge
                    size="sm"
                    variant="light"
                    color={
                      order.status === "Complete"
                        ? "success"
                        : order.status === "Pending"
                        ? "warning"
                        : "error"
                    }
                  >
                    {order.status}
                  </Badge>
                </TableCell>
                <TableCell className="px-4 py-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                  <button className="flex items-center justify-center p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                    <svg
                      className="size-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                  </button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
