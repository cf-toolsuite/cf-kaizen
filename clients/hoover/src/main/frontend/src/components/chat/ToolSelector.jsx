import React, { useEffect, useState } from 'react';
import { Hammer, ChevronDown, Info, X } from 'lucide-react';
import { useChat } from '../../context/ChatContext';

const ToolSelector = () => {
  const {
    isDarkMode,
    availableTools,
    selectedTools,
    showToolsMenu,
    toggleToolsMenu,
    handleToolSelect,
    handleSelectAll,
    handleClearAll,
    allToolsSelected,
    noToolsSelected,
    tooltipText,
    showTooltip,
    tooltipAnchor,
    handleTooltipClick,
    tooltipRef,
    historyButtonRef
  } = useChat();

  // State to track tooltip position
  const [tooltipPosition, setTooltipPosition] = useState({ top: 0, left: 0 });
  // State to track if we're on mobile
  const [isMobile, setIsMobile] = useState(false);

  // Check if we're on mobile
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Function to calculate the best position for the tooltip
  useEffect(() => {
    if (showTooltip && tooltipAnchor && tooltipRef.current) {
      const anchorRect = tooltipAnchor.getBoundingClientRect();
      const tooltipRect = tooltipRef.current.getBoundingClientRect();
      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;

      // Default position to the right of the anchor
      let left = anchorRect.right + 5;
      let top = anchorRect.top - 10;

      // Check if tooltip would go off the right edge
      if (left + tooltipRect.width > viewportWidth) {
        // Position to the left of the anchor
        left = Math.max(10, anchorRect.left - tooltipRect.width - 5);
      }

      // Check if tooltip would go off the bottom edge
      if (top + tooltipRect.height > viewportHeight) {
        // Position above the anchor
        top = Math.max(10, viewportHeight - tooltipRect.height - 10);
      }

      // Check if tooltip would go off the top edge
      if (top < 10) {
        top = 10;
      }

      setTooltipPosition({ top, left });
    }
  }, [showTooltip, tooltipAnchor, tooltipRef]);

  // Prevent info icon click from toggling checkbox
  const handleInfoIconClick = (key, description, e) => {
    e.preventDefault(); // Prevent the label's default behavior
    e.stopPropagation(); // Stop event from bubbling up
    handleTooltipClick(key, description, e);
  };

  return (
    <div className="relative tools-menu-container h-full">
      <button
        onClick={(e) => toggleToolsMenu(e)}
        type="button"
        className={`flex items-center justify-center gap-1 px-4 py-2 h-full rounded text-sm ${
          isDarkMode
            ? 'bg-gray-700 text-white hover:bg-gray-600'
            : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
        } ${selectedTools.length > 0 ? 'border-2 border-blue-500' : ''}`}
        title={`${selectedTools.length} tools selected`}
      >
        <Hammer size={16} />
        <span>{selectedTools.length > 0 ? ` ${selectedTools.length}` : ''}</span>
        <ChevronDown size={14} />
      </button>

      {/* Desktop Tools Menu */}
      {showToolsMenu && !isMobile && (
        <div
          className={`absolute left-0 bottom-full mb-1 rounded-md shadow-lg z-10 ${
            isDarkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'
          } border max-h-64 overflow-y-auto`}
          style={{ width: (historyButtonRef?.current?.offsetWidth || 150) * 4 }}
        >
          {/* Fixed header */}
          <div
            className="sticky top-0 p-2 border-b font-semibold flex justify-between items-center bg-inherit z-10"
          >
            <span>Available Tools</span>
            <div className="flex gap-3">
              <button
                onClick={handleSelectAll}
                type="button"
                className={`text-xs text-blue-500 hover:text-blue-700 ${
                  allToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                }`}
                disabled={allToolsSelected}
              >
                Select All
              </button>
              <button
                onClick={handleClearAll}
                type="button"
                className={`text-xs text-blue-500 hover:text-blue-700 ${
                  noToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                }`}
                disabled={noToolsSelected}
              >
                Clear All
              </button>
            </div>
          </div>

          {/* Scrollable content */}
          <ToolSelectorContent
            availableTools={availableTools}
            selectedTools={selectedTools}
            handleToolSelect={handleToolSelect}
            handleInfoIconClick={handleInfoIconClick}
            isDarkMode={isDarkMode}
          />
        </div>
      )}

      {/* Mobile Tools Menu - Full screen overlay */}
      {showToolsMenu && isMobile && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center">
          <div
            className={`w-11/12 max-w-lg max-h-[80vh] rounded-lg shadow-xl ${
              isDarkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'
            } flex flex-col`}
          >
            <div className="p-3 border-b flex justify-between items-center">
              <h3 className="font-semibold text-lg">Available Tools</h3>
              <div className="flex items-center gap-3">
                <button
                  onClick={handleSelectAll}
                  type="button"
                  className={`text-sm text-blue-500 hover:text-blue-700 ${
                    allToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                  }`}
                  disabled={allToolsSelected}
                >
                  Select All
                </button>
                <button
                  onClick={handleClearAll}
                  type="button"
                  className={`text-sm text-blue-500 hover:text-blue-700 ${
                    noToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                  }`}
                  disabled={noToolsSelected}
                >
                  Clear All
                </button>
                <button
                  onClick={() => toggleToolsMenu(null)}
                  className={`p-2 rounded-full ${
                    isDarkMode ? 'hover:bg-gray-700' : 'hover:bg-gray-200'
                  }`}
                  aria-label="Close tools menu"
                >
                  <X size={18} />
                </button>
              </div>
            </div>

            <div className="overflow-y-auto p-3 flex-grow">
              <ToolSelectorContent
                availableTools={availableTools}
                selectedTools={selectedTools}
                handleToolSelect={handleToolSelect}
                handleInfoIconClick={handleInfoIconClick}
                isDarkMode={isDarkMode}
              />
            </div>

            <div className="p-3 border-t flex justify-end">
              <button
                onClick={() => toggleToolsMenu(null)}
                className={`px-4 py-2 rounded text-sm ${
                  isDarkMode
                    ? 'bg-blue-600 text-white hover:bg-blue-700'
                    : 'bg-blue-500 text-white hover:bg-blue-600'
                }`}
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Tooltip positioned using the dynamic calculation */}
      {showTooltip && tooltipText && (
        <div
          ref={tooltipRef}
          className={`fixed p-3 rounded-md shadow-lg z-50 max-w-xs tooltip-content ${
            isDarkMode ? 'bg-gray-700 text-white' : 'bg-white text-gray-900'
          } border overflow-y-auto`}
          style={{
            maxHeight: '30vh',
            top: `${tooltipPosition.top}px`,
            left: `${tooltipPosition.left}px`
          }}
          onClick={(e) => e.stopPropagation()}
        >
          {tooltipText}
        </div>
      )}
    </div>
  );
};

