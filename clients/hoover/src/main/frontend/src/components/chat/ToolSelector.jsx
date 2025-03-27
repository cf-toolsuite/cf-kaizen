import React from 'react';
import { Wrench, ChevronDown, Info } from 'lucide-react';
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
    handleTooltipMouseEnter,
    handleTooltipMouseLeave,
    tooltipRef,
    historyButtonRef
  } = useChat();

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
        <Wrench size={16} />
        <span>Tools{selectedTools.length > 0 ? ` (${selectedTools.length})` : ''}</span>
        <ChevronDown size={14} />
      </button>

      {showToolsMenu && (
        <div
          className={`absolute left-0 mt-1 rounded-md shadow-lg z-10 ${
            isDarkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'
          } border max-h-64 overflow-y-auto`}
          style={{ width: (historyButtonRef?.current?.offsetWidth || 150) * 5 }} // Quintuple the width
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
                      className="font-medium cursor-pointer flex items-center group relative"
                    >
                      <span
                        className="hover:underline"
                        onMouseEnter={(e) => handleTooltipMouseEnter(key, value.description, e)}
                        onMouseLeave={handleTooltipMouseLeave}
                      >
                        {value.displayName}
                      </span>
                      <span
                        className="ml-1 text-gray-400 hover:text-gray-600 cursor-help tooltip-icon"
                        onMouseEnter={(e) => handleTooltipMouseEnter(key, value.description, e)}
                        onMouseLeave={handleTooltipMouseLeave}
                      >
                        <Info size={14} />
                      </span>
                    </label>
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Tooltip positioned relative to the anchor element */}
      {showTooltip && tooltipText && tooltipAnchor && (
        <div
          ref={tooltipRef}
          className={`fixed p-3 rounded-md shadow-lg z-20 max-w-xs ${
            isDarkMode ? 'bg-gray-700 text-white' : 'bg-white text-gray-900'
          } border`}
          style={{
            maxHeight: '30vh',
            overflow: 'auto',
            top: `${tooltipAnchor.getBoundingClientRect().bottom + window.scrollY + 5}px`, // Position below the anchor
            left: `${tooltipAnchor.getBoundingClientRect().left + window.scrollX}px`, // Align with the left edge of the anchor
          }}
          onClick={(e) => e.stopPropagation()}
        >
          {tooltipText}
        </div>
      )}
    </div>
  );
};

export default React.memo(ToolSelector);
