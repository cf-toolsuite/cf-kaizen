import React, { createContext, useContext, useRef, useEffect } from 'react';
import { useChatState } from '../hooks/useChatState';
import { useToolSelection } from '../hooks/useToolSelection';
import { useAlertState } from '../hooks/useAlertState';
import { useTooltip } from '../hooks/useTooltip';
import { useChatSubmit } from '../hooks/useChatSubmit';

const ChatContext = createContext(null);

export const ChatProvider = ({ children, isDarkMode }) => {
  const answerContainerRef = useRef(null);
  const chatContainerRef = useRef(null);
  const historyButtonRef = useRef(null);
  const tooltipRef = useRef(null);

  const getHistoryItemColor = () => {
    return isDarkMode ? 'bg-teal-800' : 'bg-gray-50';
  };

  const getHistoryItemTitleColor = () => {
    return isDarkMode ? 'bg-teal-900' : 'bg-blue-100';
  };

  // Initialize all our hooks
  const chatState = useChatState();
  const toolState = useToolSelection();
  const alertState = useAlertState();
  const tooltipState = useTooltip();

  // State for history visibility
  const [showHistory, setShowHistory] = React.useState(false);

  // Initialize the submit handler
  const { handleSubmit } = useChatSubmit(
    chatState.question,
    chatState.setQuestion,
    chatState.setIsLoading,
    chatState.setCurrentQuestion,
    chatState.setAnswer,
    chatState.setCurrentAnswer,
    chatState.setCurrentMetadata,
    alertState.showAlert,
    toolState.selectedTools,
    chatState.addToHistory,
    answerContainerRef,
    getHistoryItemColor,
    getHistoryItemTitleColor
  );

  // Fetch data when component mounts
  useEffect(() => {
    toolState.fetchTools();
    chatState.fetchGreeting();
  }, [toolState.fetchTools, chatState.fetchGreeting]);

  // Auto-scroll to bottom when new content is added
  useEffect(() => {
    if (answerContainerRef.current) {
      answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
    }
  }, [chatState.answer]);

  // Handle clicks outside of tools menu only (tooltips handled by useTooltip hook)
  useEffect(() => {
    const handleClickOutside = (event) => {
      // Close tools menu when clicking outside
      if (
        toolState.showToolsMenu &&
        !event.target.closest('.tools-menu-container') &&
        !event.target.closest('.tooltip-content')
      ) {
        toolState.setShowToolsMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [toolState.showToolsMenu, toolState.setShowToolsMenu]);

  // Function to resubmit a question from history
  const resubmitQuestion = (fullQuestionText, originalTools = []) => {
    // Set the question in the textarea
    chatState.setQuestion(fullQuestionText);
    
    // After a small delay to allow question state to update
    setTimeout(() => {
      // Set the tools that were originally used with this question
      if (originalTools && originalTools.length > 0) {
        // Extract just the tool IDs if tools are objects
        const toolIds = originalTools.map(tool => 
          typeof tool === 'string' ? tool : tool.id
        );
        toolState.setSelectedTools(toolIds);
      } 
      // If no original tools or empty tools array, ensure at least one tool is selected
      else if (toolState.selectedTools.length === 0 && toolState.availableTools.length > 0) {
        toolState.setSelectedTools([toolState.availableTools[0].id]);
      }
      
      // Focus the textarea to ensure the user sees where the text was populated
      const textarea = document.querySelector('textarea');
      if (textarea) {
        textarea.focus();
      }
    }, 100); // Small delay to ensure React state updates
  };

  // Check if submit should be disabled
  const isSubmitDisabled = chatState.isLoading ||
                           !chatState.question.trim() ||
                           toolState.selectedTools.length === 0;

  // Combine all the state and functions into a single value object
  const value = {
    // Refs
    answerContainerRef,
    chatContainerRef,
    historyButtonRef,
    tooltipRef,

    // State from hooks
    ...chatState,
    ...toolState,
    ...alertState,
    hideAlert: alertState.hideAlert,
    ...tooltipState,

    // History visibility state
    showHistory,
    setShowHistory,

    // Theme helpers
    isDarkMode,
    getHistoryItemColor,
    getHistoryItemTitleColor,

    // Derived state
    isSubmitDisabled,

    // Actions
    handleSubmit,
    resubmitQuestion
  };

  return (
    <ChatContext.Provider value={value}>
      {children}
    </ChatContext.Provider>
  );
};

export const useChat = () => {
  const context = useContext(ChatContext);
  if (context === null) {
    throw new Error('useChat must be used within a ChatProvider');
  }
  return context;
};
