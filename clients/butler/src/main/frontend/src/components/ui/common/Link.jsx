import React from 'react';

/**
 * A standardized link component for consistent styling across the application.
 *
 * @param {string} href - The URL the link points to
 * @param {ReactNode} children - Content to display inside the link
 * @param {boolean} external - Whether the link is to an external site (adds icon and opens in new tab)
 * @param {string} className - Additional CSS classes to apply
 * @param {function} onClick - Click handler
 * @returns {JSX.Element} The styled link component
 */
const Link = ({ href, children, external = false, className = '', onClick, ...props }) => {
  // Base classes for links - uses Tailwind for consistent styling
  const baseClasses = "font-medium transition-colors hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded px-1";
  
  // External link handling - adds appropriate attributes for security and UX
  const externalProps = external 
    ? { target: "_blank", rel: "noopener noreferrer" } 
    : {};
  
  return (
    <a 
      href={href}
      className={`${baseClasses} text-blue-600 dark:text-blue-400 underline decoration-[0.05em] underline-offset-2 ${className}`}
      onClick={onClick}
      {...externalProps}
      {...props}
    >
      {children}
      {external && <span className="ml-1 text-xs">↗</span>}
    </a>
  );
};

export default Link;
