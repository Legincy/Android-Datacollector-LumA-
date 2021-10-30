module.exports = app => {
    const light = require('../controllers/light.controller.js');
    const proximity = require('../controllers/proximity.controller.js');
    const accelerometer = require('../controllers/accelerator.controller.js');
    const gyroscope = require('../controllers/gyroscope.controller.js');
    const position = require('../controllers/position.controller.js');

    app.post("/light", light.create);
    app.post("/proximity", proximity.create);
    app.post("/accelerometer", accelerometer.create);
    app.post("/gyroscope", gyroscope.create);
    app.post("/position", position.create);
}