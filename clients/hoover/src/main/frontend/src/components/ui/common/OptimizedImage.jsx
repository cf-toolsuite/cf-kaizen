// components/ui/common/OptimizedImage.jsx
import React, { useState, useEffect } from 'react';

/**
 * A component that provides optimized image loading for better performance.
 * Implements lazy loading, proper sizing, and loading placeholders.
 *
 * @param {Object} props - Component props
 * @param {string} props.src - Image source URL
 * @param {string} props.alt - Alt text for the image
 * @param {number} props.width - Image width
 * @param {number} props.height - Image height
 * @param {string} props.className - Additional CSS classes
 * @returns {JSX.Element} - Rendered component
 */
const OptimizedImage = ({
  src,
  alt,
  width,
  height,
  className = '',
  ...props
}) => {
  const [loaded, setLoaded] = useState(false);

  // Reset loaded state when src changes
  useEffect(() => {
    setLoaded(false);
  }, [src]);

  return (
    <div
      className={`relative overflow-hidden ${loaded ? '' : 'bg-gray-200 dark:bg-gray-700'}`}
      style={{ width, height, aspectRatio: width && height ? `${width}/${height}` : 'auto' }}
    >
      {!loaded && (
        <div className="absolute inset-0 flex items-center justify-center">
          <span className="animate-pulse">Loading...</span>
        </div>
      )}
      <img
        src={src}
        alt={alt || ''}
        width={width}
        height={height}
        loading="lazy"
        decoding="async"
        onLoad={() => setLoaded(true)}
        className={`${loaded ? 'opacity-100' : 'opacity-0'} transition-opacity duration-300 ${className}`}
        {...props}
      />
    </div>
  );
};

export default OptimizedImage;
