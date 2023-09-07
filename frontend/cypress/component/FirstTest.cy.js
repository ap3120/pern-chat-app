describe('FirstTest.cy.js', () => {
  it('does not much', () => {
    cy.visit('http://localhost:3001');
    cy.contains('Register here').click();
    cy.url().should('include', '/register');
  })
})
