// utils/lazyLoad.js
import React, { lazy, Suspense } from 'react';

/**
 * Helper function to lazy load components with a fallback
 * This improves initial load performance by code-splitting
 * 
 * @param {Function} importFunc - The import function that returns the component module
 * @param {React.ReactNode} fallback - Fallback component to show while loading
 * @returns {React.LazyExoticComponent} - The lazy-loaded component
 */
export const lazyLoad = (importFunc, fallback = null) => {
  const LazyComponent = lazy(importFunc);
  
  return props => (
    <Suspense fallback={fallback || <div className="p-4 text-center">Loading...</div>}>
      <LazyComponent {...props} />
    </Suspense>
  );
};

/**
 * Helper function to preload components before they are needed
 * Useful for prefetching critical components
 * 
 * @param {Function} importFunc - The import function that returns the component module
 */
export const preloadComponent = importFunc => {
  importFunc();
};
