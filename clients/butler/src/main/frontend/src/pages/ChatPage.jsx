import React from 'react';
import ResponsiveContainer from '../components/layout/ResponsiveContainer';
import { ChatProvider } from '../context/ChatContext';
import ChatContainer from '../components/chat/ChatContainer';
import '@/components/ui/markdown-styles.css';

/**
 * ChatPage component that serves as the top-level component for the chat interface.
 * It wraps the entire chat functionality in the ChatProvider context.
 */
const ChatPage = ({ isDarkMode }) => {
  return (
    <ChatProvider isDarkMode={isDarkMode}>
      <ResponsiveContainer>
        <ChatContainer />
      </ResponsiveContainer>
    </ChatProvider>
  );
};

export default ChatPage;
