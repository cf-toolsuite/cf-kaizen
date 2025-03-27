/**
 * Enhanced markdown processor for better spacing in streamed responses
 * Adds suitable line breaks to improve readability while maintaining structure
 */

// Function to preprocess markdown before rendering
export const processMarkdown = (markdown) => {
  if (!markdown) return '';

  // First, normalize line endings
  let processed = markdown.replace(/\r\n|\r/g, '\n');

  // Add line break after sentences that end with a period, question mark, or exclamation point
  // but only when followed by another sentence (not at the end of paragraphs)
  processed = processed.replace(/([.!?])\s+(?=[A-Z])/g, '$1\n');

  // Handle app details pattern (key-value pairs with indentation)
  // This specifically targets structures like "* **App ID:** abcd-1234"
  processed = processed.replace(/(\s*\*\s+\*\*.+:\*\*\s+.+)$/gm, '$1\n');

  // Add specific handling for numbered application lists (like the example)
  processed = processed.replace(/(\d+\. \*\*Application Name:\*\* .+)$/gm, '$1\n');

  // Handle list items - keep them together with appropriate spacing
  // First identify list blocks
  const listBlocks = [];
  let inList = false;
  let listStart = 0;

  processed.split('\n').forEach((line, index, lines) => {
    const isListItem = line.match(/^\s*[-*+]\s+/) || line.match(/^\s*\d+\.\s+/);

    if (isListItem && !inList) {
      inList = true;
      listStart = index;
    } else if (!isListItem && inList) {
      inList = false;
      listBlocks.push({ start: listStart, end: index - 1 });
    }
  });

  if (inList) {
    // If we ended while still in a list
    listBlocks.push({ start: listStart, end: processed.split('\n').length - 1 });
  }

  // Now treat each list block to ensure proper spacing
  let lines = processed.split('\n');

  listBlocks.forEach(block => {
    // Add a blank line before and after the list block, but not within it
    if (block.start > 0) {
      lines[block.start - 1] += '\n';
    }
    if (block.end < lines.length - 1) {
      lines[block.end] += '\n';
    }
  });

  processed = lines.join('\n');

  // Handle section headers - add a line break after
  processed = processed.replace(/^(#{1,6}\s.+)$/gm, '$1\n');

  // Handle section titles in bold (like "Applications" in the example)
  processed = processed.replace(/^(\*\*[A-Za-z\s]+\*\*)$/gm, '\n$1\n');

  // Handle code blocks - add a line break after
  processed = processed.replace(/```[\s\S]*?```/g, match => match + '\n');

  // Handle blockquotes
  processed = processed.replace(/^(>\s.+)$/gm, '$1\n');

  // Clean up excessive line breaks (more than 2 consecutive)
  processed = processed.replace(/\n{3,}/g, '\n\n');

  return processed;
};
