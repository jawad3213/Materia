import React, { useState, useRef, useEffect } from "react";

interface PopoverProps {
  children: React.ReactNode;
  title: string;
  content: React.ReactNode;
  position?: "top" | "bottom" | "left" | "right";
  trigger?: "click" | "hover";
}

export default function Popover({
  children,
  title,
  content,
  position = "top",
  trigger = "click",
}: PopoverProps) {
  const [isOpen, setIsOpen] = useState(false);
  const popoverRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (trigger === "hover") return;
    
    const handleClickOutside = (event: MouseEvent) => {
      if (popoverRef.current && !popoverRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [trigger]);

  const handleMouseEnter = () => {
    if (trigger === "hover") setIsOpen(true);
  };

  const handleMouseLeave = () => {
    if (trigger === "hover") setIsOpen(false);
  };

  const handleClick = () => {
    if (trigger === "click") setIsOpen(!isOpen);
  };

  const getPositionClasses = () => {
    switch (position) {
      case "top":
        return "bottom-full left-1/2 -translate-x-1/2 mb-3";
      case "bottom":
        return "top-full left-1/2 -translate-x-1/2 mt-3";
      case "left":
        return "right-full top-1/2 -translate-y-1/2 mr-3";
      case "right":
        return "left-full top-1/2 -translate-y-1/2 ml-3";
      default:
        return "bottom-full left-1/2 -translate-x-1/2 mb-3";
    }
  };

  const getArrowClasses = () => {
    switch (position) {
      case "top":
        return "top-full left-1/2 -translate-x-1/2 border-t-white dark:border-t-gray-800 border-x-transparent border-b-transparent";
      case "bottom":
        return "bottom-full left-1/2 -translate-x-1/2 border-b-white dark:border-b-gray-800 border-x-transparent border-t-transparent";
      case "left":
        return "left-full top-1/2 -translate-y-1/2 border-l-white dark:border-l-gray-800 border-y-transparent border-r-transparent";
      case "right":
        return "right-full top-1/2 -translate-y-1/2 border-r-white dark:border-r-gray-800 border-y-transparent border-l-transparent";
      default:
        return "top-full left-1/2 -translate-x-1/2 border-t-white dark:border-t-gray-800 border-x-transparent border-b-transparent";
    }
  };

  return (
    <div
      className="relative inline-block"
      ref={popoverRef}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      onClick={handleClick}
    >
      {children}
      
      {isOpen && (
        <div
          className={`absolute z-50 w-72 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl shadow-theme-lg ${getPositionClasses()}`}
        >
          {/* Arrow */}
          <div
            className={`absolute border-[7px] ${getArrowClasses()}`}
            style={{
              filter:
                position === "top"
                  ? "drop-shadow(0px 2px 1px rgba(0,0,0,0.05))"
                  : position === "bottom"
                  ? "drop-shadow(0px -2px 1px rgba(0,0,0,0.05))"
                  : position === "left"
                  ? "drop-shadow(2px 0px 1px rgba(0,0,0,0.05))"
                  : "drop-shadow(-2px 0px 1px rgba(0,0,0,0.05))",
            }}
          ></div>
          
          <div className="px-5 py-4 border-b border-gray-200 dark:border-gray-700">
            <h3 className="font-semibold text-gray-800 dark:text-white/90">
              {title}
            </h3>
          </div>
          <div className="px-5 py-4">
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {content}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
