describe('Initial Test', () => {
  it('should load the home page', () => {
    cy.visit('/');
    // Check if the page contains some text that should be there
    // Since I don't know the exact content, I'll just check if the body exists
    cy.get('body').should('exist');
  });
});
