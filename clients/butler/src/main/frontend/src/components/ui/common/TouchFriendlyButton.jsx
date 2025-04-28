// components/ui/common/TouchFriendlyButton.jsx
import React from 'react';

/**
 * A button component with touch-friendly dimensions and behavior.
 * Ensures minimum touch target size and proper touch action handling.
 *
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child elements to render inside the button
 * @param {Function} props.onClick - Click handler function
 * @param {string} props.className - Additional CSS classes to apply
 * @returns {JSX.Element} - Rendered component
 */
const TouchFriendlyButton = ({ children, onClick, className = '', ...props }) => {
  return (
    <button
      onClick={onClick}
      className={`p-3 rounded-full touch-action-manipulation min-h-[44px] min-w-[44px] ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default TouchFriendlyButton;
