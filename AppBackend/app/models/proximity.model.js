const sql = require('./db');

const Proximity = function (data) {
    this.value = data.value;
};

Proximity.create = (newData, result) => {
    sql.query("INSERT INTO proximity SET ?", newData, (err, res) => {
        if (err) {
            console.log("[ERROR]: ", err);
            result(err, null);
            return;
        }

        console.log("created data: ", { id: res.insertId, ...newData });
        result(null, { id: res.insertId, ...newData });
    })
};

module.exports = Proximity