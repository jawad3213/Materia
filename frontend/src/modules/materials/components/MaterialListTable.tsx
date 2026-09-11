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
import { materialApi } from "../services/materialApi";
import { categoryApi } from "../../categories/services/categoryApi";
import type { MaterialListItem } from "../types/MaterialListItem";
import Pagination from "../../../shared/components/ui/Pagination";
import MaterialCard from "./MaterialCard";
import MaterialFilters from "./MaterialFilters";
import MaterialInfoModal from "./MaterialInfoModal";
import StockStatCards, { type StockFilterType } from "./StockStatCards";
import ReorderRecommendationModal from "./ReorderRecommendationModal";

const colorClasses: Record<string, string> = {
  red: "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  orange: "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  purple: "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  green: "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
  blue: "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-500",
};

const colors = ["red", "orange", "purple", "green", "blue"];

export default function MaterialListTable() {
  const [materials, setMaterials] = useState<MaterialListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedMaterials, setSelectedMaterials] = useState<string[]>([]);
  const [materialToDelete, setMaterialToDelete] = useState<MaterialListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  
  // Filter state
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [filterCategoryId, setFilterCategoryId] = useState("");
  const [filterCategoryName, setFilterCategoryName] = useState("");
  const [filterMaterialType, setFilterMaterialType] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  // Stock filter tab state
  const [activeStockFilter, setActiveStockFilter] = useState<StockFilterType>("ALL");
  const [stockCounts, setStockCounts] = useState({
    total: 0,
    reorderNeeded: 0,
    critical: 0,
    outOfStock: 0,
  });

  // Reorder modal state
  const [reorderMaterialId, setReorderMaterialId] = useState<string | null>(null);
  const [isReorderModalOpen, setIsReorderModalOpen] = useState(false);
  
  // Modal state
  const [selectedMaterialForModal, setSelectedMaterialForModal] = useState<MaterialListItem | null>(null);
  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const isFirstMount = useRef(true);
  const activeFiltersCount = [filterCategoryId, filterMaterialType, filterStatus].filter(Boolean).length;

  useEffect(() => {
    fetchStockCounts();
  }, [refreshTrigger]);

  const fetchStockCounts = async () => {
    try {
      const [reorderRes, criticalRes, outRes] = await Promise.all([
        materialApi.getReorderNeeded(),
        materialApi.getCriticalStock(),
        materialApi.getOutOfStock(),
      ]);

      setStockCounts((prev) => ({
        ...prev,
        reorderNeeded: reorderRes.data?.length || 0,
        critical: criticalRes.data?.length || 0,
        outOfStock: outRes.data?.length || 0,
      }));
    } catch (err) {
      console.error("Failed to load stock counts:", err);
    }
  };

  const handleSelectStockFilter = (filter: StockFilterType) => {
    setActiveStockFilter(filter);
    setPage(0);
    setRefreshTrigger((prev) => prev + 1);
  };

  useEffect(() => {
    if (filterCategoryId) {
      categoryApi.getById(filterCategoryId)
        .then(res => setFilterCategoryName(res.data.name))
        .catch(() => setFilterCategoryName(filterCategoryId));
    } else {
      setFilterCategoryName("");
    }
  }, [filterCategoryId]);

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }
    const timer = setTimeout(() => {
      if (page === 0) {
        setRefreshTrigger(prev => prev + 1);
      } else {
        setPage(0);
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [searchKeyword]);

  useEffect(() => {
    fetchMaterials();
  }, [page, size, refreshTrigger, activeStockFilter]);

  const fetchMaterials = async () => {
    try {
      setLoading(true);

      // 1. If a stock filter tab is active (Reorder, Critical, or Out of Stock)
      if (activeStockFilter === "REORDER_NEEDED") {
        const res = await materialApi.getReorderNeeded();
        let items: MaterialListItem[] = res.data || [];
        if (searchKeyword.trim()) {
          const kw = searchKeyword.toLowerCase();
          items = items.filter(m => 
            m.name?.toLowerCase().includes(kw) || 
            m.code?.toLowerCase().includes(kw) ||
            m.description?.toLowerCase().includes(kw)
          );
        }
        setTotalElements(items.length);
        setTotalPages(Math.ceil(items.length / size) || 1);
        setMaterials(items.slice(page * size, (page + 1) * size));
        setLoading(false);
        return;
      } else if (activeStockFilter === "CRITICAL") {
        const res = await materialApi.getCriticalStock();
        let items: MaterialListItem[] = res.data || [];
        if (searchKeyword.trim()) {
          const kw = searchKeyword.toLowerCase();
          items = items.filter(m => 
            m.name?.toLowerCase().includes(kw) || 
            m.code?.toLowerCase().includes(kw) ||
            m.description?.toLowerCase().includes(kw)
          );
        }
        setTotalElements(items.length);
        setTotalPages(Math.ceil(items.length / size) || 1);
        setMaterials(items.slice(page * size, (page + 1) * size));
        setLoading(false);
        return;
      } else if (activeStockFilter === "OUT_OF_STOCK") {
        const res = await materialApi.getOutOfStock();
        let items: MaterialListItem[] = res.data || [];
        if (searchKeyword.trim()) {
          const kw = searchKeyword.toLowerCase();
          items = items.filter(m => 
            m.name?.toLowerCase().includes(kw) || 
            m.code?.toLowerCase().includes(kw) ||
            m.description?.toLowerCase().includes(kw)
          );
        }
        setTotalElements(items.length);
        setTotalPages(Math.ceil(items.length / size) || 1);
        setMaterials(items.slice(page * size, (page + 1) * size));
        setLoading(false);
        return;
      }

      // 2. Default: ALL
      let res;
      if (searchKeyword) {
        // If there's a search keyword, search across all fields
        res = await materialApi.searchAdvancedList({
          code: searchKeyword,
          name: searchKeyword,
          description: searchKeyword,
          shortDescription: searchKeyword,
          searchKeywords: searchKeyword,
          alternativeName: searchKeyword,
          categoryId: filterCategoryId || undefined,
          materialType: filterMaterialType || undefined,
          status: filterStatus || undefined,
        }, page, size);
      } else if (!filterCategoryId && !filterMaterialType && !filterStatus) {
        // No filters applied, use the standard getAll with pagination
        res = await materialApi.getAll(page, size);
      } else {
        // Filters applied, use filterList
        res = await materialApi.filterList({
          categoryId: filterCategoryId || undefined,
          materialType: filterMaterialType || undefined,
          status: filterStatus || undefined,
        }, page, size);
      }
      
      setMaterials(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);

      // Update total catalog items count
      if (!searchKeyword && !filterCategoryId && !filterMaterialType && !filterStatus) {
        setStockCounts(prev => ({ ...prev, total: res.data.totalElements || 0 }));
      }
    } catch (err) {
      console.error("Failed to load materials:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!materialToDelete) return;
    try {
      setIsDeleting(true);
      await materialApi.delete(materialToDelete.id);
      setMaterials(materials.filter(m => m.id !== materialToDelete.id));
      setMaterialToDelete(null);
    } catch (err) {
      console.error("Failed to delete material:", err);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleSearchKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      if (page !== 0) {
        setPage(0);
      } else {
        fetchMaterials();
      }
    }
  };

  const handleApplyFilters = () => {
    setIsFilterOpen(false);
    if (page === 0) {
      setRefreshTrigger(prev => prev + 1);
    } else {
      setPage(0); // This will trigger useEffect to fetchMaterials
    }
  };

  const handleClearFilters = () => {
    setFilterCategoryId("");
    setFilterCategoryName("");
    setFilterMaterialType("");
    setFilterStatus("");
    setIsFilterOpen(false);
    if (page === 0) {
      setRefreshTrigger(prev => prev + 1);
    } else {
      setPage(0);
    }
  };

  const handleSelectAll = () => {
    if (selectedMaterials.length === materials.length && materials.length > 0) {
      setSelectedMaterials([]);
    } else {
      setSelectedMaterials(materials.map(m => m.id));
    }
  };

  const handleSelectOne = (id: string) => {
    if (selectedMaterials.includes(id)) {
      setSelectedMaterials(selectedMaterials.filter(materialId => materialId !== id));
    } else {
      setSelectedMaterials([...selectedMaterials, id]);
    }
  };

  const getInitials = (name: string) => {
    if (!name) return "M";
    return name.substring(0, 2).toUpperCase();
  };

  const getColorForMaterial = (name: string) => {
    if (!name) return "blue";
    const sum = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return colors[sum % colors.length];
  };

  return (
    <>
      {/* 4 Stock Filter Stat Cards (Global database counts & quick filter) */}
      <StockStatCards
        activeFilter={activeStockFilter}
        onSelectFilter={handleSelectStockFilter}
        counts={stockCounts}
      />

      <div className="rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Materials List
          </h3>
        </div>
        <div className="flex items-center gap-3 flex-wrap justify-end">
          {selectedMaterials.length > 0 && (
            <div className="flex items-center gap-2 mr-2 border-r border-gray-200 pr-4 dark:border-white/[0.05]">
              <button className="flex items-center gap-2 rounded-full border border-gray-200 bg-white px-3 py-1.5 hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:hover:bg-white/[0.05]">
                <div className="flex h-5 w-5 items-center justify-center rounded-[4px] bg-[#2C2B35] text-white">
                  <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <span className="text-sm font-semibold text-gray-900 dark:text-white">{selectedMaterials.length}</span>
                <svg className="h-4 w-4 text-gray-600 dark:text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <button className="rounded-full border border-gray-200 bg-white px-4 py-1.5 text-sm font-semibold text-[#183B7E] hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-blue-400 dark:hover:bg-white/[0.05]">
                Renew
              </button>
              <button className="rounded-full border border-gray-200 bg-white px-4 py-1.5 text-sm font-semibold text-[#183B7E] hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-blue-400 dark:hover:bg-white/[0.05]">
                Deactivate
              </button>
              <button className="rounded-full border border-gray-200 bg-white px-4 py-1.5 text-sm font-semibold text-[#183B7E] hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-blue-400 dark:hover:bg-white/[0.05]">
                Delete
              </button>
            </div>
          )}
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
                placeholder="Search materials..."
                value={searchKeyword}
                onChange={(e) => {
                  setSearchKeyword(e.target.value);
                  if (e.target.value === '') {
                    if (page === 0) {
                      setRefreshTrigger(prev => prev + 1);
                    } else {
                      setPage(0);
                    }
                  }
                }}
                onKeyDown={handleSearchKeyDown}
                className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-8 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
              />
              {searchKeyword && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchKeyword("");
                    if (page === 0) {
                      setRefreshTrigger(prev => prev + 1);
                    } else {
                      setPage(0);
                    }
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
              <Button variant="outline" size="sm" onClick={() => setIsFilterOpen(!isFilterOpen)}>
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
              
              <MaterialFilters
                isOpen={isFilterOpen}
                onClose={() => setIsFilterOpen(false)}
                filterCategoryId={filterCategoryId}
                setFilterCategoryId={setFilterCategoryId}
                filterMaterialType={filterMaterialType}
                setFilterMaterialType={setFilterMaterialType}
                filterStatus={filterStatus}
                setFilterStatus={setFilterStatus}
                onApply={handleApplyFilters}
                onClear={handleClearFilters}
              />
            </div>
          <Link to="/materials/create-material">
            <Button size="sm">
              <span className="flex items-center gap-2">
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Material
              </span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Active Filter Pills Bar (matching SupplierListTable view) */}
      {activeFiltersCount > 0 && (
        <div className="flex flex-wrap items-center gap-2 px-5 py-2.5 bg-gray-50/75 border-b border-gray-100 dark:bg-gray-900/40 dark:border-white/[0.05]">
          <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
            Active filters:
          </span>
          {filterCategoryId && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Category: {filterCategoryName || filterCategoryId}
              <button
                type="button"
                onClick={() => {
                  setFilterCategoryId("");
                  setFilterCategoryName("");
                  if (page === 0) setRefreshTrigger(prev => prev + 1);
                  else setPage(0);
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterMaterialType && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Type: {filterMaterialType.replace(/_/g, " ")}
              <button
                type="button"
                onClick={() => {
                  setFilterMaterialType("");
                  if (page === 0) setRefreshTrigger(prev => prev + 1);
                  else setPage(0);
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterStatus && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Status: {filterStatus}
              <button
                type="button"
                onClick={() => {
                  setFilterStatus("");
                  if (page === 0) setRefreshTrigger(prev => prev + 1);
                  else setPage(0);
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
                <p className="text-sm text-gray-500 dark:text-gray-400">Searching materials...</p>
              </div>
            </div>
          ) : materials.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16">
              <div className="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gray-50 dark:bg-gray-800">
                <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-1">No results found</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm text-center">
                No materials match "{searchKeyword}". Try a different keyword.
              </p>
            </div>
          ) : (
            <>
              <div className="mb-4 flex items-center gap-2">
                <svg className="w-4 h-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  Found <span className="font-semibold text-gray-800 dark:text-white/90">{totalElements}</span> result{totalElements !== 1 ? 's' : ''} for "{searchKeyword}"
                </span>
              </div>
              <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
                {materials.map((material) => (
                  <MaterialCard
                    key={material.id}
                    material={material}
                    highlightKeyword={searchKeyword}
                    onCardClick={(m) => {
                      setSelectedMaterialForModal(m as MaterialListItem);
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
                    checked={materials.length > 0 && selectedMaterials.length === materials.length} 
                    onChange={handleSelectAll} 
                  />
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Material
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Stock
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Price
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
              {loading ? (
                <TableRow>
                  <td colSpan={6} className="px-5 py-4 text-center text-gray-500">
                    Loading materials...
                  </td>
                </TableRow>
              ) : materials.length === 0 ? (
                <TableRow>
                  <td colSpan={6} className="px-5 py-16 text-center">
                    <div className="flex flex-col items-center justify-center">
                      {(filterCategoryId || filterMaterialType || filterStatus) ? (
                        <>
                          <div className="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gray-50 dark:bg-gray-800">
                            <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                            </svg>
                          </div>
                          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-1">No matches found</h3>
                          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm">
                            We couldn't find any materials matching your current filters. Try adjusting your category, type, or status.
                          </p>
                        </>
                      ) : (
                        <>
                          <div className="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gray-50 dark:bg-gray-800">
                            <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                            </svg>
                          </div>
                          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-1">Your inventory is empty</h3>
                          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm mb-4">
                            Get started by adding your first material to the database.
                          </p>
                        </>
                      )}
                    </div>
                  </td>
                </TableRow>
              ) : (
                materials.map((material) => {
                  const isOutOfStock = material.currentStock === 0 || material.stockStatus === "OUT_OF_STOCK";
                  const isCritical = material.stockStatus === "CRITICAL";
                  const isReorderNeeded = material.stockStatus === "REORDER_NEEDED";
                  const isInStock = !isOutOfStock && !isCritical && !isReorderNeeded;

                  return (
                    <TableRow key={material.id} className={selectedMaterials.includes(material.id) ? "bg-gray-50 dark:bg-white/[0.02]" : ""}>
                      <TableCell className="px-5 py-4">
                        <Checkbox 
                          checked={selectedMaterials.includes(material.id)} 
                          onChange={() => handleSelectOne(material.id)} 
                        />
                      </TableCell>
                      <TableCell className="px-4 py-4 text-start">
                        <div className="flex items-center gap-3">
                          <div
                            className={`flex h-10 w-10 items-center justify-center rounded-full font-medium ${
                              colorClasses[getColorForMaterial(material.name)]
                            }`}
                          >
                            {getInitials(material.name)}
                          </div>
                          <div>
                            <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                              {material.name}
                            </span>
                            <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                              {material.code}
                            </span>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-4 text-start">
                        <div className="flex items-center gap-2">
                          <span className="block font-semibold text-gray-800 text-theme-sm dark:text-white/90">
                            {material.currentStock} {material.unitOfMeasure}
                          </span>
                          {isOutOfStock ? (
                            <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-400">
                              Out of Stock
                            </span>
                          ) : isCritical ? (
                            <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-400">
                              Critical
                            </span>
                          ) : isReorderNeeded ? (
                            <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-400">
                              Reorder
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400">
                              In Stock
                            </span>
                          )}
                        </div>
                        <div className="flex items-center gap-1.5 text-theme-xs text-gray-500 dark:text-gray-400 mt-0.5">
                          <span>{material.materialType}</span>
                          {material.stockOnOrder != null && material.stockOnOrder > 0 && (
                            <span className="text-brand-600 dark:text-brand-400 font-medium">
                              • {material.stockOnOrder} on order
                            </span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-4 text-start">
                        <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          {material.standardPrice}
                        </span>
                        <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                          {material.standardPriceCurrency}
                        </span>
                      </TableCell>
                      <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                        <Badge size="sm" variant="light" color={material.status === 'ACTIVE' ? 'success' : 'error'}>
                          {material.status || 'ACTIVE'}
                        </Badge>
                      </TableCell>
                      <TableCell className="px-4 py-4">
                        <div className="flex items-center gap-2">
                          {/* 1-Click Reorder Action (Disabled if In Stock) */}
                          <button 
                            disabled={isInStock}
                            onClick={() => {
                              if (isInStock) return;
                              setReorderMaterialId(material.id);
                              setIsReorderModalOpen(true);
                            }}
                            className={`flex items-center justify-center p-2 rounded-lg transition-colors ${
                              isInStock
                                ? "text-gray-300 dark:text-gray-600 opacity-40"
                                : "text-amber-500 hover:text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-500/10 dark:hover:text-amber-400"
                            }`}
                            title={
                              isInStock
                                ? "Stock is optimal (no reorder needed)"
                                : "1-Click Reorder (Recommendation & Purchase Requisition)"
                            }
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                            </svg>
                          </button>

                        <Link to={`/materials/view/${material.id}`}>
                          <button 
                            className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                            title="View Material"
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                          </button>
                        </Link>
                        <Link to={`/materials/edit/${material.id}`}>
                          <button 
                            className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                            title="Update Material"
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                        </Link>
                        <button 
                          onClick={() => setMaterialToDelete(material)}
                          className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-error-500 hover:bg-error-50 dark:hover:bg-error-500/10 dark:hover:text-error-500 transition-colors"
                          title="Delete Material"
                        >
                          <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    </TableCell>
                  </TableRow>
                  );
                })
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
        isOpen={!!materialToDelete}
        onClose={() => setMaterialToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Danger Alert!"
        message={`Are you sure you want to delete the material "${materialToDelete?.name}"?`}
        isDeleting={isDeleting}
      />

      {/* Quick Info Modal for tapping card */}
      <MaterialInfoModal
        isOpen={isInfoModalOpen}
        onClose={() => setIsInfoModalOpen(false)}
        material={selectedMaterialForModal}
      />

      {/* 1-Click Reorder Recommendation & PR Trigger Modal */}
      <ReorderRecommendationModal
        materialId={reorderMaterialId}
        isOpen={isReorderModalOpen}
        onClose={() => {
          setIsReorderModalOpen(false);
          setReorderMaterialId(null);
        }}
        onReorderSuccess={() => {
          fetchMaterials();
          fetchStockCounts();
        }}
      />
    </div>
    </>
  );
}
