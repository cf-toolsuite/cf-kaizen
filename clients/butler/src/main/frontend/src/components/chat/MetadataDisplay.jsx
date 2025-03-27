import React from 'react';
import { Clock, Target, PhoneIncoming, PhoneOutgoing, Sigma, Gauge } from 'lucide-react';

const MetadataDisplay = ({ metadata, isDarkMode, condensed = false, isResponseTile = false }) => {
  if (!metadata) return null;

  const styleClass = `${condensed ? 'text-xs mt-1' : 'mt-2 text-xs'} ${
    isDarkMode ? (condensed ? 'text-gray-300' : 'text-gray-400') : (condensed ? 'text-gray-600' : 'text-gray-500')
  }`;

  const iconSize = condensed ? 10 : 12;

  // For condensed display (collapsed history items)
  if (condensed) {
    return (
      <div className={styleClass}>
        <div className="flex gap-x-3">
          {metadata.responseTime && (
            <span className="flex items-center gap-1">
              <Clock size={iconSize} /> {metadata.responseTime}
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
  }
  
  // For response tile (main chat area)
  if (isResponseTile) {
    return (
      <div className={styleClass}>
        <div className="flex flex-wrap gap-x-3">
          {metadata.model && (
            <span className="flex items-center gap-1">
              <Target size={iconSize} /> {metadata.model}
            </span>
          )}
          {metadata.inputTokens && (
            <span className="flex items-center gap-1">
              <PhoneIncoming size={iconSize} /> {metadata.inputTokens}
            </span>
          )}
          {metadata.outputTokens && (
            <span className="flex items-center gap-1">
              <PhoneOutgoing size={iconSize} /> {metadata.outputTokens}
            </span>
          )}
          {metadata.responseTime && (
            <span className="flex items-center gap-1">
              <Clock size={iconSize} /> {metadata.responseTime}
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
  }
  
  // For expanded history items (two rows)
  return (
    <div className={styleClass}>
      <div className="flex flex-col gap-y-1">
        {/* First line: model, input tokens, output tokens */}
        <div className="flex gap-x-2">
          {metadata.model && (
            <span className="flex items-center gap-1">
              <Target size={iconSize} /> {metadata.model}
            </span>
          )}
          {metadata.inputTokens && (
            <span className="flex items-center gap-1 ml-2">
              <PhoneIncoming size={iconSize} /> {metadata.inputTokens}
            </span>
          )}
          {metadata.outputTokens && (
            <span className="flex items-center gap-1 ml-2">
              <PhoneOutgoing size={iconSize} /> {metadata.outputTokens}
            </span>
          )}
        </div>
        
        {/* Second line: response time, total tokens, tokens per second */}
        <div className="flex gap-x-2">
          {metadata.responseTime && (
            <span className="flex items-center gap-1">
              <Clock size={iconSize} /> {metadata.responseTime}
            </span>
          )}
          {metadata.totalTokens && (
            <span className="flex items-center gap-1 ml-2">
              <Sigma size={iconSize} /> {metadata.totalTokens}
            </span>
          )}
          {metadata.tokensPerSecond && (
            <span className="flex items-center gap-1 ml-2">
              <Gauge size={iconSize} /> {metadata.tokensPerSecond} t/s
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default React.memo(MetadataDisplay);