import React, { useState, useRef, useEffect, useMemo } from 'react';
import '@/components/ui/markdown-styles.css';
import { Alert, AlertDescription } from '@/components/ui/alert';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeFormat from 'rehype-format';
import remarkBreaks from 'remark-breaks';
import { processMarkdown } from '@/utils/markdownProcessor';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import {
    ChevronDown,
    ChevronUp,
    Clock,
    Target,
    PhoneIncoming,
    PhoneOutgoing,
    Sigma,
    Gauge,
    SidebarClose,
    SidebarOpen,
    Wrench,
    Info,
} from 'lucide-react';

const ChatPage = ({ isDarkMode }) => {
    const [question, setQuestion] = useState('');
    const [answer, setAnswer] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [alert, setAlert] = useState({ show: false, message: '' });
    const answerContainerRef = useRef(null);
    const chatContainerRef = useRef(null);
    const historyButtonRef = useRef(null);
    const tooltipRef = useRef(null);
    const [chatHistory, setChatHistory] = useState([]);
    const [currentAnswer, setCurrentAnswer] = useState(''); // State to track the current streamed answer
    const processedAnswer = useMemo(() => processMarkdown(currentAnswer), [currentAnswer]);
    const [currentQuestion, setCurrentQuestion] = useState(''); // To display the current question
    const [currentMetadata, setCurrentMetadata] = useState(null); // To store current response metadata
    const [showHistory, setShowHistory] = useState(false); // Control visibility of chat history
    const [greeting, setGreeting] = useState(''); // To store greeting message
    const [availableTools, setAvailableTools] = useState({}); // To store available tools
    const [selectedTools, setSelectedTools] = useState([]); // To store selected tools
    const [showToolsMenu, setShowToolsMenu] = useState(false); // Control visibility of tools dropdown
    const [tooltipText, setTooltipText] = useState(''); // To store the tooltip content
    const [showTooltip, setShowTooltip] = useState(false); // To control tooltip visibility
    const [hoveredToolKey, setHoveredToolKey] = useState(null); // Track which tool is being hovered
    const [tooltipAnchor, setTooltipAnchor] = useState(null); // Store the element to anchor the tooltip to.

    const getHistoryItemColor = () => {
        return isDarkMode ? 'bg-orange-600' : 'bg-orange-500';
    };

    const getHistoryItemTitleColor = () => {
        return isDarkMode ? 'bg-orange-700' : 'bg-orange-600';
    };

    // Fetch available tools and process them to remove prefixes
    useEffect(() => {
        const fetchTools = async () => {
            try {
                const response = await fetch('/api/butler/tools');
                if (response.ok) {
                    const toolsData = await response.json();

                    // Process tool names to remove any prefixes
                    const cleanedTools = {};
                    Object.entries(toolsData).forEach(([name, description]) => {
                        // Extract just the last part of the tool name if it contains a prefix
                        const cleanName = name.includes('_') ? name.split('_').pop() : name;
                        cleanedTools[name] = {
                            displayName: cleanName,
                            description: description,
                        };
                    });

                    setAvailableTools(cleanedTools);

                    // Select all tools by default
                    setSelectedTools(Object.keys(cleanedTools));
                }
            } catch (error) {
                console.error('Error fetching tools:', error);
            }
        };

        fetchTools();
    }, []);

    const handleToolSelect = (toolName) => {
        setSelectedTools((prev) => {
            // If tool is already selected, remove it, otherwise add it
            if (prev.includes(toolName)) {
                return prev.filter((tool) => tool !== toolName);
            } else {
                return [...prev, toolName];
            }
        });
    };

    const handleSelectAll = (e) => {
        e.stopPropagation();
        setSelectedTools(Object.keys(availableTools));
    };

    const handleClearAll = (e) => {
        e.stopPropagation();
        setSelectedTools([]);
    };

    const handleTooltipMouseEnter = (key, description, event) => {
        setTooltipText(description);
        setHoveredToolKey(key);
        setShowTooltip(true);
        setTooltipAnchor(event.currentTarget); // Set the anchor element
    };

    const handleTooltipMouseLeave = () => {
        setShowTooltip(false);
        setHoveredToolKey(null);
        setTooltipAnchor(null);
    };

    const handleSubmit = async (e) => {
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
        setCurrentQuestion(questionText); // Set the current question to display
        setQuestion('');
        setAnswer('');
        setCurrentAnswer(''); // Reset the current answer state
        setCurrentMetadata(null); // Reset metadata

        try {
            const response = await fetch('/api/butler/stream/chat', {
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

            // Save to chat history AFTER streaming is complete
            // We use a callback form of setState to ensure we're working with the latest state
            setChatHistory((prev) => {
                // Add the new history item to the beginning of the array
                const newHistory = [
                    {
                        question: questionText,
                        answer: '🤖 ' + fullAnswer, // Make sure we include the robot emoji
                        expanded: false,
                        // Use question index for numbering purposes
                        questionNumber: prev.length > 0 ? prev[0].questionNumber + 1 : 1,
                        color: getHistoryItemColor(),
                        titleColor: getHistoryItemTitleColor(),
                        metadata: latestMetadata, // Use the captured metadata
                        tools: selectedTools.length > 0 ? [...selectedTools] : [], // Save selected tools in history
                    },
                    ...prev,
                ];

                // If we have more than 10 items, remove the last one
                if (newHistory.length > 10) {
                    return newHistory.slice(0, 10);
                }
                return newHistory;
            });
        } catch (error) {
            console.error('Error processing chat request:', error);
            showAlert('Error processing chat request');
        } finally {
            setIsLoading(false);
        }
    };

    const showAlert = (message) => {
        setAlert({ show: true, message });
        setTimeout(() => setAlert({ show: false, message: '' }), 5000);
    };

    // Modified to ensure only one history item can be expanded at a time
    const toggleHistoryItem = (index) => {
        setChatHistory((prev) => {
            // Create a new array with all items collapsed first
            const updatedHistory = prev.map((item) => ({
                ...item,
                expanded: false,
            }));

            // Only toggle the expansion state of the clicked item
            // If it was already expanded (which is now false after the map above),
            // it will remain collapsed. Otherwise, it will be expanded.
            updatedHistory[index] = {
                ...updatedHistory[index],
                expanded: !prev[index].expanded,
            };

            return updatedHistory;
        });
    };

    // Render metadata as a formatted string
    const renderMetadata = (metadata) => {
        if (!metadata) return null;

        return (
            <div className={`mt-2 text-xs ${isDarkMode ? 'text-gray-400' : 'text-gray-500'}`}>
                <div className="flex flex-wrap gap-x-4">
                    {metadata.model && (
                        <span className="flex items-center gap-1">
              <Target size={12} /> {metadata.model}
            </span>
                    )}
                    {metadata.responseTime && (
                        <span className="flex items-center gap-1">
              <Clock size={12} /> {metadata.responseTime}
            </span>
                    )}
                    {metadata.inputTokens && (
                        <span className="flex items-center gap-1">
              <PhoneIncoming size={12} /> {metadata.inputTokens}
            </span>
                    )}
                    {metadata.outputTokens && (
                        <span className="flex items-center gap-1">
              <PhoneOutgoing size={12} /> {metadata.outputTokens}
            </span>
                    )}
                    {metadata.totalTokens && (
                        <span className="flex items-center gap-1">
              <Sigma size={12} /> {metadata.totalTokens}
            </span>
                    )}
                    {metadata.tokensPerSecond && (
                        <span className="flex items-center gap-1">
              <Gauge size={12} /> {metadata.tokensPerSecond} t/s
            </span>
                    )}
                </div>
            </div>
        );
    };

    // Tool selection component with tooltips
    const ToolSelector = () => {
        // Check if all tools are selected
        const allToolsSelected =
            Object.keys(availableTools).length > 0 &&
            Object.keys(availableTools).length === selectedTools.length;

        // Check if no tools are selected
        const noToolsSelected = selectedTools.length === 0;

        return (
            <div className="relative tools-menu-container h-full">
                <button
                    onClick={() => setShowToolsMenu(!showToolsMenu)}
                    className={`flex items-center justify-center gap-1 px-4 py-2 h-full rounded text-sm ${
                        isDarkMode
                            ? 'bg-gray-700 text-white hover:bg-gray-600'
                            : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                    } ${selectedTools.length > 0 ? 'border-2 border-blue-500' : ''}`}
                    title={`${selectedTools.length} tools selected`}
                >
                    <Wrench size={16} />
                    <span>Tools{selectedTools.length > 0 ? ` (${selectedTools.length})` : ''}</span>
                    <ChevronDown size={14} />
                </button>

                {showToolsMenu && (
                    <div
                        className={`absolute left-0 mt-1 rounded-md shadow-lg z-10 ${
                            isDarkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'
                        } border max-h-64 overflow-y-auto`}
                        style={{ width: (historyButtonRef?.current?.offsetWidth || 150) * 5 }} // Quintuple the width
                    >
                        {/* Fixed header */}
                        <div
                            className="sticky top-0 p-2 border-b font-semibold flex justify-between items-center bg-inherit z-10"
                        >
                            <span>Available Tools</span>
                            <div className="flex gap-3">
                                <button
                                    onClick={handleSelectAll}
                                    className={`text-xs text-blue-500 hover:text-blue-700 ${
                                        allToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                                    }`}
                                    disabled={allToolsSelected}
                                >
                                    Select All
                                </button>
                                <button
                                    onClick={handleClearAll}
                                    className={`text-xs text-blue-500 hover:text-blue-700 ${
                                        noToolsSelected ? 'opacity-50 cursor-not-allowed' : ''
                                    }`}
                                    disabled={noToolsSelected}
                                >
                                    Clear All
                                </button>
                            </div>
                        </div>

                        {/* Scrollable content */}
                        <div className="p-2">
                            {Object.entries(availableTools)
                                .sort((a, b) => a[1].displayName.localeCompare(b[1].displayName))
                                .map(([key, value]) => (
                                    <div key={key} className="flex items-start mb-2 relative">
                                        <input
                                            type="checkbox"
                                            id={`tool-${key}`}
                                            checked={selectedTools.includes(key)}
                                            onChange={() => handleToolSelect(key)}
                                            className={`mr-2 mt-1 ${isDarkMode ? 'bg-gray-700' : 'bg-white'}`}
                                        />
                                        <div className="flex-grow">
                                            <label
                                                htmlFor={`tool-${key}`}
                                                className="font-medium cursor-pointer flex items-center group relative"  // Make label relative for tooltip positioning
                                            >
                        <span
                            className="hover:underline"
                            onMouseEnter={(e) => handleTooltipMouseEnter(key, value.description, e)}
                            onMouseLeave={handleTooltipMouseLeave}
                        >
                          {value.displayName}
                        </span>
                                                <span
                                                    className="ml-1 text-gray-400 hover:text-gray-600 cursor-help tooltip-icon"
                                                    onMouseEnter={(e) => handleTooltipMouseEnter(key, value.description, e)}
                                                    onMouseLeave={handleTooltipMouseLeave}
                                                >
                          <Info size={14} />
                        </span>
                                            </label>
                                        </div>
                                    </div>
                                ))}
                        </div>
                    </div>
                )}

                {/* Tooltip positioned relative to the anchor element */}
                {showTooltip && tooltipText && tooltipAnchor && (
                    <div
                        ref={tooltipRef}
                        className={`fixed p-3 rounded-md shadow-lg z-20 max-w-xs ${
                            isDarkMode ? 'bg-gray-700 text-white' : 'bg-white text-gray-900'
                        } border`}
                        style={{
                            maxHeight: '30vh',
                            overflow: 'auto',
                            top: `${tooltipAnchor.getBoundingClientRect().bottom + window.scrollY + 5}px`, // Position below the anchor
                            left: `${tooltipAnchor.getBoundingClientRect().left + window.scrollX}px`, // Align with the left edge of the anchor
                        }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        {tooltipText}
                    </div>
                )}
            </div>
        );
    };

    // Modified to auto-scroll to bottom when new content is added
    useEffect(() => {
        if (answerContainerRef.current) {
            answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
        }
    }, [answer]);

    // Fetch greeting message when component mounts
    useEffect(() => {
        const fetchGreeting = async () => {
            try {
                const response = await fetch('/api/butler/greeting');
                if (response.ok) {
                    const greetingText = await response.text();
                    setGreeting(greetingText);
                }
            } catch (error) {
                console.error('Error fetching greeting:', error);
            }
        };

        fetchGreeting();
    }, []);

    // Close the tools menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            // If the tools menu is open and the click is outside, close it
            if (showToolsMenu && !event.target.closest('.tools-menu-container') && !event.target.closest('.tooltip-container') && !event.target.closest('.tooltip-icon')) {
                setShowToolsMenu(false);
                setShowTooltip(false);
            } else if (showTooltip && !event.target.closest('.tooltip-container') && !event.target.closest('.tooltip-icon')) {
                setShowTooltip(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showToolsMenu, showTooltip]); // Include showTooltip in the dependency array

    // Check if submit should be disabled - recalculate on each render
    const isSubmitDisabled = isLoading || !question.trim() || selectedTools.length === 0;

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
                    {alert.show && (
                        <Alert className="mb-4 bg-red-100">
                            <AlertDescription>{alert.message}</AlertDescription>
                        </Alert>
                    )}

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
                                {currentMetadata && renderMetadata(currentMetadata)}
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

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
              <textarea
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  placeholder="Enter your question..."
                  className={`w-full p-2 border rounded-md h-32 ${
                      isDarkMode
                          ? 'bg-gray-700 border-gray-600 text-white placeholder-gray-400'
                          : 'bg-white border-gray-300 text-gray-900'
                  }`}
                  disabled={isLoading}
              />
                        </div>

                        <div className="flex items-stretch">
                            <button
                                type="submit"
                                className={`px-4 py-2 rounded ${
                                    isDarkMode
                                        ? 'bg-blue-600 hover:bg-blue-700 text-white disabled:bg-gray-600'
                                        : 'bg-blue-500 hover:bg-blue-600 text-white disabled:bg-gray-400'
                                } ${isSubmitDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
                                disabled={isSubmitDisabled}
                                title={selectedTools.length === 0 ? 'Please select at least one tool' : ''}
                            >
                                {isLoading ? 'Processing...' : 'Submit'}
                            </button>

                            {/* Tool selector button */}
                            <div className="ml-2 flex-grow" style={{ maxWidth: '150px' }}>
                                <ToolSelector />
                            </div>
                        </div>
                    </form>
                </div>
                {showHistory && (
                    <div className="w-1/3 flex flex-col justify-end">
                        <div className="flex flex-col-reverse mt-auto">
                            {' '}
                            {/* Reverse column for stacking effect */}
                            {chatHistory.map((item, index) => (
                                <div
                                    key={index}
                                    className={`mb-2 border rounded p-2 ${item.color} ${
                                        isDarkMode ? 'text-white' : 'text-gray-900'
                                    }`}
                                >
                                    <div
                                        className="flex items-center justify-between cursor-pointer"
                                        onClick={() => toggleHistoryItem(index)}
                                    >
                    <span
                        className={`font-semibold truncate pr-2 py-1 px-2 rounded ${item.titleColor || ''}`}
                    >
                      {item.question}
                    </span>
                                        {item.expanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                    </div>

                                    {/* Show a condensed version of metadata in collapsed state */}
                                    {!item.expanded && item.metadata && (
                                        <div className={`text-xs mt-1 ${isDarkMode ? 'text-gray-300' : 'text-gray-600'}`}>
                                            <div className="flex gap-x-3">
                                                {item.metadata.responseTime && (
                                                    <span className="flex items-center gap-1">
                            <Clock size={10} /> {item.metadata.responseTime}
                          </span>
                                                )}
                                                {item.metadata.totalTokens && (
                                                    <span className="flex items-center gap-1">
                            <Sigma size={10} /> {item.metadata.totalTokens}
                          </span>
                                                )}
                                                {item.metadata.tokensPerSecond && (
                                                    <span className="flex items-center gap-1">
                            <Gauge size={10} /> {item.metadata.tokensPerSecond} t/s
                          </span>
                                                )}
                                            </div>
                                        </div>
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
                                            <div
                                                className="max-h-64 overflow-y-auto pr-1"
                                            >
                                                {' '}
                                                {/* Added fixed height with scrolling */}
                                                <ReactMarkdown
                                                    className={`prose markdown-content ${isDarkMode ? 'prose-invert' : ''}`}
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
                                                {item.metadata && renderMetadata(item.metadata)}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ChatPage;