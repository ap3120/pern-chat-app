describe('Login page', () => {
  it('successfully loads the login page', () => {
    cy.visit('/')
    cy.contains('Register here').click()
    cy.url().should('include', '/register')
  })

  it('switches theme', () => {
    cy.visit('/')
    
  })

  it('tries to loggin', () => {
    cy.visit('/')
    cy.get('[id="username"]').type('Baldwin')
    cy.get('[id="username"]').should('have.value', 'Baldwin')
    cy.get('[id="password"]').type('wrongpassword')
    cy.get('[id="password"]').should('have.value', 'wrongpassword')

    cy.get('[id="login-btn"]').click();
    cy.get('[id="helper-text"]').should('be.visible')

    cy.get('[id="password"]').clear().type(Cypress.env('baldwin_password'))
    cy.get('[id="password"]').should('have.value', Cypress.env('baldwin_password'))
    cy.get('[id="login-btn"]').click();
    cy.url().should('include', '/dashboard')
  })
})
