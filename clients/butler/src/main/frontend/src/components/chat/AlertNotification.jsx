import React from 'react';
import { Alert, AlertIcon, AlertDescription } from '@/components/ui/alert';
import { AlertTriangle, AlertCircle, Info, CheckCircle, X } from 'lucide-react';
import { useChat } from '../../context/ChatContext';

const AlertNotification = ({ alert }) => {
  const { isDarkMode, hideAlert } = useChat();
  
  if (!alert.show) return null;

  return (
    <Alert 
      variant={alert.type} 
      isDarkMode={isDarkMode}
      className="mb-4 shadow-lg animate-in slide-in-from-top-5 fade-in duration-300 border border-opacity-20"
    >
      <AlertIcon>
        {alert.type === 'destructive' && <AlertCircle className="h-5 w-5" />}
        {alert.type === 'warning' && <AlertTriangle className="h-5 w-5" />}
        {alert.type === 'default' && <Info className="h-5 w-5" />}
        {alert.type === 'success' && <CheckCircle className="h-5 w-5" />}
      </AlertIcon>
      <AlertDescription className="flex-grow">{alert.message}</AlertDescription>
      <button 
        onClick={hideAlert}
        className="ml-auto p-1 rounded-full hover:bg-opacity-20 hover:bg-black transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-transparent focus:ring-current"
        aria-label="Close alert"
      >
        <X className="h-4 w-4" />
      </button>
    </Alert>
  );
};

export default React.memo(AlertNotification);
