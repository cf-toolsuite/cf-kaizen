import { useState, useCallback, useMemo } from 'react';
import { processMarkdown } from '@/utils/markdownProcessor';

export const useChatState = () => {
  const [question, setQuestion] = useState('');
  const [currentQuestion, setCurrentQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [currentAnswer, setCurrentAnswer] = useState('');
  const [currentMetadata, setCurrentMetadata] = useState(null);
  const [chatHistory, setChatHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [greeting, setGreeting] = useState('');

  // Process the markdown on change
  const processedAnswer = useMemo(() =>
    processMarkdown(currentAnswer),
    [currentAnswer]
  );

  const resetChat = useCallback(() => {
    setCurrentQuestion('');
    setAnswer('');
    setCurrentAnswer('');
    setCurrentMetadata(null);
  }, []);

  const addToHistory = useCallback((questionText, answerText, metadata, tools, color, titleColor) => {
    setChatHistory(prev => {
      const newHistory = [
        {
          question: questionText,
          fullQuestionText: questionText, // Store the full question text
          answer: answerText,
          expanded: false,
          questionNumber: prev.length > 0 ? prev[0].questionNumber + 1 : 1,
          color,
          titleColor,
          metadata,
          tools: tools && tools.length > 0 ? [...tools] : [],
          timestamp: new Date(), // Add timestamp
        },
        ...prev,
      ];

      // Limit history to 10 items
      if (newHistory.length > 10) {
        return newHistory.slice(0, 10);
      }
      return newHistory;
    });
  }, []);

  const toggleHistoryItem = useCallback((index) => {
    setChatHistory(prev => {
      // Create a new array with all items collapsed first
      const updatedHistory = prev.map((item) => ({
        ...item,
        expanded: false,
      }));

      // Toggle the clicked item
      updatedHistory[index] = {
        ...updatedHistory[index],
        expanded: !prev[index].expanded,
      };

      return updatedHistory;
    });
  }, []);

  // Fetch greeting message on first load
  const fetchGreeting = useCallback(async () => {
    try {
      const response = await fetch('/api/butler/greeting');
      if (response.ok) {
        const greetingText = await response.text();
        setGreeting(greetingText);
      }
    } catch (error) {
      console.error('Error fetching greeting:', error);
    }
  }, []);

  return {
    // State
    question,
    setQuestion,
    currentQuestion,
    setCurrentQuestion,
    answer,
    setAnswer,
    currentAnswer,
    setCurrentAnswer,
    currentMetadata,
    setCurrentMetadata,
    chatHistory,
    setChatHistory,
    isLoading,
    setIsLoading,
    greeting,
    processedAnswer,

    // Actions
    resetChat,
    addToHistory,
    toggleHistoryItem,
    fetchGreeting
  };
};
