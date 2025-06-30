// Debug script to test parseRecurrenceRule function
const parseRecurrenceRule = (
  rrule,
  recurrenceEndDate,
  recurrenceCount
) => {
  if (!rrule) {
    return {
      repeatType: 'none',
      repeatEndType: 'never',
      repeatEndDate: '',
      repeatCount: 10,
    };
  }

  let repeatType = 'none';
  if (rrule.includes('FREQ=DAILY')) repeatType = 'daily';
  else if (rrule.includes('FREQ=WEEKLY')) repeatType = 'weekly';
  else if (rrule.includes('FREQ=MONTHLY')) repeatType = 'monthly';

  // Determine repeat end type based on available data
  let repeatEndType = 'never';
  let repeatEndDate = '';
  let repeatCount = 10;

  if (recurrenceEndDate) {
    repeatEndType = 'date';
    repeatEndDate = recurrenceEndDate.split('T')[0]; // Convert to date format
  } else if (recurrenceCount && recurrenceCount > 0) {
    repeatEndType = 'count';
    repeatCount = recurrenceCount;
  }

  return {
    repeatType,
    repeatEndType,
    repeatEndDate,
    repeatCount,
  };
};

// Test with the data from the failing test
const testData = {
  recurrenceRule: 'FREQ=WEEKLY',
  recurrenceCount: 5,
};

const result = parseRecurrenceRule(
  testData.recurrenceRule,
  undefined,
  testData.recurrenceCount
);

console.log('Input:', testData);
console.log('Output:', result);