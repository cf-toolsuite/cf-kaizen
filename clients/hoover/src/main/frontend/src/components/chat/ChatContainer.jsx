import React from 'react';
import { SidebarClose, SidebarOpen } from 'lucide-react';
import MessageDisplay from './MessageDisplay';
import ChatInput from './ChatInput';
import ChatHistory from './ChatHistory';
import AlertNotification from './AlertNotification';
import { useChat } from '../../context/ChatContext';

const ChatContainer = () => {
  const {
    alert,
    showHistory,
    setShowHistory,
    chatContainerRef,
    historyButtonRef,
    isDarkMode
  } = useChat();

  return (
    <div className="max-w-4xl mx-auto p-6 flex flex-col">
      {/* History visibility toggle button */}
      <div className="flex justify-end mb-2">
        <button
          ref={historyButtonRef}
          onClick={() => setShowHistory(!showHistory)}
          className={`flex items-center gap-1 px-3 py-1 rounded text-sm ${
            isDarkMode ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
          }`}
        >
          {showHistory ? (
            <>
              <SidebarClose size={16} />
              <span>Hide History</span>
            </>
          ) : (
            <>
              <SidebarOpen size={16} />
              <span>Show History</span>
            </>
          )}
        </button>
      </div>
      <div className="flex">
        <div ref={chatContainerRef} className={showHistory ? 'w-2/3 pr-4' : 'w-full'}>
          <AlertNotification alert={alert} />
          <MessageDisplay />
          <ChatInput />
        </div>
        {showHistory && (
          <div className="w-1/3 flex flex-col justify-end">
            <ChatHistory />
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatContainer;
