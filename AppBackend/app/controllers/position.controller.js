const Position = require('../models/position.model.js');

exports.addPosition = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const devData = new Position({
        deviceid: req.body.deviceid,
        longitude: req.body.longitude,
        latitude: req.body.latitude,
        route: req.body.route,
        type: req.body.type,
        marked: req.body.marked
    });

    console.log("RECEIVED: " + devData);

    Position.addPosition(devData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
}

exports.getRoute = (req, res) => {
    if (!req.body) {
        res.status(400).send({
            message: "Content can not be empty!"
        })
    }

    const devData = new Position({
        deviceid: req.body.deviceid
    });

    Position.getRoute(devData, (err, data) => {
        if (err)
            res.status(500).send({
                message:
                    err.message || "Some error occurred while inserting the data."
            });
        else res.send(data);
    });
}