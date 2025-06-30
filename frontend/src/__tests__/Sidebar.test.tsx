import { render, screen } from '@testing-library/react';
import Sidebar from '../components/Sidebar';

describe('Sidebar', () => {
  it('renders the sidebar with new event button', () => {
    render(<Sidebar />);

    expect(
      screen.getByRole('button', { name: '+ New Event' })
    ).toBeInTheDocument();
  });

  it('renders as an aside element', () => {
    const { container } = render(<Sidebar />);

    expect(container.querySelector('aside')).toBeInTheDocument();
  });

  it('has correct CSS classes for sidebar', () => {
    const { container } = render(<Sidebar />);
    const aside = container.querySelector('aside');

    expect(aside).toHaveClass(
      'w-64',
      'bg-white',
      'border-r',
      'p-4',
      'hidden',
      'md:block'
    );
  });

  it('button has correct styling classes', () => {
    render(<Sidebar />);
    const button = screen.getByRole('button', { name: '+ New Event' });

    expect(button).toHaveClass(
      'w-full',
      'bg-blue-500',
      'text-white',
      'py-2',
      'rounded'
    );
  });

  it('button displays correct text', () => {
    render(<Sidebar />);

    expect(screen.getByText('+ New Event')).toBeInTheDocument();
  });
});
