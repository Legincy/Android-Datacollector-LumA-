const sql = require('./db');

const Accelerometer = function (data) {
    this.x = data.x;
    this.y = data.y;
    this.z = data.z;
    this.deviceid = data.deviceid;
};

Accelerometer.create = (newData, result) => {
    sql.query("INSERT INTO accelerometer SET ?", newData, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...newData });
        result(null, { id: res.insertId, ...newData });
    })
};

module.exports = Accelerometer