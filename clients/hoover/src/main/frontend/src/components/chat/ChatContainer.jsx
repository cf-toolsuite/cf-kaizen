import React, { useEffect, useState, useRef } from 'react';
import { useSwipeable } from 'react-swipeable';
import { SidebarClose, SidebarOpen, X } from 'lucide-react';
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

  // Refs for measuring
  const inputPanelRef = useRef(null);
  const messageDisplayRef = useRef(null);

  // State for layout calculations
  const [mainContentWidth, setMainContentWidth] = useState('100%');
  const [historyTopPadding, setHistoryTopPadding] = useState('0px');

  // Calculate the padding needed to align the first history item with the bottom of the question panel
  useEffect(() => {
    if (showHistory && window.innerWidth >= 768) {
      const calculateHistoryPadding = () => {
        const messageDisplayHeight = messageDisplayRef.current?.clientHeight || 0;
        const inputPanelHeight = inputPanelRef.current?.clientHeight || 0;
        
        // Get the height of a typical history item (approximate)
        const historyItemHeight = 64; // Approx height of a single history item
        
        // Calculate padding needed to push first item to align with bottom of question panel
        const totalMainContentHeight = messageDisplayHeight + inputPanelHeight;
        const paddingTop = Math.max(0, totalMainContentHeight - historyItemHeight);
        
        setHistoryTopPadding(`${paddingTop}px`);
      };
      
      // Run calculation after a short delay to ensure components have rendered
      const timer = setTimeout(calculateHistoryPadding, 50);
      
      // Also recalculate on window resize
      window.addEventListener('resize', calculateHistoryPadding);
      
      return () => {
        clearTimeout(timer);
        window.removeEventListener('resize', calculateHistoryPadding);
      };
    }
  }, [showHistory]);

  // Effect to handle resizing and calculate widths
  useEffect(() => {
    const updateLayout = () => {
      if (showHistory && window.innerWidth >= 768) {
        setMainContentWidth('calc(66.666% - 1.5rem)');
      } else {
        setMainContentWidth('100%');
      }
    };

    updateLayout();
    window.addEventListener('resize', updateLayout);
    return () => window.removeEventListener('resize', updateLayout);
  }, [showHistory]);

  // Close history sidebar when screen becomes smaller than md breakpoint
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 768 && showHistory) {
        setShowHistory(false);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [showHistory, setShowHistory]);

  // Swipe gesture handlers
  const swipeHandlers = useSwipeable({
    onSwipedRight: () => !showHistory && setShowHistory(true),
    onSwipedLeft: () => showHistory && setShowHistory(false),
    preventDefaultTouchmoveEvent: true,
    trackMouse: false,
    delta: 50 // Minimum distance (in px) before a swipe is detected
  });

  return (
    <div className="w-full flex flex-col" {...swipeHandlers}>
      {/* History visibility toggle button */}
      <div className="flex justify-end mb-2 md:mb-4 mt-3 md:mt-5">
        <button
          ref={historyButtonRef}
          onClick={() => setShowHistory(!showHistory)}
          className={`flex items-center gap-1 px-3 py-1 md:px-4 md:py-2 rounded text-sm md:text-base ${
            isDarkMode ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
          }`}
        >
          {showHistory ? (
            <>
              <SidebarClose size={16} className="mr-1" />
              <span className="hidden xs:inline">Hide History</span>
            </>
          ) : (
            <>
              <SidebarOpen size={16} className="mr-1" />
              <span className="hidden xs:inline">Show History</span>
            </>
          )}
        </button>
      </div>

      {/* Mobile history overlay */}
      {showHistory && (
        <div className="md:hidden fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-end">
          <div 
            className={`w-4/5 max-w-xs h-full ${
              isDarkMode ? 'bg-gray-800' : 'bg-white'
            } p-4 overflow-y-auto flex flex-col`}
          >
            <div className="flex justify-between items-center mb-3">
              <h3 className="font-semibold">Chat History</h3>
              <button 
                onClick={() => setShowHistory(false)}
                className={`p-2 rounded-full ${
                  isDarkMode ? 'hover:bg-gray-700' : 'hover:bg-gray-200'
                }`}
                aria-label="Close history"
              >
                <X size={18} />
              </button>
            </div>
            <div className="flex-grow overflow-y-auto">
              <ChatHistory />
            </div>
          </div>
        </div>
      )}
      
      {/* Main layout - changed to use responsive classes */}
      <div className="flex flex-col md:flex-row space-y-4 md:space-y-0 md:space-x-6">
        <div 
          ref={chatContainerRef} 
          className="w-full transition-all duration-300 ease-in-out flex flex-col"
          style={{ width: mainContentWidth }}
        >
          <AlertNotification alert={alert} />
          
          {/* Add ref to MessageDisplay for height calculations */}
          <div ref={messageDisplayRef}>
            <MessageDisplay />
          </div>
          
          {/* Add ref to ChatInput for height calculations */}
          <div ref={inputPanelRef}>
            <ChatInput />
          </div>
        </div>
        
        {/* Desktop history sidebar - only visible on md screens and up */}
        {showHistory && (
          <div 
            className="hidden md:flex md:w-1/3 flex-col md:border-l md:pl-5 md:overflow-y-auto"
            style={{ 
              maxHeight: 'calc(100vh - 180px)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'flex-end'
            }}
          >
            {/* This spacer pushes the content down to align with the bottom */}
            <div style={{ paddingTop: historyTopPadding }} className="flex-grow"></div>
            
            <ChatHistory />
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatContainer;
