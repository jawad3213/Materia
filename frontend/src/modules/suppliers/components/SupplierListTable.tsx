import React, { useEffect, useState, useRef } from "react";
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
import type { Supplier } from "../types/Supplier";
import Pagination from "../../../shared/components/ui/Pagination";
import SupplierFilters from "./SupplierFilters";
import SupplierCard from "./SupplierCard";
import SupplierInfoModal from "./SupplierInfoModal";

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
  const [searchSuppliers, setSearchSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedSuppliers, setSelectedSuppliers] = useState<string[]>([]);
  const [supplierToDelete, setSupplierToDelete] = useState<SupplierListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Filter & Search states
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState("");
  const [filterCurrency, setFilterCurrency] = useState("");
  const [filterCountry, setFilterCountry] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  // Pagination state
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Modal for supplier quick info
  const [selectedSupplierForModal, setSelectedSupplierForModal] = useState<Supplier | null>(null);
  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);

  const activeFiltersCount = [filterStatus, filterCurrency, filterCountry].filter(Boolean).length;
  const isFirstMount = useRef(true);

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }
    const timer = setTimeout(() => {
      if (page === 0) {
        setRefreshTrigger((prev) => prev + 1);
      } else {
        setPage(0);
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [searchKeyword]);

  useEffect(() => {
    fetchSuppliers();
  }, [page, size, refreshTrigger]);

  const fetchSuppliers = async () => {
    try {
      setLoading(true);

      let res;
      if (searchKeyword.trim()) {
        res = await supplierApi.searchAdvancedList({
          code: searchKeyword.trim(),
          name: searchKeyword.trim(),
          description: searchKeyword.trim(),
          contactPerson: searchKeyword.trim(),
          contactEmail: searchKeyword.trim(),
          fullAddress: searchKeyword.trim(),
          status: filterStatus || undefined,
          currencyCode: filterCurrency || undefined,
          country: filterCountry || undefined,
        }, page, size);
        setSearchSuppliers(res.data.content || []);
      } else if (!filterStatus && !filterCurrency && !filterCountry) {
        res = await supplierApi.getAllList(page, size);
        setSuppliers(res.data.content || []);
      } else {
        res = await supplierApi.filterList({
          status: filterStatus || undefined,
          currencyCode: filterCurrency || undefined,
          country: filterCountry || undefined,
        }, page, size);
        setSuppliers(res.data.content || []);
      }

      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      console.error("Failed to load suppliers:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyFilters = () => {
    setIsFilterOpen(false);
    if (page === 0) {
      setRefreshTrigger((prev) => prev + 1);
    } else {
      setPage(0);
    }
  };

  const handleClearFilters = () => {
    setFilterStatus("");
    setFilterCurrency("");
    setFilterCountry("");
    setIsFilterOpen(false);
    if (page === 0) {
      setRefreshTrigger((prev) => prev + 1);
    } else {
      setPage(0);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!supplierToDelete) return;
    try {
      setIsDeleting(true);
      await supplierApi.delete(supplierToDelete.id);
      setSuppliers(suppliers.filter(s => s.id !== supplierToDelete.id));
      setTotalElements((prev) => Math.max(0, prev - 1));
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
    <div className="rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Suppliers List
          </h3>
        </div>
        <div className="flex items-center gap-3 flex-wrap justify-end">
          {/* Search bar */}
          <div className="relative hidden sm:block">
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 size-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
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
              placeholder="Search suppliers..."
              value={searchKeyword}
              onChange={(e) => {
                setSearchKeyword(e.target.value);
                if (e.target.value === "") {
                  fetchSuppliers(filterStatus, filterCurrency, filterCountry, "");
                }
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  fetchSuppliers();
                }
              }}
              className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-8 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
            />
            {searchKeyword && (
              <button
                type="button"
                onClick={() => {
                  setSearchKeyword("");
                  fetchSuppliers(filterStatus, filterCurrency, filterCountry, "");
                }}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
              >
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>

          {/* Filter Dropdown Toggle Button */}
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsFilterOpen(!isFilterOpen)}
            >
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
            {activeFiltersCount > 0 && (
              <span className="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white shadow-sm">
                {activeFiltersCount}
              </span>
            )}

            <SupplierFilters
              isOpen={isFilterOpen}
              onClose={() => setIsFilterOpen(false)}
              filterStatus={filterStatus}
              setFilterStatus={setFilterStatus}
              filterCurrency={filterCurrency}
              setFilterCurrency={setFilterCurrency}
              filterCountry={filterCountry}
              setFilterCountry={setFilterCountry}
              onApply={handleApplyFilters}
              onClear={handleClearFilters}
            />
          </div>

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

      {/* Active Filter Pills Bar */}
      {activeFiltersCount > 0 && (
        <div className="flex flex-wrap items-center gap-2 px-5 py-2.5 bg-gray-50/75 border-b border-gray-100 dark:bg-gray-900/40 dark:border-white/[0.05]">
          <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
            Active filters:
          </span>
          {filterStatus && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Status: {filterStatus}
              <button
                type="button"
                onClick={() => {
                  setFilterStatus("");
                  if (page === 0) {
                    setRefreshTrigger((prev) => prev + 1);
                  } else {
                    setPage(0);
                  }
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterCurrency && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Currency: {filterCurrency}
              <button
                type="button"
                onClick={() => {
                  setFilterCurrency("");
                  if (page === 0) {
                    setRefreshTrigger((prev) => prev + 1);
                  } else {
                    setPage(0);
                  }
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterCountry && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Country: {filterCountry}
              <button
                type="button"
                onClick={() => {
                  setFilterCountry("");
                  if (page === 0) {
                    setRefreshTrigger((prev) => prev + 1);
                  } else {
                    setPage(0);
                  }
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          <button
            type="button"
            onClick={handleClearFilters}
            className="text-xs font-semibold text-gray-500 hover:text-error-500 ml-2 transition-colors"
          >
            Clear all
          </button>
        </div>
      )}

      {/* Search mode: Card grid view */}
      {searchKeyword.trim() ? (
        <div className="p-5">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="flex flex-col items-center gap-3">
                <svg className="animate-spin h-8 w-8 text-brand-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <p className="text-sm text-gray-500 dark:text-gray-400">Searching suppliers...</p>
              </div>
            </div>
          ) : searchSuppliers.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16">
              <div className="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gray-50 dark:bg-gray-800">
                <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-1">No suppliers found</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm text-center">
                No suppliers match "{searchKeyword}". Try another keyword or clear search.
              </p>
            </div>
          ) : (
            <>
              <div className="mb-4 flex items-center gap-2">
                <svg className="w-4 h-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  Found <span className="font-semibold text-gray-800 dark:text-white/90">{searchSuppliers.length}</span> result{searchSuppliers.length !== 1 ? 's' : ''} for "{searchKeyword}"
                </span>
              </div>
              <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
                {searchSuppliers.map((supplier) => (
                  <SupplierCard
                    key={supplier.id}
                    supplier={supplier}
                    highlightKeyword={searchKeyword}
                    onCardClick={(s) => {
                      setSelectedSupplierForModal(s);
                      setIsInfoModalOpen(true);
                    }}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      ) : (
        /* Default mode: Table list view */
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
                        <Link to={`/suppliers/view/${supplier.id}`}>
                          <button 
                            className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                            title="View Supplier"
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                          </button>
                        </Link>
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
      )}

      {/* Pagination Controls */}
      <div className="flex flex-col sm:flex-row items-center justify-between px-5 py-4 gap-4 border-t border-gray-100 dark:border-white/[0.05]">
        <div className="text-sm text-gray-500 dark:text-gray-400">
          Showing <span className="font-medium text-gray-800 dark:text-white/90">{totalElements === 0 ? 0 : page * size + 1}</span> to <span className="font-medium text-gray-800 dark:text-white/90">{Math.min((page + 1) * size, totalElements)}</span> of <span className="font-medium text-gray-800 dark:text-white/90">{totalElements}</span> results
        </div>
        
        <Pagination 
          currentPage={page} 
          totalPages={Math.max(1, totalPages)} 
          onPageChange={(newPage) => setPage(newPage)} 
        />
      </div>

      <DeleteConfirmModal
        isOpen={!!supplierToDelete}
        onClose={() => setSupplierToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Danger Alert!"
        message={`Are you sure you want to delete the supplier "${supplierToDelete?.name}"?`}
        isDeleting={isDeleting}
      />

      {/* Quick Info Modal for tapping card */}
      <SupplierInfoModal
        isOpen={isInfoModalOpen}
        onClose={() => setIsInfoModalOpen(false)}
        supplier={selectedSupplierForModal}
      />
    </div>
  );
}
