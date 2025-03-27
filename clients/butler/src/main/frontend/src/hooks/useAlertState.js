import { useState, useCallback } from 'react';

export const useAlertState = () => {
  const [alert, setAlert] = useState({ show: false, message: '' });
  
  const showAlert = useCallback((message) => {
    setAlert({ show: true, message });
    setTimeout(() => setAlert({ show: false, message: '' }), 5000);
  }, []);
  
  return {
    alert,
    showAlert
  };
};
