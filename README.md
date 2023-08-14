# Getting Started with PERN chat App

This project is a chat application using a Postgres database, an Express server and React.

## Before starting

To use the application you need to create a Postgres database following the SQL provided in `database.sql`.
Then you need to create a `.env` file following the `.env-sample` model and set up your environment variables.

Install the node dependencies:
### `npm install`
### `cd frontend && npm install`

## Starting the server

In the root of the application run:
### `npm run devstart`

The server starts on the port set up in .env or on port 3000 by default.

## Starting the frontend

### `cd frontend && npm start`

If the server runs on port 3000, React might ask you to use a different port, type `'yes'`.
