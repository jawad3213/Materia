import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";

const products = [
  { id: 1, name: "Macbook pro 13\"", qty: 1, cost: "$1200", discount: "0%", total: "$1200" },
  { id: 2, name: "Apple Watch Ultra", qty: 1, cost: "$300", discount: "50%", total: "$150" },
  { id: 3, name: "iPhone 15 Pro Max", qty: 2, cost: "$800", discount: "0%", total: "$1600" },
  { id: 4, name: "iPad Pro 3rd Gen", qty: 1, cost: "$900", discount: "0%", total: "$900" },
];

export default function TransactionOrderDetails() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Order Details
        </h3>
      </div>
      <div className="p-6">
        <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-gray-800">
          <Table>
            <TableHeader className="bg-gray-50 dark:bg-gray-900 rounded-t-xl">
              <TableRow>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  S. No.
                </TableCell>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  Products
                </TableCell>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  Quantity
                </TableCell>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  Unit Cost
                </TableCell>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  Discount
                </TableCell>
                <TableCell isHeader className="px-6 py-4 font-medium text-gray-800 dark:text-white/90 text-sm whitespace-nowrap">
                  Total
                </TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-gray-800">
              {products.map((product, index) => (
                <TableRow key={product.id}>
                  <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {index + 1}
                  </TableCell>
                  <TableCell className="px-6 py-4 text-sm font-medium text-gray-800 dark:text-white/90">
                    {product.name}
                  </TableCell>
                  <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {product.qty}
                  </TableCell>
                  <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {product.cost}
                  </TableCell>
                  <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {product.discount}
                  </TableCell>
                  <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {product.total}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        <div className="mt-8 flex justify-end">
          <div className="w-full max-w-sm">
            <h4 className="mb-4 text-sm font-semibold text-gray-800 dark:text-white/90">
              Order summary
            </h4>
            <div className="flex flex-col gap-3 text-sm">
              <div className="flex justify-between text-gray-500 dark:text-gray-400">
                <span>Sub Total</span>
                <span className="font-medium text-gray-800 dark:text-white/90">$3,850</span>
              </div>
              <div className="flex justify-between text-gray-500 dark:text-gray-400">
                <span>Vat (10%):</span>
                <span className="font-medium text-gray-800 dark:text-white/90">$385</span>
              </div>
              <div className="flex justify-between mt-2 text-base font-semibold text-gray-800 dark:text-white/90">
                <span>Total</span>
                <span>$4,235</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
