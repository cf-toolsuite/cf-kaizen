import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeFormat from 'rehype-format';
import remarkBreaks from 'remark-breaks';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import MetadataDisplay from './MetadataDisplay';
import { useChat } from '../../context/ChatContext';
import Link from '../ui/common/Link';

const MessageDisplay = () => {
  const {
    currentQuestion,
    processedAnswer,
    currentMetadata,
    isDarkMode,
    greeting,
    selectedTools,
    currentAnswer,
    answerContainerRef
  } = useChat();

  // State to store the calculated height
  const [containerHeight, setContainerHeight] = useState('480px');

  // Effect to calculate and set the optimal container height
  useEffect(() => {
    const calculateHeight = () => {
      // Get the viewport height
      const viewportHeight = window.innerHeight;
      
      // Calculate available space (subtracting space for header, input, padding, etc.)
      // These values can be adjusted based on your specific layout
      const headerHeight = 80; // Approx. header height
      const inputHeight = 140; // Approx. input area height
      const buttonSpace = 60; // Approx. space for buttons
      const margins = 100; // Approx. total margins and padding
      
      // Calculate available height for the message container
      const availableHeight = viewportHeight - headerHeight - inputHeight - buttonSpace - margins;
      
      // Ensure we have a minimum height
      const optimalHeight = Math.max(availableHeight, 300);
      
      // Set the container height
      setContainerHeight(`${optimalHeight}px`);
    };
    
    // Calculate on mount and window resize
    calculateHeight();
    window.addEventListener('resize', calculateHeight);
    
    // Cleanup event listener
    return () => window.removeEventListener('resize', calculateHeight);
  }, []);

  return (
    <div
      ref={answerContainerRef}
      className={`p-2 xs:p-3 sm:p-4 md:p-6 rounded-lg mb-2 xs:mb-3 sm:mb-4 md:mb-5 overflow-y-auto relative ${
        isDarkMode ? 'bg-gray-800 prose-invert max-w-none' : 'bg-gray-50 prose-slate max-w-none'
      }`}
      style={{ height: containerHeight }}
    >
      {/* Greeting message shown at the top */}
      {greeting && !currentQuestion && (
        <div className={`mb-4 p-3 md:p-4 rounded-md ${isDarkMode ? 'bg-green-900/30' : 'bg-green-100'}`}>
          <div className="font-bold mb-1">👋 Welcome</div>
          <div>{greeting}</div>
          <div className="mt-3 text-sm font-semibold">
            All tools are selected by default. You may customize which tools to use for your question.
          </div>
        </div>
      )}

      {/* Show the current question in bold with light blue background */}
      {currentQuestion && (
        <div
          className={`font-bold mb-4 p-3 md:p-4 rounded-md ${isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'}`}
        >
          {currentQuestion}
        </div>
      )}

      {currentAnswer ? (
        <>
          <ReactMarkdown
            remarkPlugins={[remarkGfm, remarkBreaks]}
            rehypePlugins={[rehypeRaw, rehypeFormat]}
            className="markdown-content md:text-base space-y-4 leading-relaxed"
            components={{
              code({ node, inline, className, children, ...props }) {
                const match = /language-(\w+)/.exec(className || '');
                return !inline && match ? (
                  <SyntaxHighlighter
                    language={match[1]}
                    PreTag="div"
                    style={isDarkMode ? vscDarkPlus : undefined}
                    {...props}
                  >
                    {String(children).replace(/\n$/, '')}
                  </SyntaxHighlighter>
                ) : (
                  <code className={className} {...props}>
                    {children}
                  </code>
                );
              },
              
              // Custom link renderer with consistent styling
              a({ node, href, children, ...props }) {
                const isExternal = href && (href.startsWith('http://') || href.startsWith('https://'));
                return (
                  <Link href={href} external={isExternal} {...props}>
                    {children}
                  </Link>
                );
              },
            }}
          >
            {processedAnswer}
          </ReactMarkdown>

          {/* Show metadata below the current answer if available */}
          {currentMetadata && <MetadataDisplay metadata={currentMetadata} isDarkMode={isDarkMode} isResponseTile={true} />}
        </>
      ) : (
        <div>
          {selectedTools.length === 0 ? (
            <div className="text-amber-600 dark:text-amber-400">
              Please select at least one tool before submitting.
            </div>
          ) : (
            'Response will appear here...'
          )}
        </div>
      )}
    </div>
  );
};

export default React.memo(MessageDisplay);
