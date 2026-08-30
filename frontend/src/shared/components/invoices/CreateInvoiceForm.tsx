import React, { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";

const initialProducts = [
  { id: 1, name: "Macbook pro 13\"", qty: 1, cost: 1200, discount: 0, total: 1200 },
  { id: 2, name: "Apple Watch Ultra", qty: 1, cost: 300, discount: 50, total: 150 },
  { id: 3, name: "iPhone 15 Pro Max", qty: 2, cost: 800, discount: 0, total: 1600 },
  { id: 4, name: "iPad Pro 3rd Gen", qty: 1, cost: 900, discount: 0, total: 900 },
];

export default function CreateInvoiceForm() {
  const [products, setProducts] = useState(initialProducts);

  const subTotal = products.reduce((sum, p) => sum + p.total, 0);
  const vat = subTotal * 0.1;
  const grandTotal = subTotal + vat;

  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Create Invoice
        </h3>
      </div>
      
      <div className="p-6">
        {/* Form Fields */}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
          <div>
            <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
              Invoice Number
            </label>
            <input
              type="text"
              defaultValue="WP-3434434"
              className="w-full rounded-lg border border-gray-200 bg-transparent px-4 py-3 text-sm text-gray-500 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-400 dark:focus:border-brand-500"
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
              Customer Name
            </label>
            <input
              type="text"
              defaultValue="John Deniyal"
              className="w-full rounded-lg border border-gray-200 bg-transparent px-4 py-3 text-sm text-gray-500 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-400 dark:focus:border-brand-500"
            />
          </div>
          <div className="sm:col-span-2">
            <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
              Customer Address
            </label>
            <input
              type="text"
              placeholder="Enter customer address"
              className="w-full rounded-lg border border-gray-200 bg-transparent px-4 py-3 text-sm text-gray-500 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-400 dark:focus:border-brand-500"
            />
          </div>
        </div>

        {/* Product Table */}
        <div className="mt-8 rounded-xl border border-gray-200 dark:border-gray-800">
          <div className="overflow-x-auto">
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
                  <TableCell isHeader className="px-6 py-4 text-sm whitespace-nowrap">
                    {/* Action */}
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
                      ${product.cost}
                    </TableCell>
                    <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                      {product.discount}%
                    </TableCell>
                    <TableCell className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                      ${product.total.toFixed(2)}
                    </TableCell>
                    <TableCell className="px-6 py-4 text-right">
                      <button className="text-gray-500 hover:text-error-500 dark:text-gray-400 transition-colors">
                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>

        {/* Add Product Block */}
        <div className="mt-8 rounded-xl border border-gray-100 bg-gray-50/50 p-6 dark:border-gray-800 dark:bg-gray-900/50">
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-12 lg:items-end">
            <div className="lg:col-span-3">
              <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
                Product Name
              </label>
              <input
                type="text"
                placeholder="Enter product name"
                className="w-full rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:focus:border-brand-500"
              />
            </div>
            <div className="lg:col-span-3">
              <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
                Price
              </label>
              <input
                type="text"
                placeholder="Enter product price"
                className="w-full rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:focus:border-brand-500"
              />
            </div>
            <div className="lg:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
                Quantity
              </label>
              <div className="flex items-center justify-between rounded-lg border border-gray-200 bg-white px-3 py-2.5 dark:border-gray-800 dark:bg-gray-900">
                <button className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                  </svg>
                </button>
                <span className="text-sm font-medium text-gray-800 dark:text-white/90">1</span>
                <button className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                </button>
              </div>
            </div>
            <div className="lg:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-800 dark:text-white/90">
                Discount
              </label>
              <div className="relative">
                <select className="w-full appearance-none rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:focus:border-brand-500">
                  <option>0%</option>
                  <option>10%</option>
                  <option>20%</option>
                  <option>50%</option>
                </select>
                <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-500">
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </span>
              </div>
            </div>
            <div className="lg:col-span-2">
              <button className="w-full rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 transition-colors">
                Save Product
              </button>
            </div>
          </div>
          <div className="mt-4 flex items-start gap-2 text-sm text-gray-500 dark:text-gray-400">
            <svg className="mt-0.5 h-4 w-4 shrink-0 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p>After filling in the product details, press Enter/Return or click 'Save Product' to add it to the list.</p>
          </div>
        </div>

        {/* Order Summary */}
        <div className="mt-8 flex justify-end">
          <div className="w-full max-w-sm border-b border-gray-200 pb-6 dark:border-gray-800">
            <h4 className="mb-4 text-sm font-semibold text-gray-800 dark:text-white/90">
              Order summary
            </h4>
            <div className="flex flex-col gap-3 text-sm">
              <div className="flex justify-between text-gray-500 dark:text-gray-400">
                <span>Sub Total</span>
                <span className="font-medium text-gray-800 dark:text-white/90">${subTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-gray-500 dark:text-gray-400">
                <span>Vat (10%):</span>
                <span className="font-medium text-gray-800 dark:text-white/90">${vat.toFixed(2)}</span>
              </div>
              <div className="flex justify-between mt-1 text-base font-semibold text-gray-800 dark:text-white/90">
                <span>Total</span>
                <span>${grandTotal.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-6 flex flex-wrap items-center justify-end gap-3">
          <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            Preview Invoice
          </button>
          <button className="flex items-center gap-2 rounded-lg bg-brand-500 px-6 py-2.5 text-sm font-medium text-white hover:bg-brand-600 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
            </svg>
            Save Invoice
          </button>
        </div>
      </div>
    </div>
  );
}
