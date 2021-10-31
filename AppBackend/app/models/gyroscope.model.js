const sql = require('./db');

const Gyroscope = function (data) {
    this.x = data.x;
    this.y = data.y;
    this.z = data.z;
    this.device_id = data.deviceid;
};

Gyroscope.create = (newData, result) => {
    sql.query("INSERT INTO gyroscope SET ?", newData, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...newData });
        result(null, { id: res.insertId, ...newData });
    })
};

module.exports = Gyroscope