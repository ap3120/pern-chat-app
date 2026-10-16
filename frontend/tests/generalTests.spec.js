import { test, expect } from '@playwright/test';
import {
    fillRegisterForm,
    registerNewUser,
    fillLoginForm,
    loginUser,
    fillChangeUserPasswordFrom
} from "./playwrigth-utils";

const user1 = 'test_user1';
const user2 = 'test_user2';
const user3 = 'test_user3';
const user4 = 'test_user4';
const user1Pwd = "mdp_test_user1";
const user2Pwd = "mdp_test_user2";
const user3Pwd = "mdp_test_user3";
const shortPwd = "short";
const nonMatchingPwd = "non_matching_pwd";
const wrongPwd = "wrong_pwd";

test.describe('General tests', () => {
    test.describe.configure({ mode: 'serial' });
    
    let context;
    let page;

    test.beforeEach(async({}, testInfo) => {
        console.log(`Test "${testInfo.title}" starting`);
    })

    test.afterEach(async({}, testInfo) => {
        console.log(`Test "${testInfo.title}" ending`);
    })

    test('Register new users', async ({browser}) => {
        context = await browser.newContext();
        page = await context.newPage();
        await page.goto('http://localhost:3000/');
        await page.getByTestId('register-page-link').click();
        await fillRegisterForm(page, user1, shortPwd, shortPwd);
        await expect(page.getByTestId('password-form-helper')).toHaveText('Password must be at least height characters long');
        await fillRegisterForm(page, user1, user1Pwd, nonMatchingPwd);
        await expect(page.getByTestId('confirm-password-form-helper')).toHaveText('Passwords must match');
        // Creating new user1
        await registerNewUser(page, user1, user1Pwd, user1Pwd);
        // Creating new user2
        await registerNewUser(page, user2, user2Pwd, user2Pwd);
        // Creating existing user1
        await fillRegisterForm(page, user1, user1Pwd, user1Pwd)
        await expect(page.getByTestId('error-alert')).toBeVisible();
        await expect(page.getByTestId('error-alert')).toHaveText(`User ${user1} already exists.`);
        // Creating new user3
        await registerNewUser(page, user3, user3Pwd, user3Pwd);
        await page.getByTestId('login-page-link').click();
    });

    test('Login a user', async() => {
        // Login with non existing user
        await fillLoginForm(page, user4, 'mdp_test_user4');
        await expect(page.getByTestId('error-alert')).toBeVisible();
        await expect(page.getByTestId('error-alert')).toHaveText(`User ${user4} doesn't exist.`);
        // Login with wrong password
        await fillLoginForm(page, user1, 'wrong_pwd');
        await expect(page.getByTestId('error-alert')).toBeVisible();
        await expect(page.getByTestId('error-alert')).toHaveText(`Wrong password for ${user1}`);
        // Successfull login
        await loginUser(page, user1, user1Pwd);
    });

    test('Send a message', async() => {
        await page.getByTestId('new-chat-btn').click();
        await page.getByRole('button', { name: 'Open' }).click();
        await page.getByRole('option', { name: user2 }).click();
        await page.getByRole('listitem').click();
        await expect(page.getByRole('paragraph').filter({ hasText: user2 })).toBeVisible();
        await page.getByTestId('text-area').fill(`Hello ${user2}`);
        await page.getByTestId('send-msg-btn').click();
        await expect(page.getByTestId('message-0')).toHaveText(`Hello ${user2}`);
    });

    test('Change password', async() => {
        const newPwd = "new_mdp_test_user1";
        await page.getByTestId('settings-btn').click();
        await page.getByRole('menuitem', { name: 'Profile' }).click();
        await fillChangeUserPasswordFrom(page, wrongPwd, newPwd, newPwd);
        await expect(page.getByTestId('error-alert')).toHaveText('Incorrect password.');
        await fillChangeUserPasswordFrom(page, user1Pwd, newPwd, nonMatchingPwd);
        await expect(page.getByTestId('confirm-new-password-helper-text')).toHaveText('Passwords do not match');
        await fillChangeUserPasswordFrom(page, user1Pwd, shortPwd, shortPwd);
        await expect(page.getByTestId('new-password-helper-text')).toHaveText('Password must be at least eight characters long');
        await fillChangeUserPasswordFrom(page, user1Pwd, newPwd, newPwd);
        await expect(page.getByTestId('success-alert')).toHaveText('Password successfully updated');
        await page.locator('div').filter({ hasText: /^Welcome, test_user1$/ }).getByRole('button').click();
        await page.getByTestId('settings-btn').click();
        await page.getByRole('menuitem', { name: 'Logout' }).click();
        await loginUser(page, user1, newPwd);
    })
});

