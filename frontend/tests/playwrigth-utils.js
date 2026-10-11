import {expect} from '@playwright/test'

export async function fillRegisterForm(page, username, password, confirmPassword){
    await page.getByTestId('username-field').locator('input').fill(username);
    await page.getByTestId('password-field').locator('input').fill(password);
    await page.getByTestId('confirm-password-field').locator('input').fill(confirmPassword);
    await page.getByTestId('register-btn').click();
}

export async function registerNewUser(page, username, password, confirmPassword) {
    await fillRegisterForm(page, username, password, confirmPassword);
    await expect(page.getByTestId('success-alert')).toBeVisible();
    await expect(page.getByTestId('success-alert')).toHaveText(`User ${username} was successfully created.`);
}

export async function fillLoginForm(page, username, password) {
    await page.getByTestId('username-field').locator('input').fill(username);
    await page.getByTestId('password-field').locator('input').fill(password);
    await page.getByTestId('login-btn').click();
}

export async function loginUser(page, username, password) {
    await fillLoginForm(page, username, password);
    await expect(page.getByTestId('session-username')).toHaveText(username);
}

export async function fillChangeUserPasswordFrom(page, currentPassword, newPassword, confirmNewPassword) {
    await page.getByTestId('current-password-field').locator('input').fill(currentPassword);
    await page.getByTestId('new-password-field').locator('input').fill(newPassword);
    await page.getByTestId('confirm-new-password-field').locator('input').fill(confirmNewPassword);
    await page.getByTestId('change-password-btn').click();
}