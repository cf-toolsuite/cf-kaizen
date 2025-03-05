// src/App.jsx
import React, { useState, useEffect } from 'react';
import ChatPage from './pages/ChatPage';
import ThemeToggle from './components/ui/ThemeToggle';

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
            <header className="px-6 py-4 border-b flex justify-between items-center">
                <h1 className="text-xl font-semibold">Cloud Foundry Kaizen Hoover Frontend</h1>
                <ThemeToggle isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
            </header>
            <main>
                <ChatPage isDarkMode={isDarkMode} />
            </main>
        </div>
    );
};

export default App;