import React, { useState, useRef, useEffect } from "react";
import { categoryApi } from "../services/categoryApi";
import type { CategoryListItem } from "../types/CategoryListItem";

export interface TreeNode extends CategoryListItem {
  children?: TreeNode[];
  isExpanded?: boolean;
  isLoading?: boolean;
}

interface CategoryTreeSelectProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
  maxHeightClass?: string;
  error?: boolean;
}

export default function CategoryTreeSelect({
  value,
  onChange,
  placeholder = "Select a category",
  className = "",
  maxHeightClass = "max-h-[320px]",
  error = false,
}: CategoryTreeSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [nodes, setNodes] = useState<TreeNode[]>([]);
  const [isLoadingRoots, setIsLoadingRoots] = useState(false);
  
  const selectRef = useRef<HTMLDivElement>(null);
  const [selectedLabel, setSelectedLabel] = useState<string>("");

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    // Fetch roots once
    const fetchRoots = async () => {
      try {
        setIsLoadingRoots(true);
        const res = await categoryApi.getRoots();
        setNodes(res.data);
      } catch (err) {
        console.error("Failed to load root categories", err);
      } finally {
        setIsLoadingRoots(false);
      }
    };
    fetchRoots();
  }, []);

  // Update selected label if value changes
  useEffect(() => {
    if (!value) {
      setSelectedLabel("");
      return;
    }
    // Deep search in nodes
    const findLabel = (nodesList: TreeNode[]): string | null => {
      for (const node of nodesList) {
        if (node.id === value) return node.name;
        if (node.children) {
          const found = findLabel(node.children);
          if (found) return found;
        }
      }
      return null;
    };
    const label = findLabel(nodes);
    if (label) {
      setSelectedLabel(label);
    } else {
      // If we can't find it in our current loaded tree, we fetch it
      categoryApi.getById(value).then(res => setSelectedLabel(res.data.name)).catch(() => {});
    }
  }, [value, nodes]);

  const handleToggleExpand = async (e: React.MouseEvent, node: TreeNode) => {
    e.stopPropagation(); // prevent selecting the item
    
    // Create a deep copy function to update the tree
    const updateNodeInTree = (tree: TreeNode[], targetId: string, updater: (n: TreeNode) => TreeNode): TreeNode[] => {
      return tree.map(n => {
        if (n.id === targetId) return updater(n);
        if (n.children) {
          return { ...n, children: updateNodeInTree(n.children, targetId, updater) };
        }
        return n;
      });
    };

    if (node.isExpanded) {
      setNodes(prev => updateNodeInTree(prev, node.id, n => ({ ...n, isExpanded: false })));
      return;
    }

    if (node.children) {
      setNodes(prev => updateNodeInTree(prev, node.id, n => ({ ...n, isExpanded: true })));
      return;
    }

    // Need to fetch children
    setNodes(prev => updateNodeInTree(prev, node.id, n => ({ ...n, isLoading: true })));
    try {
      const res = await categoryApi.getSubCategories(node.id);
      setNodes(prev => updateNodeInTree(prev, node.id, n => ({ 
        ...n, 
        children: res.data,
        isExpanded: true,
        isLoading: false
      })));
    } catch (err) {
      console.error("Failed to fetch subcategories", err);
      setNodes(prev => updateNodeInTree(prev, node.id, n => ({ ...n, isLoading: false })));
    }
  };

  const handleSelect = (node: TreeNode) => {
    onChange(node.id);
    setSelectedLabel(node.name);
    setIsOpen(false);
  };

  const renderNodes = (nodeList: TreeNode[], level = 0) => {
    return nodeList.map((node) => (
      <div key={node.id}>
        <div
          className={`flex items-center px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-800 ${
            value === node.id
              ? "bg-brand-50 text-brand-500 dark:bg-brand-500/10 font-medium"
              : "text-gray-700 dark:text-gray-300"
          }`}
          style={{ paddingLeft: `${(level * 1.5) + 1}rem` }}
        >
          {/* Caret area (fixed width) */}
          <div className="w-5 flex items-center justify-center mr-1 cursor-pointer" onClick={(e) => {
              if (node.subCategoryCount > 0) {
                 handleToggleExpand(e, node);
              }
          }}>
            {node.isLoading ? (
              <svg className="w-3 h-3 animate-spin text-brand-500" fill="none" viewBox="0 0 24 24">
                 <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                 <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            ) : node.subCategoryCount > 0 ? (
              <svg 
                className={`w-3.5 h-3.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-transform ${node.isExpanded ? 'rotate-90' : ''}`} 
                fill="none" viewBox="0 0 24 24" stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            ) : (
              <span className="w-3.5 h-3.5 inline-block" /> /* spacer */
            )}
          </div>
          
          <div className="truncate flex-1 cursor-pointer" onClick={() => handleSelect(node)}>
            {node.name}
          </div>
          
          {/* Subcategory count badge */}
          {node.subCategoryCount > 0 && (
             <span className="text-[10px] bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded-full dark:bg-gray-800 dark:text-gray-400 ml-2">
                {node.subCategoryCount} sub
             </span>
          )}
        </div>
        
        {node.isExpanded && node.children && (
          <div>
            {renderNodes(node.children, level + 1)}
          </div>
        )}
      </div>
    ));
  };

  return (
    <div className={`relative ${className}`} ref={selectRef}>
      <div
        className={`w-full cursor-pointer appearance-none rounded-lg border bg-transparent px-4 py-3 pr-10 text-sm outline-none bg-no-repeat bg-[position:right_1rem_center] bg-[length:1.25rem_1.25rem] ${
          error 
            ? "border-error-500 focus:border-error-300 focus:ring-error-500/20 dark:border-error-500 dark:focus:border-error-800" 
            : "border-gray-200 focus:border-brand-500 dark:border-gray-800 dark:focus:border-brand-500"
        } ${
          value ? "text-gray-800 dark:text-white/90" : "text-gray-500 dark:text-gray-400"
        }`}
        style={{
          backgroundImage: `url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236b7280'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'/%3E%3C/svg%3E")`,
        }}
        onClick={() => setIsOpen(!isOpen)}
      >
        <span className="block truncate">
          {selectedLabel || placeholder}
        </span>
      </div>

      {isOpen && (
        <div className={`absolute left-0 top-full z-50 mt-1 ${maxHeightClass} flex w-full flex-col overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-800 dark:bg-gray-900`}>
          <div className="flex-1 overflow-auto py-1">
            {isLoadingRoots ? (
              <div className="flex justify-center p-4">
                 <svg className="w-5 h-5 animate-spin text-brand-500" fill="none" viewBox="0 0 24 24">
                   <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                   <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                 </svg>
              </div>
            ) : nodes.length > 0 ? (
              renderNodes(nodes)
            ) : (
              <div className="px-4 py-3 text-center text-sm text-gray-500 dark:text-gray-400">
                No categories found
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
