import {expect, test as setup} from '@playwright/test';

const user5 = 'test_user5';
const user5Pwd = "mdp_test_user5";
const user6 = 'test_user6';
const user6Pwd = "mdp_test_user6";

setup('create new database', async ({request}) => {
    console.log('Preparing database...');
    const response = await request.post("/test/reset-db");
    expect(response.ok()).toBeTruthy();

    const createUser5Response = await request.post("/test/create-user", {
        data: {
            "username": user5,
            "password": user5Pwd
        }
    });
    expect(createUser5Response.ok()).toBeTruthy();

    const createUser6Response = await request.post("/test/create-user", {
        data: {
            "username": user6,
            "password": user6Pwd
        }
    });
    expect(createUser6Response.ok()).toBeTruthy();
});