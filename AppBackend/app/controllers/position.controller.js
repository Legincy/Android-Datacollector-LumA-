const Position = require('../models/position.model.js');

exports.create = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const posData = new Position({
        longitude: req.body.longitude,
        latitude: req.body.latitude,
        deviceid: req.body.deviceid,
        type: req.body.type
    });

    Position.create(posData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
};