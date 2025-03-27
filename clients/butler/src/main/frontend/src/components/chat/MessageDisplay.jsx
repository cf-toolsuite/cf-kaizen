import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeFormat from 'rehype-format';
import remarkBreaks from 'remark-breaks';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import MetadataDisplay from './MetadataDisplay';
import { useChat } from '../../context/ChatContext';

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

  return (
    <div
      ref={answerContainerRef}
      className={`p-4 rounded-lg mb-4 h-96 overflow-y-auto relative ${
        isDarkMode ? 'bg-gray-800 prose-invert max-w-none' : 'bg-gray-50 prose-slate max-w-none'
      }`}
    >
      {/* Greeting message shown at the top */}
      {greeting && !currentQuestion && (
        <div className={`mb-4 p-3 rounded-md ${isDarkMode ? 'bg-green-900/30' : 'bg-green-100'}`}>
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
          className={`font-bold mb-4 p-3 rounded-md ${isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'}`}
        >
          {currentQuestion}
        </div>
      )}

      {currentAnswer ? (
        <>
          <ReactMarkdown
            remarkPlugins={[remarkGfm, remarkBreaks]}
            rehypePlugins={[rehypeRaw, rehypeFormat]}
            className="markdown-content"
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
