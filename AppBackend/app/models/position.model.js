const sql = require('./db');

const Position = function (data) {
    this.longitude = data.longitude;
    this.latitude = data.latitude;
    this.device_id = data.deviceid;
    this.type = data.type;
};

Position.create = (newData, result) => {
    sql.query("INSERT INTO position SET ?", newData, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...newData });
        result(null, { id: res.insertId, ...newData });
    })
};

module.exports = Position