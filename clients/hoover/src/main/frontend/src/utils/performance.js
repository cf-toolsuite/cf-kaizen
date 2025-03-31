// utils/performance.js
/**
 * A collection of utility functions for performance optimization.
 * These help improve mobile performance by optimizing how resources are loaded.
 */

/**
 * Debounce function to limit the rate at which a function can fire.
 * Useful for optimizing event handlers like scroll or resize.
 * 
 * @param {Function} func - The function to debounce
 * @param {number} wait - The time to wait in milliseconds
 * @param {boolean} immediate - Whether to call the function immediately
 * @returns {Function} - The debounced function
 */
export const debounce = (func, wait = 300, immediate = false) => {
  let timeout;
  
  return function(...args) {
    const context = this;
    
    const later = function() {
      timeout = null;
      if (!immediate) func.apply(context, args);
    };
    
    const callNow = immediate && !timeout;
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
    
    if (callNow) func.apply(context, args);
  };
};

/**
 * Throttle function to limit the number of times a function can be called
 * in a given time period. Different from debounce - this will execute
 * at a regular rate, rather than waiting for a pause.
 * 
 * @param {Function} func - The function to throttle
 * @param {number} limit - The time limit in milliseconds
 * @returns {Function} - The throttled function
 */
export const throttle = (func, limit = 300) => {
  let inThrottle;
  
  return function(...args) {
    const context = this;
    
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};

/**
 * Detect if the device is a mobile device based on user agent or screen size
 * 
 * @returns {boolean} - True if the device is likely a mobile device
 */
export const isMobileDevice = () => {
  const userAgent = navigator.userAgent || navigator.vendor || window.opera;
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i;
  
  // Check screen size as well (common mobile breakpoint)
  const smallScreen = window.innerWidth < 768;
  
  return mobileRegex.test(userAgent) || smallScreen;
};

/**
 * Detect touch support in the browser
 * 
 * @returns {boolean} - True if touch is supported
 */
export const hasTouchSupport = () => {
  return 'ontouchstart' in window || 
    navigator.maxTouchPoints > 0 || 
    navigator.msMaxTouchPoints > 0;
};

/**
 * Preconnect to important domains to improve performance
 * 
 * @param {Array} domains - Array of domains to preconnect to
 */
export const preconnect = (domains = []) => {
  domains.forEach(domain => {
    const link = document.createElement('link');
    link.rel = 'preconnect';
    link.href = domain;
    link.crossOrigin = 'anonymous';
    document.head.appendChild(link);
  });
};
