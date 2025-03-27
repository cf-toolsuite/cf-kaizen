import React from 'react';
import { Clock, Target, PhoneIncoming, PhoneOutgoing, Sigma, Gauge } from 'lucide-react';

const MetadataDisplay = ({ metadata, isDarkMode, condensed = false }) => {
  if (!metadata) return null;

  const styleClass = `${condensed ? 'text-xs mt-1' : 'mt-2 text-xs'} ${
    isDarkMode ? (condensed ? 'text-gray-300' : 'text-gray-400') : (condensed ? 'text-gray-600' : 'text-gray-500')
  }`;

  const iconSize = condensed ? 10 : 12;

  return (
    <div className={styleClass}>
      <div className={`flex ${condensed ? 'gap-x-3' : 'flex-wrap gap-x-4'}`}>
        {metadata.model && !condensed && (
          <span className="flex items-center gap-1">
            <Target size={iconSize} /> {metadata.model}
          </span>
        )}
        {metadata.responseTime && (
          <span className="flex items-center gap-1">
            <Clock size={iconSize} /> {metadata.responseTime}
          </span>
        )}
        {metadata.inputTokens && !condensed && (
          <span className="flex items-center gap-1">
            <PhoneIncoming size={iconSize} /> {metadata.inputTokens}
          </span>
        )}
        {metadata.outputTokens && !condensed && (
          <span className="flex items-center gap-1">
            <PhoneOutgoing size={iconSize} /> {metadata.outputTokens}
          </span>
        )}
        {metadata.totalTokens && (
          <span className="flex items-center gap-1">
            <Sigma size={iconSize} /> {metadata.totalTokens}
          </span>
        )}
        {metadata.tokensPerSecond && (
          <span className="flex items-center gap-1">
            <Gauge size={iconSize} /> {metadata.tokensPerSecond} t/s
          </span>
        )}
      </div>
    </div>
  );
};

export default React.memo(MetadataDisplay);
