import React from 'react';

export const Alert = ({ variant = 'default', className, isDarkMode = false, ...props }) => {
  const baseClass = 'p-4 mb-4 rounded-lg flex items-center';
  const variantClasses = {
    default: isDarkMode 
      ? 'bg-blue-800 text-blue-100'
      : 'bg-blue-100 text-blue-800',
    destructive: isDarkMode 
      ? 'bg-red-900 text-gray-100'
      : 'bg-red-200 text-red-900',
    warning: isDarkMode 
      ? 'bg-amber-900 text-amber-100'
      : 'bg-amber-100 text-amber-800',
    success: isDarkMode 
      ? 'bg-green-900 text-green-100'
      : 'bg-green-100 text-green-800'
  };

  return (
    <div 
      role="alert"
      className={`${baseClass} ${variantClasses[variant]} ${className}`} 
      {...props} 
    />
  );
};

export const AlertTitle = ({ className, ...props }) => (
  <h5 className={`font-medium text-sm ${className}`} {...props} />
);

export const AlertDescription = ({ className, ...props }) => (
  <div className={`text-sm ${className}`} {...props} />
);

export const AlertIcon = ({ children, className, ...props }) => (
  <div className={`mr-3 ${className}`} {...props}>
    {children}
  </div>
);
