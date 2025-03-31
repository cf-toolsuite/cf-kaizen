// src/components/ui/ThemeToggle.jsx
import React from 'react';
import { Sun, Moon } from 'lucide-react';

// Keep TouchFriendlyButton for mobile but use conditional rendering
const TouchFriendlyButton = React.lazy(() => import('./common/TouchFriendlyButton'));

const ThemeToggle = ({ isDarkMode, toggleTheme }) => {
    const [isMobile, setIsMobile] = React.useState(false);
    
    React.useEffect(() => {
        // Check if the current device is mobile
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };
        
        // Initial check
        checkMobile();
        
        // Add event listener for window resize
        window.addEventListener('resize', checkMobile);
        
        // Cleanup
        return () => window.removeEventListener('resize', checkMobile);
    }, []);
    
    // Regular toggle button for desktop
    const DesktopToggle = () => (
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
    );
    
    // Mobile toggle using TouchFriendlyButton
    const MobileToggle = () => (
        <React.Suspense fallback={<DesktopToggle />}>
            <TouchFriendlyButton
                onClick={toggleTheme}
                className={`relative inline-flex h-10 w-14 items-center justify-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 ${
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
            </TouchFriendlyButton>
        </React.Suspense>
    );

    return (
        <div className="flex items-center">
            {isMobile ? <MobileToggle /> : <DesktopToggle />}
        </div>
    );
};

export default ThemeToggle;
