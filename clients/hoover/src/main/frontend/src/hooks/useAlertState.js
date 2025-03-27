import { useState, useCallback, useRef } from 'react';

export const useAlertState = () => {
  const [alert, setAlert] = useState({ show: false, message: '', type: 'destructive' });
  const alertTimeoutRef = useRef(null);
  
  const showAlert = useCallback((message, type = 'destructive', duration = 5000) => {
    // Clear any existing timeout to prevent multiple alerts overlapping
    if (alertTimeoutRef.current) {
      clearTimeout(alertTimeoutRef.current);
    }
    
    setAlert({ show: true, message, type });
    
    // Set new timeout and store the reference
    alertTimeoutRef.current = setTimeout(() => {
      setAlert({ show: false, message: '', type: 'destructive' });
      alertTimeoutRef.current = null;
    }, duration);
  }, []);
  
  // Explicitly hide the alert
  const hideAlert = useCallback(() => {
    if (alertTimeoutRef.current) {
      clearTimeout(alertTimeoutRef.current);
      alertTimeoutRef.current = null;
    }
    setAlert({ show: false, message: '', type: 'destructive' });
  }, []);
  
  return {
    alert,
    showAlert,
    hideAlert
  };
};
