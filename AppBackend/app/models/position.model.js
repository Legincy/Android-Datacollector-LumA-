const sql = require('./db');

const Position = function (data) {
    this.longitude = data.longitude;
    this.latitude = data.latitude;
    this.deviceid = data.deviceid;
    this.type = data.type;
    this.marked = data.marked;
    this.route = data.route;
};

Position.addPosition = (data, result) => {
    sql.query(`INSERT INTO \`position\`(\`routen_id\`, \`longitude\`, \`latitude\`, \`type_id\`, \`marked\`) 
    VALUES ('${data.route}','${data.longitude}','${data.latitude}','${data.type}','${data.marked}');`, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        result(null, { msg: "OK", code: 200 });
    })
};

Position.getRoute = (data, result) => {
    sql.query(`INSERT INTO \`route\`(\`device_id\`) VALUES ('${data.deviceid}');`, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        result(null, { msg: "OK", code: 200, routeid: res.insertId });
    })
};

module.exports = Position