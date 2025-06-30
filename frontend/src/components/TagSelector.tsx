import React, { useState, useEffect } from 'react';
import { TagDTO } from '../pages/CalendarPage';
import api from '../services/api';

interface TagSelectorProps {
  selectedTagIds: number[];
  onTagsChange: (tagIds: number[]) => void;
  className?: string;
}

export default function TagSelector({
  selectedTagIds,
  onTagsChange,
  className = '',
}: TagSelectorProps) {
  const [availableTags, setAvailableTags] = useState<TagDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch available tags
  useEffect(() => {
    const fetchTags = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await api.get<TagDTO[]>('/tags');
        setAvailableTags(response.data);
      } catch (err) {
        console.error('Failed to fetch tags:', err);
        setError('Failed to load tags');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTags();
  }, []);

  const handleTagToggle = (tagId: number) => {
    const isSelected = selectedTagIds.includes(tagId);
    let newSelectedIds: number[];

    if (isSelected) {
      // Remove tag
      newSelectedIds = selectedTagIds.filter(id => id !== tagId);
    } else {
      // Add tag
      newSelectedIds = [...selectedTagIds, tagId];
    }

    onTagsChange(newSelectedIds);
  };

  if (isLoading) {
    return (
      <div className={`${className}`}>
        <label className="block text-sm font-medium text-gray-300 mb-2"></label>
        <div className="text-gray-400 text-sm">Loading tags...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`${className}`}>
        <label className="block text-sm font-medium text-gray-300 mb-2">
          Tags
        </label>
        <div className="text-red-400 text-sm">{error}</div>
      </div>
    );
  }

  return (
    <div className={`${className}`}>
      <label className="block text-sm font-medium text-gray-300 mb-2">
        Tags
      </label>

      {availableTags.length === 0 ? (
        <div className="text-gray-400 text-sm">
          No tags available. Create some tags first.
        </div>
      ) : (
        <div className="space-y-2 max-h-32 overflow-y-auto">
          {availableTags.map(tag => {
            const isSelected = selectedTagIds.includes(tag.id);
            return (
              <label
                key={tag.id}
                className="flex items-center space-x-2 cursor-pointer hover:bg-white/5 p-1 rounded"
              >
                <input
                  type="checkbox"
                  checked={isSelected}
                  onChange={() => handleTagToggle(tag.id)}
                  className="rounded border-gray-600 bg-gray-700 text-blue-600 focus:ring-blue-500 focus:ring-offset-gray-800"
                />
                <span className="text-gray-300 text-sm">{tag.name}</span>
              </label>
            );
          })}
        </div>
      )}

      {/* Selected tags preview */}
      {selectedTagIds.length > 0 && (
        <div className="mt-3 p-2 bg-gray-800 rounded text-sm">
          <span className="text-gray-400">Selected: </span>
          {selectedTagIds
            .map(id => availableTags.find(tag => tag.id === id)?.name)
            .filter(Boolean)
            .map((tagName, index, array) => (
              <span key={tagName} className="text-blue-400">
                {tagName}
                {index < array.length - 1 ? ', ' : ''}
              </span>
            ))}
        </div>
      )}
    </div>
  );
}
