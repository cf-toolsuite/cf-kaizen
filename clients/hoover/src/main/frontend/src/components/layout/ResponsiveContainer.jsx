// components/layout/ResponsiveContainer.jsx
import React from 'react';

/**
 * A responsive container component that adapts to different screen sizes.
 * Uses Tailwind's responsive classes to adjust width and padding based on screen size.
 *
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child elements to render inside the container
 * @param {string} props.className - Additional CSS classes to apply
 * @returns {JSX.Element} - Rendered component
 */
const ResponsiveContainer = ({ children, className = '' }) => {
  return (
    <div className={`w-full px-2 xs:px-3 sm:px-4 md:px-6 lg:px-8 mx-auto
                     xs:max-w-full sm:max-w-full md:max-w-4xl lg:max-w-5xl xl:max-w-6xl
                     ${className}`}>
      {children}
    </div>
  );
};

export default ResponsiveContainer;
