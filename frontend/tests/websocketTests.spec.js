import {expect, test} from '@playwright/test'

import {loginUser} from "./playwrigth-utils";

const user5 = 'test_user5';
const user6 = 'test_user6';
const user5Pwd = "mdp_test_user5";
const user6Pwd = "mdp_test_user6";

test.describe("Websocket tests", () => {

    test.beforeEach(async ({page}, testInfo) => {
        console.log(`Test "${testInfo.title}" starting`);
        await page.goto("http://localhost:3000");
    })

    test.afterEach(async ({page}, testInfo) => {
        await page.getByTestId('settings-btn').click();
        await page.getByRole('menuitem', { name: 'Logout' }).click();
        console.log(`Test "${testInfo.title}" ending`);
    })

    test("Send ping and receive pong", async ({page}) => {
        await loginUser(page, user5, user5Pwd);
        await page.getByTestId('new-chat-btn').click();
        await page.getByRole('button', { name: 'Open' }).click();
        await page.getByRole('option', { name: user6 }).click();
        await page.getByRole('listitem').click();
        await page.getByTestId('text-area').fill(`ping`);
        await page.getByTestId('send-msg-btn').click();
        await expect(page.getByTestId('message-0')).toHaveText(`ping`);
        /* Wait for user6 to send pong message */
        await page.waitForTimeout(2000);
        await expect(page.getByTestId('message-1')).toHaveText(`pong`);
    })

    test("Receive ping and send pong", async ({page}) => {
        await loginUser(page, user6, user6Pwd);
        await page.getByTestId('new-chat-btn').click();
        await page.getByRole('button', { name: 'Open' }).click();
        await page.getByRole('option', { name: user5 }).click();
        await page.getByRole('listitem').click();
        /* Let user5 send ping message first */
        await page.waitForTimeout(2000);
        await expect(page.getByTestId('message-0')).toHaveText(`ping`);
        await page.getByTestId('text-area').fill(`pong`);
        await page.getByTestId('send-msg-btn').click();
        await expect(page.getByTestId('message-1')).toHaveText(`pong`);
    })
});