import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import TagSelector from '../components/TagSelector';
import api from '../services/api';

// Mock the API
jest.mock('../services/api');
const mockedApi = api as jest.Mocked<typeof api>;

const mockTags = [
  { id: 1, name: 'Work' },
  { id: 2, name: 'Personal' },
  { id: 3, name: 'Meeting' },
];

describe('TagSelector', () => {
  const mockOnTagsChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockedApi.get.mockResolvedValue({ data: mockTags });
  });

  const defaultProps = {
    selectedTagIds: [],
    onTagsChange: mockOnTagsChange,
  };

  it('renders loading state initially', () => {
    render(<TagSelector {...defaultProps} />);
    expect(screen.getByText('Loading tags...')).toBeInTheDocument();
  });

  it('renders tags after loading', async () => {
    render(<TagSelector {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('Work')).toBeInTheDocument();
      expect(screen.getByText('Personal')).toBeInTheDocument();
      expect(screen.getByText('Meeting')).toBeInTheDocument();
    });
  });

  it('renders error state when API fails', async () => {
    mockedApi.get.mockRejectedValue(new Error('API Error'));

    render(<TagSelector {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load tags')).toBeInTheDocument();
    });
  });

  it('renders empty state when no tags available', async () => {
    mockedApi.get.mockResolvedValue({ data: [] });

    render(<TagSelector {...defaultProps} />);

    await waitFor(() => {
      expect(
        screen.getByText('No tags available. Create some tags first.')
      ).toBeInTheDocument();
    });
  });

  it('shows selected tags as checked', async () => {
    render(<TagSelector {...defaultProps} selectedTagIds={[1, 3]} />);

    await waitFor(() => {
      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      const personalCheckbox = screen.getByRole('checkbox', {
        name: /personal/i,
      });
      const meetingCheckbox = screen.getByRole('checkbox', {
        name: /meeting/i,
      });

      expect(workCheckbox).toBeChecked();
      expect(personalCheckbox).not.toBeChecked();
      expect(meetingCheckbox).toBeChecked();
    });
  });

  it('calls onTagsChange when tag is selected', async () => {
    render(<TagSelector {...defaultProps} />);

    await waitFor(() => {
      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      fireEvent.click(workCheckbox);
    });

    expect(mockOnTagsChange).toHaveBeenCalledWith([1]);
  });

  it('calls onTagsChange when tag is deselected', async () => {
    render(<TagSelector {...defaultProps} selectedTagIds={[1, 2]} />);

    await waitFor(() => {
      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      fireEvent.click(workCheckbox);
    });

    expect(mockOnTagsChange).toHaveBeenCalledWith([2]);
  });

  it('displays selected tags preview', async () => {
    render(<TagSelector {...defaultProps} selectedTagIds={[1, 3]} />);

    // Wait for tags to load and selected tags preview to appear
    await waitFor(() => {
      expect(screen.getByText('Selected:')).toBeInTheDocument();
    });

    // Check that Work and Meeting appear in the preview
    await waitFor(() => {
      const workElements = screen.getAllByText('Work');
      const meetingElements = screen.getAllByText('Meeting');
      expect(workElements.length).toBeGreaterThanOrEqual(1);
      expect(meetingElements.length).toBeGreaterThanOrEqual(1);
    });
  });

  it('does not show selected tags preview when none selected', async () => {
    render(<TagSelector {...defaultProps} selectedTagIds={[]} />);

    await waitFor(() => {
      expect(screen.queryByText('Selected:')).not.toBeInTheDocument();
    });
  });

  it('applies custom className', () => {
    const { container } = render(
      <TagSelector {...defaultProps} className="custom-class" />
    );
    expect(container.firstChild).toHaveClass('custom-class');
  });

  it('handles multiple tag selection correctly', async () => {
    render(<TagSelector {...defaultProps} selectedTagIds={[1]} />);

    await waitFor(() => {
      const personalCheckbox = screen.getByRole('checkbox', {
        name: /personal/i,
      });
      fireEvent.click(personalCheckbox);
    });

    expect(mockOnTagsChange).toHaveBeenCalledWith([1, 2]);
  });

  it('fetches tags on mount', () => {
    render(<TagSelector {...defaultProps} />);
    expect(mockedApi.get).toHaveBeenCalledWith('/tags');
  });
});
