import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../../shared/components/ui/table";
import Badge from "../../../shared/components/ui/badge/Badge";
import Checkbox from "../../../shared/components/form/input/Checkbox";
import Button from "../../../shared/components/ui/button/Button";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import { supplierApi } from "../services/supplierApi";
import type { SupplierListItem } from "../types/SupplierListItem";

const colorClasses: Record<string, string> = {
  red: "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  orange: "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  purple: "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  green: "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
  blue: "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-500",
};

const colors = ["red", "orange", "purple", "green", "blue"];

export default function SupplierListTable() {
  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedSuppliers, setSelectedSuppliers] = useState<string[]>([]);
  const [supplierToDelete, setSupplierToDelete] = useState<SupplierListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const fetchSuppliers = async () => {
    try {
      setLoading(true);
      const res = await supplierApi.getAll();
      setSuppliers(res.data);
    } catch (err) {
      console.error("Failed to load suppliers:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!supplierToDelete) return;
    try {
      setIsDeleting(true);
      await supplierApi.delete(supplierToDelete.id);
      setSuppliers(suppliers.filter(s => s.id !== supplierToDelete.id));
      setSupplierToDelete(null);
    } catch (err) {
      console.error("Failed to delete supplier:", err);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleSelectAll = () => {
    if (selectedSuppliers.length === suppliers.length && suppliers.length > 0) {
      setSelectedSuppliers([]);
    } else {
      setSelectedSuppliers(suppliers.map(s => s.id));
    }
  };

  const handleSelectOne = (id: string) => {
    if (selectedSuppliers.includes(id)) {
      setSelectedSuppliers(selectedSuppliers.filter(supplierId => supplierId !== id));
    } else {
      setSelectedSuppliers([...selectedSuppliers, id]);
    }
  };

  const getInitials = (name: string) => {
    if (!name) return "S";
    return name.substring(0, 2).toUpperCase();
  };

  const getColorForSupplier = (name: string) => {
    if (!name) return "blue";
    const sum = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return colors[sum % colors.length];
  };

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Suppliers List
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
          <Link to="/suppliers/create-supplier">
            <Button size="sm">
              <span className="flex items-center gap-2">
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Supplier
              </span>
            </Button>
          </Link>
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
                <Checkbox 
                  checked={suppliers.length > 0 && selectedSuppliers.length === suppliers.length} 
                  onChange={handleSelectAll} 
                />
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Supplier
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Contact Person
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Location
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Currency
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
            {loading ? (
              <TableRow>
                <td colSpan={6} className="px-5 py-4 text-center text-gray-500">
                  Loading suppliers...
                </td>
              </TableRow>
            ) : suppliers.length === 0 ? (
              <TableRow>
                <td colSpan={6} className="px-5 py-4 text-center text-gray-500">
                  No suppliers found. Create one to get started.
                </td>
              </TableRow>
            ) : (
              suppliers.map((supplier) => (
                <TableRow key={supplier.id} className={selectedSuppliers.includes(supplier.id) ? "bg-gray-50 dark:bg-white/[0.02]" : ""}>
                  <TableCell className="px-5 py-4">
                    <Checkbox 
                      checked={selectedSuppliers.includes(supplier.id)} 
                      onChange={() => handleSelectOne(supplier.id)} 
                    />
                  </TableCell>
                  <TableCell className="px-4 py-4 text-start">
                    <div className="flex items-center gap-3">
                      <div
                        className={`flex h-10 w-10 items-center justify-center rounded-full font-medium ${
                          colorClasses[getColorForSupplier(supplier.name)]
                        }`}
                      >
                        {getInitials(supplier.name)}
                      </div>
                      <div>
                        <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          {supplier.name}
                        </span>
                        <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                          {supplier.contactEmail}
                        </span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell className="px-4 py-4 text-start">
                    <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {supplier.contactPerson}
                    </span>
                    <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                      {supplier.contactPhone}
                    </span>
                  </TableCell>
                  <TableCell className="px-4 py-4 text-start">
                    <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {supplier.city}
                    </span>
                    <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                      {supplier.country}
                    </span>
                  </TableCell>
                  <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    <Badge size="sm" variant="light" color="success">
                      {supplier.currencyCode}
                    </Badge>
                  </TableCell>
                  <TableCell className="px-4 py-4">
                    <div className="flex items-center gap-2">
                      <Link to={`/suppliers/edit/${supplier.id}`}>
                        <button 
                          className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                          title="Update Supplier"
                        >
                          <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                      </Link>
                      <button 
                        onClick={() => setSupplierToDelete(supplier)}
                        className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-error-500 hover:bg-error-50 dark:hover:bg-error-500/10 dark:hover:text-error-500 transition-colors"
                        title="Delete Supplier"
                      >
                        <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <DeleteConfirmModal
        isOpen={!!supplierToDelete}
        onClose={() => setSupplierToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Danger Alert!"
        message={`Are you sure you want to delete the supplier "${supplierToDelete?.name}"?`}
        isDeleting={isDeleting}
      />
    </div>
  );
}
