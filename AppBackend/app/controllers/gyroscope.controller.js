const Gyroscope = require('../models/gyroscope.model.js');

exports.create = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const gyroData = new Gyroscope({
        x: req.body.x,
        y: req.body.y,
        z: req.body.z
    });

    Gyroscope.create(gyroData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
};