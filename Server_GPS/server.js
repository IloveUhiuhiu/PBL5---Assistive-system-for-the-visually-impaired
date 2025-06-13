const express = require("express");
const cors = require("cors");
const admin = require("firebase-admin");
const bodyParser = require("body-parser");
const app = express();

const serviceAccount = require("./firebase-key.json");

admin.initializeApp({
  credential:   admin.credential.cert(serviceAccount),
  //databaseURL: "https://smart-stick-d6e39-default-rtdb.asia-southeast1.firebasedatabase.app",
  databaseURL: "https://gpsapp-31a42-default-rtdb.firebaseio.com"
});

const db = admin.database();
const ref = db.ref("devices");
const ref2 = db.ref("fall");
app.use(bodyParser.json());
app.use(cors());


app.get("/", function (req, res) {
  res.send("Hello World");
});

app.put("/updategps/:deviceId",(req,res)=>{
  console.log("gps")
  const { latitude, longitude } = req.body;
  const deviceId=req.params.deviceId
  console.log(`\n[PUT] /update/${deviceId}`);
  console.log("Received Data:", req.body);
  
  if (!deviceId || !latitude || !longitude) {
    return res.status(400).send({ message: "Invalid params" });
  }
  ref.child(deviceId).update({
    latitude,
    longitude,
    time_stamp: Date.now()
  })
  res.status(200).send({ message: "Updated coordinate" });
})

app.put("/detectfall/:deviceId",(req,res)=>{
  console.log("fall")
  const {message} = req.body;
  const deviceId=req.params.deviceId
  console.log(`\n[PUT] /detectfall/${deviceId}`);
  console.log("Received Data:", req.body);
  ref2.child(deviceId).update({
    message,
    time_stamp: Date.now()
  })
   res.status(200).send({ message: "Updated fall" });
  
})


// console.log(`PORT: ${process.env.PORT}`);
// console.log(`HOST_NAME: ${process.env.HOST_NAME}`);


const port = Number(process.env.PORT) || 8888;
const hostname = process.env.HOST_NAME || "0.0.0.0";

app.listen(port, hostname, () => {
  console.log("Listening on port " + port);
});
