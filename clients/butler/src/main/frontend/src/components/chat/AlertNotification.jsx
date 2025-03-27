import React from 'react';
import { Alert, AlertDescription } from '@/components/ui/alert';

const AlertNotification = ({ alert }) => {
  if (!alert.show) return null;

  return (
    <Alert className="mb-4 bg-red-100">
      <AlertDescription>{alert.message}</AlertDescription>
    </Alert>
  );
};

export default React.memo(AlertNotification);
