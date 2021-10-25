const express = require("express");
const bodyParser = require("body-parser");
//const routes = require('./app/routes/light.routes.js');

const port = 3000
const app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }))

//routes(app)

app.get("/", (req, res) => {
    res.json({ message: "Hallo Welt" })
})


require("./app/routes/root.routes.js")(app);
app.listen(3000, () => {
    console.log(`Server is running on localhost:${port}`);
})