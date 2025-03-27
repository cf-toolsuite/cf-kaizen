import { useState, useEffect, useCallback } from 'react';

export const useToolSelection = () => {
  const [availableTools, setAvailableTools] = useState({});
  const [selectedTools, setSelectedTools] = useState([]);
  const [showToolsMenu, setShowToolsMenu] = useState(false);
  
  // Fetch available tools and process them
  const fetchTools = useCallback(async () => {
    try {
      const response = await fetch('/api/butler/tools');
      if (response.ok) {
        const toolsData = await response.json();

        // Process tool names to remove any prefixes
        const cleanedTools = {};
        Object.entries(toolsData).forEach(([name, description]) => {
          // Extract just the last part of the tool name if it contains a prefix
          const cleanName = name.includes('_') ? name.split('_').pop() : name;
          cleanedTools[name] = {
            displayName: cleanName,
            description: description,
          };
        });

        setAvailableTools(cleanedTools);

        // Select all tools by default
        setSelectedTools(Object.keys(cleanedTools));
      }
    } catch (error) {
      console.error('Error fetching tools:', error);
    }
  }, []);

  const handleToolSelect = useCallback((toolName) => {
    setSelectedTools((prev) => {
      // If tool is already selected, remove it, otherwise add it
      if (prev.includes(toolName)) {
        return prev.filter((tool) => tool !== toolName);
      } else {
        return [...prev, toolName];
      }
    });
  }, []);

  const handleSelectAll = useCallback((e) => {
    e.stopPropagation();
    setSelectedTools(Object.keys(availableTools));
  }, [availableTools]);

  const handleClearAll = useCallback((e) => {
    e.stopPropagation();
    setSelectedTools([]);
  }, []);
  
  const toggleToolsMenu = useCallback((e) => {
    // Prevent the event from propagating to the form
    if (e) e.preventDefault();
    setShowToolsMenu(prev => !prev);
  }, []);
  
  // Check if all tools are selected
  const allToolsSelected = Object.keys(availableTools).length > 0 &&
    Object.keys(availableTools).length === selectedTools.length;

  // Check if no tools are selected
  const noToolsSelected = selectedTools.length === 0;
  
  return {
    availableTools,
    selectedTools,
    setSelectedTools,
    showToolsMenu,
    setShowToolsMenu,
    allToolsSelected,
    noToolsSelected,
    fetchTools,
    handleToolSelect,
    handleSelectAll,
    handleClearAll,
    toggleToolsMenu
  };
};
