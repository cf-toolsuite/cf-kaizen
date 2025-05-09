import React from 'react';
import ToolSelector from './ToolSelector';
import { useChat } from '../../context/ChatContext';

const ChatInput = () => {
  const {
    question,
    setQuestion,
    isLoading,
    isSubmitDisabled,
    handleSubmit,
    isDarkMode,
    selectedTools
  } = useChat();

  return (
    <form onSubmit={handleSubmit} className="space-y-2 sm:space-y-4">
      <div>
        <textarea
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="Enter your question..."
          className={`w-full p-2 md:p-3 border rounded-md h-24 xs:h-28 sm:h-32 md:h-36 text-base ${
            isDarkMode
              ? 'bg-gray-700 border-gray-600 text-white placeholder-gray-400'
              : 'bg-white border-gray-300 text-gray-900'
          }`}
          disabled={isLoading}
        />
      </div>

      <div className="flex items-stretch md:space-x-4">
        <button
          type="submit"
          className={`px-4 py-2 md:px-5 md:py-3 rounded md:text-base font-medium ${
            isDarkMode
              ? 'bg-blue-600 hover:bg-blue-700 text-white disabled:bg-gray-600'
              : 'bg-blue-500 hover:bg-blue-600 text-white disabled:bg-gray-400'
          } ${isSubmitDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          disabled={isSubmitDisabled}
          title={selectedTools.length === 0 ? 'Please select at least one tool' : ''}
        >
          {isLoading ? 'Processing...' : 'Submit'}
        </button>

        {/* Tool selector button */}
        <div className="ml-2 md:ml-0 flex-grow" style={{ maxWidth: '150px' }}>
          <ToolSelector />
        </div>
      </div>
    </form>
  );
};

export default React.memo(ChatInput);
