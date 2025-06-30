import { render, screen } from '@testing-library/react';
import CalendarView from '../components/CalendarView';

describe('CalendarView', () => {
  it('renders calendar placeholder text', () => {
    render(<CalendarView />);

    expect(screen.getByText('Calendar goes here')).toBeInTheDocument();
  });

  it('renders a div element', () => {
    const { container } = render(<CalendarView />);

    expect(container.firstChild).toBeInstanceOf(HTMLDivElement);
  });
});
