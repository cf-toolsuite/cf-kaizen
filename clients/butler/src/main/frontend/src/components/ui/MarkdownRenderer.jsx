import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkBreaks from 'remark-breaks';
import rehypeRaw from 'rehype-raw';
import rehypeFormat from 'rehype-format';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { processMarkdown } from '@/utils/markdownProcessor';
import Link from './common/Link';

// Custom component for better markdown rendering with optimal spacing
const MarkdownRenderer = ({ content, isDarkMode, className = '' }) => {
  // Process the markdown content first
  const processedContent = processMarkdown(content);
  
  return (
    <ReactMarkdown
      remarkPlugins={[remarkGfm, remarkBreaks]}
      rehypePlugins={[rehypeRaw, rehypeFormat]}
      className={`markdown-content ${className}`}
      components={{
        // Enhance paragraph spacing for better readability
        p: ({ node, children, ...props }) => {
          // Detect if this is a list of key-value pairs based on content
          const isKeyValueList = 
            String(children).includes('**') && 
            String(children).includes(':') && 
            !String(children).match(/[.!?]$/);
            
          return (
            <p 
              className={isKeyValueList ? 'key-value-item' : ''} 
              style={{ marginBottom: isKeyValueList ? '0.15rem' : '0.5rem' }}
              {...props}
            >
              {children}
            </p>
          );
        },
        
        // Add custom spacing for headings
        h1: ({ node, ...props }) => <h1 style={{ marginTop: '1.2rem', marginBottom: '0.8rem' }} {...props} />,
        h2: ({ node, ...props }) => <h2 style={{ marginTop: '1rem', marginBottom: '0.6rem' }} {...props} />,
        h3: ({ node, ...props }) => <h3 style={{ marginTop: '0.8rem', marginBottom: '0.5rem' }} {...props} />,
        
        // Custom list items renderer
        li: ({ node, children, ordered, ...props }) => {
          // Check if this is an application entry or a detail item
          const isApplicationEntry = String(children).includes('Application Name');
          const isDetailItem = String(children).includes('**') && String(children).includes(':');
          
          return (
            <li 
              style={{ 
                marginBottom: isApplicationEntry ? '0.6rem' : (isDetailItem ? '0.1rem' : '0.2rem')
              }}
              {...props}
            >
              {children}
            </li>
          );
        },
        
        // Code block handling with syntax highlighting
        code: ({ node, inline, className, children, ...props }) => {
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
        a: ({ node, href, children, ...props }) => {
          const isExternal = href && (href.startsWith('http://') || href.startsWith('https://'));
          return (
            <Link href={href} external={isExternal} {...props}>
              {children}
            </Link>
          );
        }
      }}
    >
      {processedContent}
    </ReactMarkdown>
  );
};

export default MarkdownRenderer;
