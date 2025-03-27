import { useState, useCallback } from 'react';

export const useTooltip = () => {
  const [tooltipText, setTooltipText] = useState('');
  const [showTooltip, setShowTooltip] = useState(false);
  const [hoveredKey, setHoveredKey] = useState(null);
  const [tooltipAnchor, setTooltipAnchor] = useState(null);
  
  const handleTooltipMouseEnter = useCallback((key, text, event) => {
    setTooltipText(text);
    setHoveredKey(key);
    setShowTooltip(true);
    setTooltipAnchor(event.currentTarget);
  }, []);
  
  const handleTooltipMouseLeave = useCallback(() => {
    setShowTooltip(false);
    setHoveredKey(null);
    setTooltipAnchor(null);
  }, []);
  
  return {
    tooltipText,
    showTooltip,
    hoveredKey,
    tooltipAnchor,
    handleTooltipMouseEnter,
    handleTooltipMouseLeave
  };
};
