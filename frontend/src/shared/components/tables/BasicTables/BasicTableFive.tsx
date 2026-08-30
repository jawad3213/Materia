import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";

const orders = [
  {
    id: 1,
    products: "TailGrids",
    category: "UI Kits",
    countryImage: "/images/country/country-01.svg",
    cr: "Dashboard",
    value: "$12,499",
  },
  {
    id: 2,
    products: "GrayGrids",
    category: "Templates",
    countryImage: "/images/country/country-02.svg",
    cr: "Dashboard",
    value: "$5498",
  },
  {
    id: 3,
    products: "Uideck",
    category: "Templates",
    countryImage: "/images/country/country-03.svg",
    cr: "Dashboard",
    value: "$4621",
  },
  {
    id: 4,
    products: "FormBold",
    category: "SaaS",
    countryImage: "/images/country/country-04.svg",
    cr: "Dashboard",
    value: "$13843",
  },
  {
    id: 5,
    products: "NextAdmin",
    category: "Templates",
    countryImage: "/images/country/country-05.svg",
    cr: "Dashboard",
    value: "$7523",
  },
  {
    id: 6,
    products: "Form Builder",
    category: "Templates",
    countryImage: "/images/country/country-06.svg",
    cr: "Dashboard",
    value: "$1,377",
  },
  {
    id: 7,
    products: "AyroUI",
    category: "Templates",
    countryImage: "/images/country/country-07.svg",
    cr: "Dashboard",
    value: "$599.00",
  },
];

export default function BasicTableFive() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="flex items-center justify-between px-6 py-5">
        <h3 className="text-lg font-bold text-gray-800 dark:text-white/90">
          Recent Orders
        </h3>
        <div className="flex items-center gap-3">
          <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
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
          </button>
          <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            See all
          </button>
        </div>
      </div>

      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="bg-transparent border-y border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-6 py-4 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Products
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-3 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Category
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-3 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Country
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-3 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                CR
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-3 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Value
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {orders.map((order) => (
              <TableRow key={order.id}>
                <TableCell className="px-6 py-3 text-start">
                  <span className="font-medium text-gray-900 text-sm dark:text-white/90">
                    {order.products}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-3 text-start">
                  <span className="text-sm text-gray-500 dark:text-gray-400">
                    {order.category}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-3 text-start">
                  <img
                    src={order.countryImage}
                    alt="country flag"
                    className="h-6 w-6 rounded-full object-cover"
                  />
                </TableCell>
                <TableCell className="px-6 py-3 text-start">
                  <span className="text-sm text-gray-500 dark:text-gray-400">
                    {order.cr}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-3 text-start">
                  <span className="text-sm font-medium text-[#219653]">
                    {order.value}
                  </span>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
