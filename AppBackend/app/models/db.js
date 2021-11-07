const mysql = require('mysql');
const dbConfig = require('../config/db.config.js');

const connection = mysql.createConnection({
    host: dbConfig.HOST,
    user: dbConfig.USER,
    port: dbConfig.PORT,
    password: dbConfig.PASSWORD,
    database: dbConfig.DB
});

connection.connect(error => {
    if (error) throw error;
    console.log("[LOG]: Successfully connected to database.")
})

module.exports = connection;