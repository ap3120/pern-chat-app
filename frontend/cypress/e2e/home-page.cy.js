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
    cy.get('#username').type('Baldwin')
    cy.get('[id="username"]').should('have.value', 'Baldwin')
  })
})
