import React from 'react';

export const Alert = ({ variant = 'default', className, ...props }) => {
  const baseClass = 'p-4 mb-4 rounded-lg';
  const variantClasses = {
    default: 'bg-blue-100 text-blue-800',
    destructive: 'bg-red-100 text-red-800'
  };

  return (
    <div className={`${baseClass} ${variantClasses[variant]} ${className}`} {...props} />
  );
};

export const AlertDescription = ({ className, ...props }) => (
  <div className={`text-sm ${className}`} {...props} />
);