// Extract the tool selector content to a separate component for reuse
const ToolSelectorContent = ({ availableTools, selectedTools, handleToolSelect, handleInfoIconClick, isDarkMode }) => {
  return (
    <div className="p-2">
      {Object.entries(availableTools)
        .sort((a, b) => a[1].displayName.localeCompare(b[1].displayName))
        .map(([key, value]) => (
          <div key={key} className="flex items-start mb-2 relative">
            <input
              type="checkbox"
              id={`tool-${key}`}
              checked={selectedTools.includes(key)}
              onChange={() => handleToolSelect(key)}
              className={`mr-2 mt-1 ${isDarkMode ? 'bg-gray-700' : 'bg-white'}`}
            />
            <div className="flex-grow">
              <label
                htmlFor={`tool-${key}`}
                className="flex items-center group relative"
              >
                {/* Use span with tooltip-label class for the label text */}
                <span
                  className="hover:underline tooltip-label cursor-pointer"
                  onClick={() => handleToolSelect(key)}
                >
                  {value.displayName}
                </span>

                {/* Separated info icon that won't trigger checkbox toggle */}
                <span
                  className="ml-1 text-gray-400 hover:text-gray-600 cursor-pointer tooltip-icon"
                  onClick={(e) => handleInfoIconClick(key, value.description, e)}
                >
                  <Info size={14} />
                </span>
              </label>
            </div>
          </div>
        ))}
    </div>
  );
};

export default React.memo(ToolSelector);
