const React = require('react');

const MockCalendar = props => {
  // Filter out non-DOM props that shouldn't be passed to the div
  const {
    onChange,
    value,
    showNavigation,
    onClickDay,
    onClickMonth,
    onClickYear,
    ...domProps
  } = props;

  return React.createElement(
    'div',
    { 'data-testid': 'mock-calendar', ...domProps },
    'Mock Calendar'
  );
};

module.exports = MockCalendar;
module.exports.default = MockCalendar;
