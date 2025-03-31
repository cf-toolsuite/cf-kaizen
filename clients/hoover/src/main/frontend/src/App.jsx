// src/App.jsx
import React, { useState, useEffect, lazy, Suspense } from 'react';
import ThemeToggle from './components/ui/ThemeToggle';
import { Combine } from 'lucide-react';
import ResponsiveContainer from './components/layout/ResponsiveContainer';
import { isMobileDevice, preconnect } from './utils/performance';

// Lazy load the ChatPage component for better performance
const ChatPage = lazy(() => import('./pages/ChatPage'));

const App = () => {
    const [isDarkMode, setIsDarkMode] = useState(
        localStorage.getItem('theme') === 'dark' ||
        window.matchMedia('(prefers-color-scheme: dark)').matches
    );
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        // Check if the device is mobile
        setIsMobile(isMobileDevice());
        
        // Preconnect to important domains
        preconnect(['https://fonts.googleapis.com']);
        
        // Apply theme when component mounts and when theme changes
        if (isDarkMode) {
            document.documentElement.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        } else {
            document.documentElement.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        }
    }, [isDarkMode]);

    const toggleTheme = () => {
        setIsDarkMode(!isDarkMode);
    };

    return (
        <div className={`min-h-screen transition-colors duration-200 ${isDarkMode ? 'dark bg-gray-900 text-white' : 'bg-white text-gray-900'}`}>
            <header className="py-4 border-b">
                <ResponsiveContainer>
                    <div className="flex justify-between items-center">
                    <div className="flex items-center">
                        <h1 className="text-xl font-semibold flex items-center">
                            <Combine className="mr-2" size={24} /> Hoover
                        </h1>
                    </div>
                    <div className="flex items-center">
                        <ThemeToggle isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
                    </div>
                </div>
                </ResponsiveContainer>
            </header>
            <main>
                <Suspense fallback={<div className="p-8 text-center">Loading application...</div>}>
                    <ChatPage isDarkMode={isDarkMode} />
                </Suspense>
            </main>
        </div>
    );
};

export default App;