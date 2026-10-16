import {expect, test as teardown} from '@playwright/test';

teardown('cleaning database', async ({request}) => {
    console.log('cleaning database...');
    const response = await request.post("/test/reset-db");
    expect(response.ok()).toBeTruthy();
});