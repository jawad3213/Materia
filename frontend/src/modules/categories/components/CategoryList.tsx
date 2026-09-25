import React, { useEffect, useMemo, useState } from "react";
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
import { categoryApi } from "../services/categoryApi";
import type { CategoryListItem } from "../types/CategoryListItem";
import Pagination from "../../../shared/components/ui/Pagination";
import CategoryFilterPanel, {
  EMPTY_FILTERS,
  type CategoryFilters,
} from "./CategoryFilterPanel";

export default function CategoryList() {
  const [categories, setCategories] = useState<CategoryListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [categoryToDelete, setCategoryToDelete] = useState<CategoryListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<CategoryFilters>(EMPTY_FILTERS);

  // Pagination state
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  useEffect(() => {
    setPage(0);
  }, [filters]);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const res = await categoryApi.getAll(page, size);
      setCategories(res.data);
    } catch (err) {
      console.error("Failed to load categories:", err);
    } finally {
      setLoading(false);
    }
  };

  // Derive unique parent codes and levels from the loaded data
  const parentCodes = useMemo(() => {
    const codes = new Set<string>();
    categories.forEach((c) => {
      if (c.parentCode) codes.add(c.parentCode);
    });
    return Array.from(codes).sort();
  }, [categories]);

  const levels = useMemo(() => {
    const lvls = new Set<number>();
    categories.forEach((c) => lvls.add(c.level));
    return Array.from(lvls).sort((a, b) => a - b);
  }, [categories]);

  // Apply client-side filters
  const filteredCategories = useMemo(() => {
    return categories.filter((cat) => {
      if (filters.categoryType && cat.categoryType !== filters.categoryType) return false;
      if (filters.status && cat.status !== filters.status) return false;
      if (filters.parentCode && cat.parentCode !== filters.parentCode) return false;
      if (filters.level && cat.level !== Number(filters.level)) return false;
      if (filters.isRoot === "true" && cat.parentCode) return false;
      if (filters.isRoot === "false" && !cat.parentCode) return false;
      return true;
    });
  }, [categories, filters]);

  const totalElements = filteredCategories.length;
  const totalPages = Math.ceil(totalElements / size);

  const paginatedCategories = useMemo(() => {
    return filteredCategories.slice(page * size, (page + 1) * size);
  }, [filteredCategories, page, size]);

  const activeFilterCount = Object.values(filters).filter(Boolean).length;

  const handleDeleteConfirm = async () => {
    if (!categoryToDelete) return;
    try {
      setIsDeleting(true);
      await categoryApi.delete(categoryToDelete.id);
      setCategories(categories.filter(c => c.id !== categoryToDelete.id));
      setCategoryToDelete(null);
    } catch (err) {
      console.error("Failed to delete category:", err);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleSelectAll = () => {
    if (selectedCategories.length === paginatedCategories.length && paginatedCategories.length > 0) {
      setSelectedCategories([]);
    } else {
      setSelectedCategories(paginatedCategories.map(c => c.id));
    }
  };

  const handleSelectOne = (id: string) => {
    if (selectedCategories.includes(id)) {
      setSelectedCategories(selectedCategories.filter(categoryId => categoryId !== id));
    } else {
      setSelectedCategories([...selectedCategories, id]);
    }
  };

  return (
    <>
      <CategoryFilterPanel
        filters={filters}
        onChange={setFilters}
        parentCodes={parentCodes}
        levels={levels}
        isOpen={showFilters}
        onToggle={() => setShowFilters(!showFilters)}
      />

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        {/* Header */}
        <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Categories List
            </h3>
            {activeFilterCount > 0 && (
              <span className="text-xs text-gray-500 dark:text-gray-400">
                ({filteredCategories.length} of {categories.length})
              </span>
            )}
          </div>
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowFilters(!showFilters)}
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
                {activeFilterCount > 0 && (
                  <span className="inline-flex items-center justify-center h-4 min-w-[16px] px-1 rounded-full bg-brand-500 text-[10px] font-bold text-white">
                    {activeFilterCount}
                  </span>
                )}
              </span>
            </Button>
            <Link to="/categories/create-category">
              <Button size="sm">
                <span className="flex items-center gap-2">
                  <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  New Category
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
                    checked={paginatedCategories.length > 0 && selectedCategories.length === paginatedCategories.length} 
                    onChange={handleSelectAll} 
                  />
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Category
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Identifiers
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Classification
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Contents
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
                  <td colSpan={7} className="py-20 text-center">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
                      <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                        Fetching categories from database...
                      </span>
                    </div>
                  </td>
                </TableRow>
              ) : filteredCategories.length === 0 ? (
                <TableRow>
                  <td colSpan={7} className="px-5 py-8 text-center text-gray-500">
                    <div className="flex flex-col items-center gap-2">
                      <svg className="w-8 h-8 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                      </svg>
                      <span>
                        {activeFilterCount > 0
                          ? "No categories match the current filters."
                          : "No categories found. Create one to get started."}
                      </span>
                      {activeFilterCount > 0 && (
                        <button
                          onClick={() => setFilters(EMPTY_FILTERS)}
                          className="text-sm text-brand-500 hover:underline"
                        >
                          Clear all filters
                        </button>
                      )}
                    </div>
                  </td>
                </TableRow>
              ) : (
                paginatedCategories.map((category) => (
                  <TableRow key={category.id} className={selectedCategories.includes(category.id) ? "bg-gray-50 dark:bg-white/[0.02]" : ""}>
                    <TableCell className="px-5 py-4">
                      <Checkbox 
                        checked={selectedCategories.includes(category.id)} 
                        onChange={() => handleSelectOne(category.id)} 
                      />
                    </TableCell>
                    <TableCell className="px-4 py-4 text-start">
                      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {category.name}
                      </span>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        Path: {category.path || "Root"}
                      </span>
                    </TableCell>
                    <TableCell className="px-4 py-4 text-start">
                      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {category.code}
                      </span>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        Parent: {category.parentCode || "None"}
                      </span>
                    </TableCell>
                    <TableCell className="px-4 py-4 text-start">
                      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90 capitalize">
                        {category.categoryType?.toLowerCase().replace('_', ' ')}
                      </span>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        Level: {category.level}
                      </span>
                    </TableCell>
                    <TableCell className="px-4 py-4 text-start">
                      <div className="flex items-center gap-1.5 mb-1">
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200">
                          Total: {category.totalItems ?? ((category.materialCount ?? 0) + (category.subCategoryCount ?? 0))}
                        </span>
                      </div>
                      <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                        Subs: {category.subCategoryCount ?? 0} • Materials: {category.materialCount ?? 0}
                      </span>
                    </TableCell>
                    <TableCell className="px-4 py-4 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      <Badge size="sm" variant="light" color={category.status === 'ACTIVE' ? 'success' : category.status === 'INACTIVE' ? 'error' : 'warning'}>
                        {category.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <Link to={`/categories/view/${category.id}`}>
                          <button 
                            className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                            title="View Category"
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                          </button>
                        </Link>
                        <Link to={`/categories/edit/${category.id}`}>
                          <button 
                            className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
                            title="Update Category"
                          >
                            <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                        </Link>
                        <button 
                          onClick={() => setCategoryToDelete(category)}
                          className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-error-500 hover:bg-error-50 dark:hover:bg-error-500/10 dark:hover:text-error-500 transition-colors"
                          title="Delete Category"
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
          isOpen={!!categoryToDelete}
          onClose={() => setCategoryToDelete(null)}
          onConfirm={handleDeleteConfirm}
          title="Danger Alert!"
          message={`Are you sure you want to delete the category "${categoryToDelete?.name}"?`}
          isDeleting={isDeleting}
        />
      </div>
    </>
  );
}
