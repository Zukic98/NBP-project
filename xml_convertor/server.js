const express = require('express');
const oracledb = require('oracledb');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.text({ type: 'application/xml' })); // Omogućava čitanje XML-a kao text

// Konfiguracija konekcije (Prilagodi svojim podacima)
const dbConfig = {
    user: "XXXXXXXXXXXXXXX",
    password: "XXXXXXXXXXXXXXX",
    connectString: "XXXXXXXXXXXXXXXXX" // npr. localhost:1521/XEPDB1
};

// 1. Endpoint za eksport: DB -> XML
app.get('/api/export', async (req, res) => {
    let connection;
    try {
        connection = await oracledb.getConnection(dbConfig);
        // Pozivamo PL/SQL funkciju
        const result = await connection.execute(
            `BEGIN :ret := NBPT5.EXPORT_ADRESE_XML(); END;`,
            { ret: { dir: oracledb.BIND_OUT, type: oracledb.STRING, maxSize: 50000 } }
        );
        res.header('Content-Type', 'application/xml');
        res.send(result.outBinds.ret || "<Adrese/>");
    } catch (err) {
        res.status(500).send(err.message);
    } finally {
        if (connection) await connection.close();
    }
});

// 2. Endpoint za import: XML -> DB
app.post('/api/import', async (req, res) => {
    let connection;
    try {
        const xmlData = req.body; // XML poslan sa frontenda
        connection = await oracledb.getConnection(dbConfig);

        // Pozivamo PL/SQL proceduru
        await connection.execute(
            `BEGIN XXXXX.IMPORT_ADRESE_XML(:xml); END;`,
            { xml: { val: xmlData, dir: oracledb.BIND_IN, type: oracledb.STRING } }
        );
        res.send({ status: "Uspješno", message: "Podaci iz XML-a su uneseni u bazu!" });
    } catch (err) {
        res.status(500).send({ status: "Greška", message: err.message });
    } finally {
        if (connection) await connection.close();
    }
});

app.listen(3000, () => console.log('Server radi na http://localhost:3000'));