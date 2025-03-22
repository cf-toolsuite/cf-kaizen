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
    SidebarOpen
} from 'lucide-react';

const ChatPage = ({ isDarkMode }) => {
    const [question, setQuestion] = useState('');
    const [answer, setAnswer] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [alert, setAlert] = useState({ show: false, message: '' });
    const answerContainerRef = useRef(null);
    const [chatHistory, setChatHistory] = useState([]);
    const [currentAnswer, setCurrentAnswer] = useState(''); // State to track the current streamed answer
    const processedAnswer = useMemo(() => processMarkdown(currentAnswer), [currentAnswer]);
    const [currentQuestion, setCurrentQuestion] = useState(''); // To display the current question
    const [currentMetadata, setCurrentMetadata] = useState(null); // To store current response metadata
    const [showHistory, setShowHistory] = useState(false); // Control visibility of chat history
    const [greeting, setGreeting] = useState(''); // To store greeting message

    const getHistoryItemColor = () => {
        return isDarkMode ? 'bg-orange-600' : 'bg-orange-500';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!question.trim()) {
            showAlert('Please enter a question');
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
                    question: questionText
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
                setCurrentAnswer(prev => prev + chunk);
                setAnswer(prev => prev + chunk);

                if (answerContainerRef.current) {
                    answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
                }
            }

            // Save to chat history AFTER streaming is complete
            // We use a callback form of setState to ensure we're working with the latest state
            setChatHistory(prev => {
                // Add the new history item to the beginning of the array
                const newHistory = [{
                    question: questionText,
                    answer: '🤖 ' + fullAnswer, // Make sure we include the robot emoji
                    expanded: false,
                    // Use question index for numbering purposes
                    questionNumber: prev.length > 0 ? prev[0].questionNumber + 1 : 1,
                    color: getHistoryItemColor(),
                    metadata: latestMetadata // Use the captured metadata
                }, ...prev];

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

    const toggleHistoryItem = (index) => {
        setChatHistory(prev => {
            // Create a new array to avoid mutation issues
            return prev.map((item, i) => {
                if (i === index) {
                    return { ...item, expanded: !item.expanded };
                }
                return item;
            });
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

    // Modified to auto-scroll to bottom when new content is added
    useEffect(() => {
        if(answerContainerRef.current) {
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

    return (
        <div className="max-w-4xl mx-auto p-6 flex flex-col">
            {/* History visibility toggle button */}
            <div className="flex justify-end mb-2">
                <button
                    onClick={() => setShowHistory(!showHistory)}
                    className={`flex items-center gap-1 px-3 py-1 rounded text-sm ${isDarkMode ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-gray-200 text-gray-800 hover:bg-gray-300'}`}
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
                <div className={showHistory ? "w-2/3 pr-4" : "w-full"}>
                    {alert.show && (
                        <Alert className="mb-4 bg-red-100">
                            <AlertDescription>{alert.message}</AlertDescription>
                        </Alert>
                    )}

                    <div
                        ref={answerContainerRef}
                        className={`p-4 rounded-lg mb-4 h-96 overflow-y-auto relative ${
                            isDarkMode
                                ? 'bg-gray-800 prose-invert max-w-none'
                                : 'bg-gray-50 prose-slate max-w-none'
                        }`}
                    >
                        {/* Greeting message shown at the top */}
                        {greeting && !currentQuestion && (
                            <div className={`mb-4 p-3 rounded-md ${isDarkMode ? 'bg-green-900/30' : 'bg-green-100'}`}>
                                <div className="font-bold mb-1">👋 Welcome</div>
                                <div>{greeting}</div>
                            </div>
                        )}
                        
                        {/* Show the current question in bold with light blue background */}
                        {currentQuestion && (
                            <div className={`font-bold mb-4 p-3 rounded-md ${
                                isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'
                            }`}>
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
                                        code({node, inline, className, children, ...props}) {
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
                                        }
                                    }}
                                >
                                    {processedAnswer}
                                </ReactMarkdown>

                                {/* Show metadata below the current answer if available */}
                                {currentMetadata && renderMetadata(currentMetadata)}
                            </>
                        ) : (
                            'Response will appear here...'
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

                        <button
                            type="submit"
                            className={`px-4 py-2 rounded ${
                                isDarkMode
                                    ? 'bg-blue-600 hover:bg-blue-700 text-white disabled:bg-gray-600'
                                    : 'bg-blue-500 hover:bg-blue-600 text-white disabled:bg-gray-400'
                            }`}
                            disabled={isLoading}
                        >
                            {isLoading ? 'Processing...' : 'Submit'}
                        </button>
                    </form>
                </div>
                {showHistory && <div className="w-1/3 flex flex-col justify-end">
                    <div className="flex flex-col-reverse mt-auto"> {/* Reverse column for stacking effect */}
                        {chatHistory.map((item, index) => (
                            <div
                                key={index}
                                className={`mb-2 border rounded p-2 ${item.color} ${
                                    isDarkMode ? 'text-white' : 'text-gray-900'
                                }`}
                            >
                                <div className="flex items-center justify-between cursor-pointer" onClick={() => toggleHistoryItem(index)}>
                                    <span className="font-semibold">Question {item.questionNumber}</span>
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

                                {item.expanded && (
                                    <div className="mt-2">
                                        <div className="font-bold">Asked:</div>
                                        <div className={`p-2 mt-1 mb-3 rounded-md ${
                                            isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'
                                        }`}>{item.question}</div>
                                        <div className="font-bold mt-2">Response:</div>
                                        <div className="max-h-64 overflow-y-auto pr-1"> {/* Added fixed height with scrolling */}
                                            <ReactMarkdown
                                                className={`prose markdown-content ${isDarkMode ? 'prose-invert' : ''}`}
                                                remarkPlugins={[remarkGfm, remarkBreaks]}
                                                rehypePlugins={[rehypeRaw, rehypeFormat]}
                                                components={{
                                                    code({node, inline, className, children, ...props}) {
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
                                                    }
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
                </div>}
            </div>
        </div>
    );
};

export default ChatPage;