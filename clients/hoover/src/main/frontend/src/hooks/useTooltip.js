import { useState, useCallback, useEffect } from 'react';

export const useTooltip = () => {
  const [tooltipText, setTooltipText] = useState('');
  const [showTooltip, setShowTooltip] = useState(false);
  const [hoveredKey, setHoveredKey] = useState(null);
  const [tooltipAnchor, setTooltipAnchor] = useState(null);

  // New function to handle tooltip click instead of hover
  const handleTooltipClick = useCallback((key, text, event) => {
    event.stopPropagation();

    // If already showing this tooltip, close it
    if (showTooltip && hoveredKey === key) {
      setShowTooltip(false);
      setHoveredKey(null);
      setTooltipAnchor(null);
    } else {
      // Show new tooltip
      setTooltipText(text);
      setHoveredKey(key);
      setShowTooltip(true);
      setTooltipAnchor(event.currentTarget);
    }
  }, [showTooltip, hoveredKey]);

  // Keep for backward compatibility but remove hover behavior
  const handleTooltipMouseEnter = useCallback(() => {
    // No longer triggers tooltip
  }, []);

  const handleTooltipMouseLeave = useCallback(() => {
    // No longer auto-closes tooltip
  }, []);

  // Function to close tooltip
  const closeTooltip = useCallback(() => {
    setShowTooltip(false);
    setHoveredKey(null);
    setTooltipAnchor(null);
  }, []);

  // Add global click handler to close tooltip when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showTooltip &&
          tooltipAnchor &&
          !tooltipAnchor.contains(event.target) &&
          !event.target.closest('.tooltip-content')) {
        closeTooltip();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showTooltip, tooltipAnchor, closeTooltip]);

  return {
    tooltipText,
    showTooltip,
    hoveredKey,
    tooltipAnchor,
    handleTooltipClick,
    handleTooltipMouseEnter,
    handleTooltipMouseLeave,
    closeTooltip
  };
};
