// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    darkMode: 'class',
    theme: {
        extend: {
            screens: {
                'xs': '320px',  // Extra small screens (phones)
                // Tailwind defaults below
                'sm': '640px',  // Small screens (large phones, small tablets)
                'md': '768px',  // Medium screens (tablets)
                'lg': '1024px', // Large screens (desktops)
                'xl': '1280px', // Extra large screens
                '2xl': '1536px' // 2X large screens
            },
        },
    },
    plugins: [],
}
