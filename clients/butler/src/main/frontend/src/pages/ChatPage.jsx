import React, { useState, useRef } from 'react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';

const ChatPage = ({ isDarkMode }) => {
    const [question, setQuestion] = useState('');
    const [answer, setAnswer] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [alert, setAlert] = useState({ show: false, message: '' });
    const answerContainerRef = useRef(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!question.trim()) {
            showAlert('Please enter a question');
            return;
        }

        setIsLoading(true);
        setAnswer('');

        try {
            const response = await fetch('/api/butler/stream/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    question: question
                }),
            });

            if (!response.ok) throw new Error('Chat request failed');

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value);
                setAnswer(prev => prev + chunk);

                if (answerContainerRef.current) {
                    answerContainerRef.current.scrollTop = answerContainerRef.current.scrollHeight;
                }
            }
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

    return (
        <div className="max-w-4xl mx-auto p-6">
            <h1 className="text-2xl font-bold mb-6">Chat</h1>

            {alert.show && (
                <Alert className="mb-4 bg-red-100">
                    <AlertDescription>{alert.message}</AlertDescription>
                </Alert>
            )}

            <div
                ref={answerContainerRef}
                className={`p-4 rounded-lg mb-4 h-96 overflow-y-auto prose ${
                    isDarkMode
                        ? 'bg-gray-800 prose-invert max-w-none'
                        : 'bg-gray-50 prose-slate max-w-none'
                }`}
            >
                {answer ? (
                    <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={{
                            code({node, inline, className, children, ...props}) {
                                const match = /language-(\w+)/.exec(className || '');
                                return !inline && match ? (
                                    <SyntaxHighlighter
                                        language={match[1]}
                                        PreTag="div"
                                        style={isDarkMode ? vscDarkPlus : vs}
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
                        {answer}
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
    );
};

export default ChatPage;
