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
import { materialApi } from "../services/materialApi";
import type { MaterialListItem } from "../types/MaterialListItem";
import { Dropdown } from "../../../shared/components/ui/dropdown/Dropdown";
import CustomSelect from "./CustomSelect";
import CategoryTreeSelect from "../../categories/components/CategoryTreeSelect";
import { MaterialStatus, MaterialType } from "../enums";
import Pagination from "../../../shared/components/ui/Pagination";
import MaterialCard from "./MaterialCard";

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
  const [filterMaterialType, setFilterMaterialType] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  
  // Pagination state
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchMaterials();
  }, [page, size]);

  const fetchMaterials = async () => {
    try {
      setLoading(true);
      
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
      fetchMaterials();
    } else {
      setPage(0); // This will trigger useEffect to fetchMaterials
    }
  };

  const handleClearFilters = () => {
    setFilterCategoryId("");
    setFilterMaterialType("");
    setFilterStatus("");
    setIsFilterOpen(false);
    if (page === 0) {
      // Need a timeout to ensure state is updated before fetching
      setTimeout(fetchMaterials, 0);
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
    <div className="rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Materials List
          </h3>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative hidden sm:block">
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
              width="20"
              height="20"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              type="text"
              placeholder="Search materials..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={handleSearchKeyDown}
              className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-4 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-80"
            />
          </div>
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
            
            <Dropdown isOpen={isFilterOpen} onClose={() => setIsFilterOpen(false)} className="w-[600px] p-5 top-full right-0 mt-2">
              <h4 className="mb-4 text-sm font-semibold text-gray-800 dark:text-white/90">Filter Materials</h4>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">Category</label>
                  <CategoryTreeSelect
                    value={filterCategoryId}
                    onChange={setFilterCategoryId}
                    placeholder="Select Category"
                    maxHeightClass="max-h-[200px]"
                  />
                </div>
                <div>
                  <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">Material Type</label>
                  <CustomSelect
                    value={filterMaterialType}
                    onChange={setFilterMaterialType}
                    placeholder="Select Type"
                    options={Object.entries(MaterialType).map(([key, val]) => ({ value: val, label: key.replace(/_/g, " ") }))}
                  />
                </div>
                <div>
                  <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">Status</label>
                  <CustomSelect
                    value={filterStatus}
                    onChange={setFilterStatus}
                    placeholder="Select Status"
                    options={Object.entries(MaterialStatus).map(([key, val]) => ({ value: val, label: key }))}
                  />
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-4 border-t border-gray-100 dark:border-white/[0.05]">
                <Button variant="outline" size="sm" onClick={handleClearFilters}>
                  Clear
                </Button>
                <Button variant="primary" size="sm" onClick={handleApplyFilters}>
                  Apply Filters
                </Button>
              </div>
            </Dropdown>
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
                  <div
                    key={material.id}
                    className="rounded-xl transition-all duration-200 hover:ring-2 hover:ring-yellow-400 hover:shadow-[0_0_16px_rgba(250,204,21,0.15)] dark:hover:ring-yellow-500/60 dark:hover:shadow-[0_0_16px_rgba(234,179,8,0.1)]"
                  >
                    <MaterialCard
                      material={material}
                      highlightKeyword={searchKeyword}
                    />
                  </div>
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
                materials.map((material) => (
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
                      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {material.currentStock} {material.unitOfMeasure}
                      </span>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        {material.materialType}
                      </span>
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
        isOpen={!!materialToDelete}
        onClose={() => setMaterialToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Danger Alert!"
        message={`Are you sure you want to delete the material "${materialToDelete?.name}"?`}
        isDeleting={isDeleting}
      />
    </div>
  );
}
