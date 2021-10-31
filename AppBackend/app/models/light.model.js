const sql = require('./db');

const Light = function (data) {
    this.value = data.value;
    this.device_id = data.deviceid;
};

Light.create = (newData, result) => {
    sql.query("INSERT INTO light SET ?", newData, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...newData });
        result(null, { id: res.insertId, ...newData });
    })
};

module.exports = Light