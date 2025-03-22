// src/App.jsx
import React, { useState, useEffect } from 'react';
import ChatPage from './pages/ChatPage';
import ThemeToggle from './components/ui/ThemeToggle';
import { Combine } from 'lucide-react';

const App = () => {
    const [isDarkMode, setIsDarkMode] = useState(
        localStorage.getItem('theme') === 'dark' ||
        window.matchMedia('(prefers-color-scheme: dark)').matches
    );

    useEffect(() => {
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
            <header className="px-6 py-4 border-b">
                <div className="max-w-4xl mx-auto flex justify-between items-center">
                    <div className="flex-1 flex justify-center">
                        <h1 className="text-xl font-semibold mr-auto flex items-center"><Combine className="mr-2" size={24} /> Hoover</h1>
                    </div>
                    <div className="flex-1 flex justify-center">
                        <div className="ml-auto">
                            <ThemeToggle isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
                        </div>
                    </div>
                </div>
            </header>
            <main>
                <ChatPage isDarkMode={isDarkMode} />
            </main>
        </div>
    );
};

export default App;