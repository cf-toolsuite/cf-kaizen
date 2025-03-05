// src/components/ui/ThemeToggle.jsx
import React from 'react';
import { Sun, Moon } from 'lucide-react';

const ThemeToggle = ({ isDarkMode, toggleTheme }) => {
    return (
        <div className="flex items-center">
            <button
                onClick={toggleTheme}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 ${
                    isDarkMode ? 'bg-blue-600' : 'bg-gray-200'
                }`}
                role="switch"
                aria-checked={isDarkMode}
            >
        <span
            className={`${
                isDarkMode ? 'translate-x-6' : 'translate-x-1'
            } inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ease-in-out`}
        >
          {isDarkMode ? (
              <Moon className="h-4 w-4 text-blue-800" />
          ) : (
              <Sun className="h-4 w-4 text-yellow-500" />
          )}
        </span>
            </button>
        </div>
    );
};

export default ThemeToggle;
