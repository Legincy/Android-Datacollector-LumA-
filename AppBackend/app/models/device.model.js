const sql = require('./db');

const Device = function (data) {
    this.deviceId = data.deviceId;
};

Device.findById = (deviceId, result) => {
    sql.query(`SELECT * FROM device WHERE id = '${deviceId}'`, (err, res) => {
        if (err) {
            console.log("error: ", err);
            result(err, null);
            return;
        }

        if (res.length) {
            console.log("found device: ", res[0]);
            result(null, { msg: "success", ...res[0] });
            return;
        }

        result({ kind: "not_found" }, null);
    });
};

Device.create = (data, result) => {
    console.log(data)
    sql.query(`INSERT INTO device SET id= '${data.deviceId}'`, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...data });
        result(null, { msg: "success", ...data });
    })
};



module.exports = Device