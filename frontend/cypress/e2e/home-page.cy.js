describe('Login page', () => {
  it('Successfully loads', () => {
    cy.visit('/')
    cy.contains('Register here').click()
    cy.url().should('include', '/register')
  })

  it('Switch theme', () => {
    cy.visit('/')
    
  })

  it('Type username', () => {
    cy.visit('/')
    cy.get('[data-cy="username"]').type('Baldwin').should('include', 'Baldwin')
    //cy.get('[data-cy="username"]').should('have.value', 'Baldwin')
  })
})
