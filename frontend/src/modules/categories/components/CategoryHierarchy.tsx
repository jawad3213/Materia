import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { categoryApi } from "../services/categoryApi";
import type { CategoryListItem } from "../types/CategoryListItem";
import type { Category } from "../types/Category";
import Badge from "../../../shared/components/ui/badge/Badge";
import Button from "../../../shared/components/ui/button/Button";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";

export interface HierarchyNode extends CategoryListItem {
  children: HierarchyNode[];
}

export default function CategoryHierarchy() {
  const [categories, setCategories] = useState<CategoryListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [expandedCodes, setExpandedCodes] = useState<Set<string>>(new Set());
  const [selectedCategoryCode, setSelectedCategoryCode] = useState<string | null>(null);
  const [selectedDetail, setSelectedDetail] = useState<Category | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [categoryToDelete, setCategoryToDelete] = useState<CategoryListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const res = await categoryApi.getAll(0, 1000);
      setCategories(res.data);

      // Auto expand root categories and select the first root
      const roots = res.data.filter((c) => !c.parentCode || c.level === 0);
      const rootCodes = new Set(roots.map((r) => r.code));
      setExpandedCodes(rootCodes);

      if (roots.length > 0 && !selectedCategoryCode) {
        setSelectedCategoryCode(roots[0].code);
      }
    } catch (err) {
      console.error("Failed to load categories:", err);
    } finally {
      setLoading(false);
    }
  };

  // Build tree from flat categories
  const { tree, codeToNodeMap } = useMemo(() => {
    const codeMap = new Map<string, HierarchyNode>();
    categories.forEach((item) => {
      codeMap.set(item.code, { ...item, children: [] });
    });

    const roots: HierarchyNode[] = [];
    categories.forEach((item) => {
      const node = codeMap.get(item.code)!;
      if (item.parentCode && codeMap.has(item.parentCode)) {
        codeMap.get(item.parentCode)!.children.push(node);
      } else {
        roots.push(node);
      }
    });

    // Sort roots and children by name
    const sortNodes = (nodes: HierarchyNode[]) => {
      nodes.sort((a, b) => a.name.localeCompare(b.name));
      nodes.forEach((n) => sortNodes(n.children));
    };
    sortNodes(roots);

    return { tree: roots, codeToNodeMap: codeMap };
  }, [categories]);

  // Currently selected node in tree
  const selectedNode = useMemo(() => {
    if (!selectedCategoryCode) return null;
    return codeToNodeMap.get(selectedCategoryCode) || null;
  }, [selectedCategoryCode, codeToNodeMap]);

  // Fetch full details whenever selected node changes
  useEffect(() => {
    if (!selectedNode) {
      setSelectedDetail(null);
      return;
    }
    let isMounted = true;
    setLoadingDetail(true);
    categoryApi
      .getById(selectedNode.id)
      .then((res) => {
        if (isMounted) setSelectedDetail(res.data);
      })
      .catch((err) => {
        console.error("Failed to fetch category details:", err);
        if (isMounted) setSelectedDetail(null);
      })
      .finally(() => {
        if (isMounted) setLoadingDetail(false);
      });

    return () => {
      isMounted = false;
    };
  }, [selectedNode]);

  // Build breadcrumb trail from root to selected node
  const breadcrumbTrail = useMemo(() => {
    if (!selectedNode) return [];
    const trail: HierarchyNode[] = [];
    let curr: HierarchyNode | undefined = selectedNode;
    while (curr) {
      trail.unshift(curr);
      curr = curr.parentCode ? codeToNodeMap.get(curr.parentCode) : undefined;
    }
    return trail;
  }, [selectedNode, codeToNodeMap]);

  // Toggle node expansion
  const toggleExpand = (code: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setExpandedCodes((prev) => {
      const next = new Set(prev);
      if (next.has(code)) {
        next.delete(code);
      } else {
        next.add(code);
      }
      return next;
    });
  };

  const expandAll = () => {
    const allCodes = new Set(categories.map((c) => c.code));
    setExpandedCodes(allCodes);
  };

  const collapseAll = () => {
    setExpandedCodes(new Set());
  };

  // Node search filtering
  const searchMatches = useMemo(() => {
    if (!searchQuery.trim()) return null;
    const q = searchQuery.toLowerCase().trim();
    const matchingCodes = new Set<string>();

    categories.forEach((cat) => {
      if (
        cat.name.toLowerCase().includes(q) ||
        cat.code.toLowerCase().includes(q) ||
        (cat.categoryType && cat.categoryType.toLowerCase().includes(q))
      ) {
        // Add matching code and all its ancestors to keep it visible
        matchingCodes.add(cat.code);
        let parentCode = cat.parentCode;
        while (parentCode && codeToNodeMap.has(parentCode)) {
          matchingCodes.add(parentCode);
          parentCode = codeToNodeMap.get(parentCode)?.parentCode;
        }
      }
    });

    return matchingCodes;
  }, [categories, searchQuery, codeToNodeMap]);

  // When search changes, expand all matching branches
  useEffect(() => {
    if (searchMatches && searchMatches.size > 0) {
      setExpandedCodes((prev) => new Set([...prev, ...searchMatches]));
    }
  }, [searchMatches]);

  const handleDeleteConfirm = async () => {
    if (!categoryToDelete) return;
    try {
      setIsDeleting(true);
      await categoryApi.delete(categoryToDelete.id);
      setCategories((prev) => prev.filter((c) => c.id !== categoryToDelete.id));

      if (selectedCategoryCode === categoryToDelete.code) {
        const remaining = categories.filter((c) => c.id !== categoryToDelete.id);
        setSelectedCategoryCode(remaining.length > 0 ? remaining[0].code : null);
      }
      setCategoryToDelete(null);
    } catch (err) {
      console.error("Failed to delete category:", err);
    } finally {
      setIsDeleting(false);
    }
  };

  // Render tree item recursively
  const renderTreeNode = (node: HierarchyNode, depth = 0) => {
    const hasChildren = node.children && node.children.length > 0;
    const isExpanded = expandedCodes.has(node.code);
    const isSelected = selectedCategoryCode === node.code;
    const isMatching = searchMatches ? searchMatches.has(node.code) : true;

    if (searchMatches && !isMatching) return null;

    return (
      <div key={node.code} className="select-none">
        <div
          onClick={() => setSelectedCategoryCode(node.code)}
          className={`group flex items-center justify-between px-2.5 py-2 rounded-lg cursor-pointer transition-all duration-150 text-sm ${
            isSelected
              ? "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300 font-medium shadow-xs"
              : "text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/[0.04]"
          }`}
          style={{ paddingLeft: `${Math.max(10, depth * 18 + 10)}px` }}
        >
          <div className="flex items-center gap-2 min-w-0 flex-1">
            {/* Expand / Collapse Chevron */}
            {hasChildren ? (
              <button
                type="button"
                onClick={(e) => toggleExpand(node.code, e)}
                className="size-5 flex items-center justify-center rounded text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-transform"
              >
                <svg
                  className={`size-3.5 transition-transform duration-200 ${
                    isExpanded ? "rotate-90" : "rotate-0"
                  }`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            ) : (
              <span className="size-5 flex items-center justify-center text-gray-300 dark:text-gray-600">
                •
              </span>
            )}

            {/* Folder / Tag icon */}
            <span
              className={`shrink-0 ${
                isSelected
                  ? "text-brand-500 dark:text-brand-400"
                  : hasChildren
                  ? "text-amber-500/80 dark:text-amber-400"
                  : "text-gray-400 dark:text-gray-500"
              }`}
            >
              {hasChildren ? (
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.75}
                    d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
                  />
                </svg>
              ) : (
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.75}
                    d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
                  />
                </svg>
              )}
            </span>

            {/* Category Name & Code */}
            <span className="truncate text-xs font-medium">{node.name}</span>
            <span className="text-[10px] font-mono text-gray-400 dark:text-gray-500 shrink-0">
              {node.code}
            </span>
          </div>

          {/* Counts pill */}
          <div className="flex items-center gap-1.5 shrink-0 ml-2">
            {hasChildren && (
              <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-gray-200/70 text-gray-600 dark:bg-gray-800 dark:text-gray-400 font-semibold">
                {node.children.length}
              </span>
            )}
            {node.status === "INACTIVE" && (
              <span className="size-2 rounded-full bg-error-500" title="Inactive" />
            )}
          </div>
        </div>

        {/* Children indented */}
        {hasChildren && isExpanded && (
          <div className="relative border-l border-gray-100 dark:border-white/[0.05] ml-4 mt-0.5 mb-1 pl-1">
            {node.children.map((child) => renderTreeNode(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <>
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        {/* Explorer Top Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05]">
          <div className="flex items-center gap-3">
            <div className="size-9 rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400 flex items-center justify-center">
              <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
                />
              </svg>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                Taxonomy & Hierarchy Explorer
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Browse, navigate, and manage multi-level category structures
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Link to="/categories/create-category">
              <Button size="sm">
                <span className="flex items-center gap-1.5">
                  <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  New Root Category
                </span>
              </Button>
            </Link>
          </div>
        </div>

        {/* Dual-Pane Layout: Tree Navigator (Left) + Branch Detail (Right) */}
        <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[580px]">
          {/* ================= LEFT PANE: TREE NAVIGATOR ================= */}
          <div className="lg:col-span-5 xl:col-span-4 border-b lg:border-b-0 lg:border-r border-gray-100 dark:border-white/[0.05] p-4 flex flex-col">
            {/* Tree Search & Controls */}
            <div className="mb-3 space-y-2">
              <div className="relative">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search taxonomy..."
                  className="w-full h-9 rounded-lg border border-gray-200 bg-transparent py-1.5 pl-8 pr-3 text-xs text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300"
                />
                <div className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400">
                  <svg className="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                    />
                  </svg>
                </div>
                {searchQuery && (
                  <button
                    onClick={() => setSearchQuery("")}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-xs"
                  >
                    ×
                  </button>
                )}
              </div>

              <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400 px-1">
                <span>
                  Total: <strong className="text-gray-700 dark:text-gray-200">{categories.length}</strong> categories
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={expandAll}
                    className="hover:text-brand-500 transition-colors text-[11px]"
                  >
                    Expand All
                  </button>
                  <span>•</span>
                  <button
                    onClick={collapseAll}
                    className="hover:text-brand-500 transition-colors text-[11px]"
                  >
                    Collapse
                  </button>
                </div>
              </div>
            </div>

            {/* Tree Scroll Container */}
            <div className="flex-1 overflow-y-auto max-h-[500px] lg:max-h-[600px] pr-1 space-y-0.5">
              {loading ? (
                <div className="py-12 text-center text-xs text-gray-400">
                  <div className="size-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                  Loading hierarchy...
                </div>
              ) : tree.length === 0 ? (
                <div className="py-12 text-center text-xs text-gray-400">
                  No categories found. Create a category to start your taxonomy.
                </div>
              ) : (
                tree.map((rootNode) => renderTreeNode(rootNode))
              )}
            </div>
          </div>

          {/* ================= RIGHT PANE: SELECTED NODE DETAILS ================= */}
          <div className="lg:col-span-7 xl:col-span-8 p-6 flex flex-col bg-gray-50/40 dark:bg-white/[0.01]">
            {selectedNode ? (
              <div className="space-y-6">
                {/* Interactive Breadcrumb Trail */}
                <div className="flex items-center gap-1.5 flex-wrap text-xs text-gray-500 dark:text-gray-400 bg-white dark:bg-white/[0.03] p-2.5 rounded-lg border border-gray-100 dark:border-white/[0.05]">
                  <button
                    onClick={() => {
                      if (tree.length > 0) setSelectedCategoryCode(tree[0].code);
                    }}
                    className="hover:text-brand-500 transition-colors flex items-center gap-1 font-medium"
                  >
                    <svg className="size-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                      />
                    </svg>
                    Root
                  </button>
                  {breadcrumbTrail.map((crumb, idx) => (
                    <React.Fragment key={crumb.code}>
                      <span className="text-gray-300 dark:text-gray-600">/</span>
                      <button
                        onClick={() => setSelectedCategoryCode(crumb.code)}
                        className={`hover:text-brand-500 transition-colors truncate max-w-[140px] ${
                          idx === breadcrumbTrail.length - 1
                            ? "text-brand-600 dark:text-brand-400 font-semibold"
                            : "font-medium"
                        }`}
                      >
                        {crumb.name}
                      </button>
                    </React.Fragment>
                  ))}
                </div>

                {/* Selected Node Overview Header Card */}
                <div className="rounded-xl border border-gray-200 bg-white p-5 dark:border-white/[0.05] dark:bg-white/[0.03] shadow-xs">
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-4">
                    <div>
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <span className="font-mono text-xs px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 font-semibold">
                          {selectedNode.code}
                        </span>
                        <Badge
                          size="sm"
                          variant="light"
                          color={
                            selectedNode.status === "ACTIVE"
                              ? "success"
                              : selectedNode.status === "INACTIVE"
                              ? "error"
                              : "warning"
                          }
                        >
                          {selectedNode.status}
                        </Badge>
                        <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300 uppercase">
                          {selectedNode.categoryType?.replace(/_/g, " ")}
                        </span>
                      </div>
                      <h2 className="text-xl font-bold text-gray-900 dark:text-white">
                        {selectedNode.name}
                      </h2>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {loadingDetail ? (
                          "Loading details..."
                        ) : selectedDetail?.description ? (
                          selectedDetail.description
                        ) : (
                          "No extended description provided for this category."
                        )}
                      </p>
                    </div>

                    {/* Action Buttons Toolbar */}
                    <div className="flex items-center gap-2 flex-wrap">
                      <Link
                        to={`/categories/create-category?parentId=${selectedNode.id}&parentCode=${selectedNode.code}`}
                      >
                        <Button size="sm" variant="primary">
                          <span className="flex items-center gap-1.5 text-xs">
                            <svg className="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                            </svg>
                            Add Subcategory
                          </span>
                        </Button>
                      </Link>

                      <Link to={`/categories/edit/${selectedNode.id}`}>
                        <Button size="sm" variant="outline">
                          <span className="flex items-center gap-1.5 text-xs">
                            <svg className="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                              />
                            </svg>
                            Edit
                          </span>
                        </Button>
                      </Link>

                      <Link to={`/categories/view/${selectedNode.id}`}>
                        <Button size="sm" variant="outline">
                          <span className="flex items-center gap-1.5 text-xs">
                            <svg
                              className="size-3.5"
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                              />
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                              />
                            </svg>
                            Details
                          </span>
                        </Button>
                      </Link>

                      <button
                        type="button"
                        onClick={() => setCategoryToDelete(selectedNode)}
                        className="flex items-center justify-center p-2 rounded-lg text-gray-400 hover:text-error-500 hover:bg-error-50 dark:hover:bg-error-500/10 dark:hover:text-error-500 transition-colors"
                        title="Delete Category"
                      >
                        <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                          />
                        </svg>
                      </button>
                    </div>
                  </div>

                  {/* KPI Stat Chips */}
                  <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 pt-4 border-t border-gray-100 dark:border-white/[0.05]">
                    <div className="bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
                      <span className="block text-[11px] text-gray-400 dark:text-gray-500">Total Items</span>
                      <span className="text-base font-bold text-brand-600 dark:text-brand-400">
                        {selectedNode.totalItems ?? ((selectedNode.materialCount ?? 0) + (selectedNode.children?.length ?? 0))}
                      </span>
                    </div>
                    <div className="bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
                      <span className="block text-[11px] text-gray-400 dark:text-gray-500">Subcategories</span>
                      <span className="text-base font-bold text-gray-800 dark:text-white/90">
                        {selectedNode.children.length}
                      </span>
                    </div>
                    <div className="bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
                      <span className="block text-[11px] text-gray-400 dark:text-gray-500">Linked Materials</span>
                      <span className="text-base font-bold text-gray-800 dark:text-white/90">
                        {selectedNode.materialCount ?? 0}
                      </span>
                    </div>
                    <div className="bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
                      <span className="block text-[11px] text-gray-400 dark:text-gray-500">Hierarchy Depth</span>
                      <span className="text-base font-bold text-gray-800 dark:text-white/90">
                        Level {selectedNode.level ?? 0}
                      </span>
                    </div>
                    <div className="bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
                      <span className="block text-[11px] text-gray-400 dark:text-gray-500">Hierarchy Path</span>
                      <span className="text-xs font-mono font-medium text-gray-700 dark:text-gray-300 truncate block">
                        {selectedNode.path || "Root"}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Direct Children Subcategories Grid */}
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
                      Subcategories under "{selectedNode.name}" ({selectedNode.children.length})
                    </h4>
                    <span className="text-xs text-gray-500 dark:text-gray-400">
                      Click any card to drill down into its branch
                    </span>
                  </div>

                  {selectedNode.children.length === 0 ? (
                    <div className="text-center py-10 px-4 rounded-xl border border-dashed border-gray-200 dark:border-white/[0.08] bg-white dark:bg-white/[0.02]">
                      <div className="size-10 rounded-full bg-brand-50 text-brand-500 dark:bg-brand-500/10 flex items-center justify-center mx-auto mb-2">
                        <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1.75}
                            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                          />
                        </svg>
                      </div>
                      <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                        This category has no child subcategories
                      </p>
                      <p className="text-xs text-gray-400 mt-1 mb-3">
                        Expand this branch by creating the first subcategory
                      </p>
                      <Link
                        to={`/categories/create-category?parentId=${selectedNode.id}&parentCode=${selectedNode.code}`}
                      >
                        <Button size="sm" variant="outline">
                          + Add First Subcategory
                        </Button>
                      </Link>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {selectedNode.children.map((child) => (
                        <div
                          key={child.code}
                          onClick={() => {
                            setSelectedCategoryCode(child.code);
                            // Also ensure the parent is expanded in the tree
                            setExpandedCodes((prev) => new Set([...prev, selectedNode.code]));
                          }}
                          className="group relative rounded-xl border border-gray-200 bg-white p-4 dark:border-white/[0.05] dark:bg-white/[0.03] hover:border-brand-300 dark:hover:border-brand-500/30 hover:shadow-md transition-all cursor-pointer"
                        >
                          <div className="flex items-start justify-between gap-2 mb-2">
                            <div className="flex items-center gap-2 min-w-0">
                              <span className="size-7 rounded-lg bg-gray-100 dark:bg-gray-800 flex items-center justify-center text-gray-500 shrink-0 group-hover:bg-brand-50 group-hover:text-brand-600 dark:group-hover:bg-brand-500/20 transition-colors">
                                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={1.75}
                                    d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
                                  />
                                </svg>
                              </span>
                              <div className="min-w-0">
                                <h5 className="text-sm font-semibold text-gray-900 dark:text-white truncate group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
                                  {child.name}
                                </h5>
                                <span className="text-[10px] font-mono text-gray-400 dark:text-gray-500">
                                  {child.code}
                                </span>
                              </div>
                            </div>
                            <Badge
                              size="sm"
                              variant="light"
                              color={child.status === "ACTIVE" ? "success" : "error"}
                            >
                              {child.status}
                            </Badge>
                          </div>

                          <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400 pt-3 border-t border-gray-100 dark:border-white/[0.04] mt-2">
                            <span className="flex items-center gap-1.5 text-[11px]">
                              <span>Total: <strong>{child.totalItems ?? ((child.materialCount ?? 0) + (child.subCategoryCount ?? 0))}</strong></span>
                              <span>•</span>
                              <span>Subs: <strong>{child.subCategoryCount ?? child.children?.length ?? 0}</strong></span>
                              <span>•</span>
                              <span>Items: <strong>{child.materialCount ?? 0}</strong></span>
                            </span>

                            <span className="flex items-center gap-1 text-brand-600 dark:text-brand-400 font-medium text-xs group-hover:translate-x-1 transition-transform">
                              Drill Down
                              <svg className="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                              </svg>
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex-1 flex flex-col items-center justify-center py-16 text-center text-gray-400">
                <svg className="size-12 text-gray-300 dark:text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                </svg>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Select a category from the tree</p>
                <p className="text-xs text-gray-400 mt-1">Pick any category from the left tree navigator to explore its hierarchy.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <DeleteConfirmModal
        isOpen={!!categoryToDelete}
        onClose={() => setCategoryToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Danger Alert!"
        message={`Are you sure you want to delete category "${categoryToDelete?.name}"? All subcategories under it may be affected.`}
        isDeleting={isDeleting}
      />
    </>
  );
}
