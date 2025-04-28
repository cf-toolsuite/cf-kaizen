// components/ui/common/DoubleTapHandler.jsx
import React, { useState, useRef } from 'react';

/**
 * A component that handles double-tap (double-click) gestures on mobile devices.
 * This provides better touch interactions for mobile users.
 *
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child elements to render inside the component
 * @param {Function} props.onDoubleTap - Function to call when a double-tap is detected
 * @param {Function} props.onClick - Optional function to call on single click/tap
 * @param {number} props.delay - Delay between taps to be considered a double-tap (default: 300ms)
 * @param {string} props.className - Additional CSS classes to apply
 * @returns {JSX.Element} - Rendered component
 */
const DoubleTapHandler = ({
  children,
  onDoubleTap,
  onClick,
  delay = 300,
  className = '',
  ...props
}) => {
  const [lastTap, setLastTap] = useState(0);
  const timerRef = useRef(null);

  const handleTap = (e) => {
    const now = Date.now();
    const timeDiff = now - lastTap;

    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }

    if (timeDiff < delay && lastTap > 0) {
      // Double tap detected
      if (onDoubleTap) {
        onDoubleTap(e);
      }
      setLastTap(0);
    } else {
      // First tap
      setLastTap(now);

      // Schedule single tap callback after delay
      if (onClick) {
        timerRef.current = setTimeout(() => {
          onClick(e);
          timerRef.current = null;
        }, delay);
      }
    }
  };

  return (
    <div
      onClick={handleTap}
      className={`touch-action-manipulation ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default DoubleTapHandler;
