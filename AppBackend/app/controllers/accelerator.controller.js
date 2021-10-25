const Accelerometer = require('../models/accelerometer.model.js');

exports.create = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const accData = new Accelerometer({
        x: req.body.x,
        y: req.body.y,
        z: req.body.z
    });

    Accelerometer.create(accData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
};