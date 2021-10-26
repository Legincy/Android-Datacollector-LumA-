const express = require("express");
const bodyParser = require("body-parser");

const port = 3000
const app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }))

app.get("/", (req, res) => {
    res.json({ msg: "Unvalid request source." })
})


require("./app/routes/root.routes.js")(app);
app.listen(3000, () => {
    console.log(`Server is running on localhost:${port}`);
})