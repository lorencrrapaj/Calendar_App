import { render } from '@testing-library/react';
import NotificationHandler from '../components/NotificationHandler';

describe('NotificationHandler', () => {
  it('renders without crashing', () => {
    const { container } = render(<NotificationHandler />);

    // Since the component returns null, the container should be empty
    expect(container.firstChild).toBeNull();
  });

  it('returns null as expected', () => {
    const { container } = render(<NotificationHandler />);

    expect(container.innerHTML).toBe('');
  });
});
