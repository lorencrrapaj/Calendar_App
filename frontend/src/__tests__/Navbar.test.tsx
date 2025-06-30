import { render, screen } from '@testing-library/react';
import Navbar from '../components/Navbar';

describe('Navbar', () => {
  it('renders the navbar with correct title', () => {
    render(<Navbar />);

    expect(screen.getByText('My Calendar')).toBeInTheDocument();
  });

  it('renders as a header element', () => {
    const { container } = render(<Navbar />);

    expect(container.querySelector('header')).toBeInTheDocument();
  });

  it('has correct CSS classes', () => {
    const { container } = render(<Navbar />);
    const header = container.querySelector('header');

    expect(header).toHaveClass('bg-white', 'shadow', 'p-4');
  });

  it('title has correct styling classes', () => {
    render(<Navbar />);
    const title = screen.getByText('My Calendar');

    expect(title).toHaveClass('text-xl', 'font-semibold');
  });
});
