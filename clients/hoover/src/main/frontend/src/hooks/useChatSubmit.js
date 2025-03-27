import { useCallback } from 'react';

export const useChatSubmit = (
  question,
  setQuestion,
  setIsLoading,
  setCurrentQuestion,
  setAnswer,
  setCurrentAnswer,
  setCurrentMetadata,
  showAlert,
  selectedTools,
  addToHistory,
  answerContainerRef,
  getHistoryItemColor,
  getHistoryItemTitleColor
) => {
  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();

    if (!question.trim()) {
      showAlert('Please enter a question');
      return;
    }

    if (selectedTools.length === 0) {
      showAlert('Please select at least one tool');
      return;
    }

    setIsLoading(true);
    const questionText = question;
    setCurrentQuestion(questionText);
    setQuestion('');
    setAnswer('');
    setCurrentAnswer('');
    setCurrentMetadata(null);

    try {
      const response = await fetch('/api/hoover/stream/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          question: questionText,
          tools: selectedTools,
        }),
      });

      if (!response.ok) throw new Error('Chat request failed');

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let fullAnswer = '';
      let latestMetadata = null;

      // Start with the robot emoji
      setAnswer('🤖 ');
      setCurrentAnswer('🤖 ');

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value);

        try {
          // Check if this is a metadata chunk (JSON object)
          const parsedChunk = JSON.parse(chunk);
          if (parsedChunk.isMetadata && parsedChunk.metadata) {
            latestMetadata = parsedChunk.metadata;
            setCurrentMetadata(latestMetadata);
            continue; // Skip adding this to the visible answer
          }
        } catch (e) {
          // Not JSON, treat as normal text content
        }

        fullAnswer += chunk;
        setCurrentAnswer((prev) => prev + chunk);
        setAnswer((prev) => prev + chunk);

        if (answerContainerRef.current) {
          answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
        }
      }

      // Save to chat history after streaming is complete
      addToHistory(
        questionText,
        '🤖 ' + fullAnswer,
        latestMetadata,
        selectedTools,
        getHistoryItemColor(),
        getHistoryItemTitleColor()
      );
    } catch (error) {
      console.error('Error processing chat request:', error);
      showAlert('Error processing chat request');
    } finally {
      setIsLoading(false);
    }
  }, [
    question,
    selectedTools,
    setQuestion,
    setIsLoading,
    setCurrentQuestion,
    setAnswer,
    setCurrentAnswer,
    setCurrentMetadata,
    showAlert,
    addToHistory,
    answerContainerRef,
    getHistoryItemColor,
    getHistoryItemTitleColor
  ]);

  return {
    handleSubmit
  };
};
