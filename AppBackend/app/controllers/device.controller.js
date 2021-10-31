const Device = require('../models/device.model.js');

exports.findOne = (req, res) => {
    Device.findById(req.params.deviceId, (err, data) => {
        if (err) {
            if (err.kind === "not_found") {
                res.status(404).send({
                    message: `Could not find Device with id ${req.params.deviceId}.`
                });
            } else {
                res.status(500).send({
                    message: "Error retrieving Device with id " + req.params.deviceId
                });
            }
        } else res.send(data);
    });
};

exports.create = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const devData = new Device({
        deviceId: req.body.deviceid
    });

    Device.create(devData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
};