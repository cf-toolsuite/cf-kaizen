import React, { useState, useRef, useEffect } from 'react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { ChevronDown, ChevronUp } from 'lucide-react';

const ChatPage = ({ isDarkMode }) => {
    const [question, setQuestion] = useState('');
    const [answer, setAnswer] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [alert, setAlert] = useState({ show: false, message: '' });
    const answerContainerRef = useRef(null);
    const [chatHistory, setChatHistory] = useState([]);
    const [currentAnswer, setCurrentAnswer] = useState(''); // State to track the current streamed answer
    const [currentQuestion, setCurrentQuestion] = useState(''); // To display the current question

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

        try {
            const response = await fetch('/api/hoover/stream/chat', {
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

            // Start with the robot emoji
            setAnswer('🤖 ');
            setCurrentAnswer('🤖 ');

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value);
                fullAnswer += chunk;
                setCurrentAnswer((prev) => prev + chunk);
                setAnswer((prev) => prev + chunk);

                if (answerContainerRef.current) {
                    answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
                }
            }

            // Make sure we include the robot emoji in the stored answer
            const formattedAnswer = '🤖 ' + fullAnswer;
            setChatHistory(prev => {
                // Add the new history item to the beginning of the array
                const newHistory = [{
                    question: questionText,
                    answer: formattedAnswer,
                    expanded: false,
                    // Use question index for numbering purposes
                    questionNumber: prev.length > 0 ? prev[0].questionNumber + 1 : 1,
                    color: getHistoryItemColor()
                }, ...prev];

                // If we have more than 10 items, remove the last one
                if (newHistory.length > 10) {
                    newHistory.pop();
                }
                return newHistory;
            });
        } catch (error) {
            showAlert('Error processing chat request');
        } finally {
            setIsLoading(false);
        }
    };

    const showAlert = (message) => {
        setAlert({ show: true, message });
        setTimeout(() => setAlert({ show: false, message: '' }), 3000);
    };

    const toggleHistoryItem = (index) => {
        setChatHistory(prev => prev.map((item, i) => {
            if (i === index) {
                return { ...item, expanded: !item.expanded };
            }
            return item;
        }));
    };

    // Modified to auto-scroll to bottom when new content is added
    useEffect(() => {
        if(answerContainerRef.current) {
            answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
        }
    },[answer]);

    return (
        <div className="max-w-4xl mx-auto p-6 flex flex-col">
            <div className="flex">
                <div className="w-2/3 pr-4">
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
                        {/* Show the current question in bold with light blue background */}
                        {currentQuestion && (
                            <div className={`font-bold mb-4 p-3 rounded-md ${
                                isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'
                            }`}>
                                {currentQuestion}
                            </div>
                        )}

                        {currentAnswer ? (
                            <ReactMarkdown
                                remarkPlugins={[remarkGfm]}
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
                                {currentAnswer}
                            </ReactMarkdown>
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
                <div className="w-1/3 flex flex-col justify-end"> {/* Changed from w-1/4 to w-1/3 */}
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
                                    {item.expanded ? <ChevronUp /> : <ChevronDown />}
                                </div>
                                {item.expanded && (
                                    <div className="mt-2">
                                        <div className="font-bold">Asked:</div>
                                        <div className={`p-2 mt-1 mb-3 rounded-md ${
                                            isDarkMode ? 'bg-blue-900/30' : 'bg-blue-100'
                                        }`}>{item.question}</div>
                                        <div className="font-bold mt-2">Response:</div>
                                        <div className="max-h-64 overflow-y-auto pr-1"> {/* Added fixed height with scrolling */}
                                            <ReactMarkdown
                                                className={`prose ${isDarkMode ? 'prose-invert' : ''}`}
                                                remarkPlugins={[remarkGfm]}
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
                                                {item.answer}
                                            </ReactMarkdown>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ChatPage;