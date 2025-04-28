// src/components/ui/ThemeToggle.jsx
import React from 'react';
import { Sun, Moon } from 'lucide-react';

const ThemeToggle = ({ isDarkMode, toggleTheme }) => {
  return (
    <button
      onClick={toggleTheme}
      className={`relative inline-flex items-center justify-center rounded-full transition-colors focus:outline-none ${
        isDarkMode ? 'bg-blue-600' : 'bg-gray-200'
      }`}
      role="switch"
      aria-checked={isDarkMode}
      style={{
        touchAction: 'manipulation',
        minHeight: '40px',
        minWidth: '40px',
        padding: '10px'
      }}
    >
      {isDarkMode ? (
        <Moon className="h-5 w-5 text-white" />
      ) : (
        <Sun className="h-5 w-5 text-yellow-500" />
      )}
    </button>
  );
};

export default ThemeToggle;
