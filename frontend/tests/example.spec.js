// @ts-check
import { test, expect } from '@playwright/test';

const ALICE_PW = process.env.ALICE_PW;

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('textbox', { name: 'Username' }).click();
  await page.getByRole('textbox', { name: 'Username' }).fill('Alice');
  await page.getByRole('textbox', { name: 'Username' }).press('Tab');
  await page.getByRole('textbox', { name: 'Password' }).fill(`${ALICE_PW}`);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('paragraph')).toContainText('Alice');
});

