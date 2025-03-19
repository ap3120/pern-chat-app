# Getting Started with PERN chat App

This project is a chat application using a Postgres database, an java server and a React frontend. The Express server is not maintained.

## Prerequisites
You need to have the following installed:
1. Postgres
2. Java runtime environment
3. Nodejs
4. Node package manager
4. Maven

## Before starting

### Environment variables
To use the application you need to create a Postgres database following the SQL provided in `javascriptChatServer/database.sql`.
Then you need to create a `.env` file following the `.env-sample` model in both the `frontend` and `javaChatServer` folders.

### Dependencies
1. javascript: `cd frontend && npm install`
2. java: `cd javaChatServer && mvn clean install`

## Starting the Express server (not maintained)

In the root of the application run:
### `npm run devstart`

The server starts on the port set up in .env or on port 3000 by default.

## Starting the java server

### `cd javaChatServer && java -cp target/chatserver-1.0-SNAPSHOT-jar-with-dependencies.jar chatapp.JavaServer`

## Starting the frontend

### `cd frontend && npm start`

If the server runs on port 3000, React might ask you to use a different port, type `'yes'`.
