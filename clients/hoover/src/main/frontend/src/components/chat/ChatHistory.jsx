import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeFormat from 'rehype-format';
import remarkBreaks from 'remark-breaks';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { ChevronDown, ChevronUp, Wrench } from 'lucide-react';
import { processMarkdown } from '@/utils/markdownProcessor';
import MetadataDisplay from './MetadataDisplay';
import { useChat } from '../../context/ChatContext';

const ChatHistory = () => {
  const {
    chatHistory,
    toggleHistoryItem,
    isDarkMode
  } = useChat();

  if (chatHistory.length === 0) {
    return (
      <div className={`text-center p-4 ${isDarkMode ? 'text-gray-400' : 'text-gray-500'}`}>
        No chat history yet
      </div>
    );
  }

  return (
    <div className="flex flex-col-reverse mt-auto">
      {chatHistory.map((item, index) => (
        <div
          key={index}
          className={`mb-2 border rounded p-2 ${isDarkMode ? item.color : 'bg-gray-50'} ${
            isDarkMode ? 'text-gray-100' : 'text-gray-900'
          } shadow-sm`}
        >
          <div
            className="flex items-center justify-between cursor-pointer py-1 px-1 rounded hover:bg-opacity-20 hover:bg-gray-200 transition-colors"
            onClick={() => toggleHistoryItem(index)}
          >
            <span
              className={`font-semibold truncate pr-2 py-1 px-2 rounded ${isDarkMode ? item.titleColor : 'bg-blue-100'} ${isDarkMode ? 'text-teal-50' : 'text-blue-800'}`}
            >
              {item.question}
            </span>
            <div className="flex items-center justify-center w-6 h-6 min-w-6 min-h-6">
              {item.expanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            </div>
          </div>

          {/* Show a condensed version of metadata in collapsed state */}
          {!item.expanded && item.metadata && (
            <MetadataDisplay 
              metadata={item.metadata} 
              isDarkMode={isDarkMode} 
              condensed={true} 
            />
          )}

          {/* Show selected tools in the history item */}
          {!item.expanded && item.tools && item.tools.length > 0 && (
            <div className={`text-xs mt-1 ${isDarkMode ? 'text-gray-300' : 'text-gray-600'}`}>
              <div className="flex items-center gap-1">
                <Wrench size={10} />
                <span>{item.tools.length} tool{item.tools.length > 1 ? 's were' : ' was'} active</span>
              </div>
            </div>
          )}

          {item.expanded && (
            <div className="mt-2">
              <div className="font-bold mt-2">Response:</div>
              <div className="max-h-64 overflow-y-auto pr-1">
                <ReactMarkdown
                  className={`prose markdown-content ${isDarkMode ? 'prose-invert prose-a:text-blue-300' : 'prose-a:text-blue-600'}`}
                  remarkPlugins={[remarkGfm, remarkBreaks]}
                  rehypePlugins={[rehypeRaw, rehypeFormat]}
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
                  {processMarkdown(item.answer)}
                </ReactMarkdown>

                {/* Show metadata in expanded history items */}
                {item.metadata && <MetadataDisplay metadata={item.metadata} isDarkMode={isDarkMode} />}
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

export default React.memo(ChatHistory);
