'use strict';

var temp = document.getElementById("temp");
var humid = document.getElementById("humid");
var light = document.getElementById("light");
var soil = document.getElementById("soil");

var soilAutoSwitch = document.getElementById("moisture-auto");
var lightAutoSwitch = document.getElementById("light-auto");
var soilManSwitch = document.getElementById("moisture-man");
var lightManSwitch = document.getElementById("light-man");
var pumpCheckBox = document.getElementById("pump");
var ledCheckBox = document.getElementById("led");

var dbref = firebase.database().ref("/data");
var pumpRef = firebase.database().ref("/pump");
var ledRef = firebase.database().ref("/led");

dbref.limitToLast(1).on('child_added', function(snapshot) {
    let message = snapshot.val();
    temp.textContent = message.temperature.toFixed(2);
    humid.textContent = message.humidity.toFixed(2);
    light.textContent = message.light;
    soil.textContent = message.moisture;

    chart.data.labels.push("I");
    chart.data.datasets[0].data.push(message.temperature);
    chart.data.datasets[1].data.push(message.humidity);
    chart.data.datasets[2].data.push(message.light);

    if(chart.data.datasets[0].data.length > 20) {
      chart.data.labels.shift();
      chart.data.datasets[0].data.shift();
      chart.data.datasets[1].data.shift();
      chart.data.datasets[2].data.shift();
    }

    chart.update();
});


// Actuator checkbox toggled
function actuatorToggle(element) {
    if(element.checked) {
      element.parentElement.style.backgroundColor="#5d78ff";
      switch (element.id) {
        case "pump":
          pumpRef.update({"activation":1});
          break;
        case "led":
          ledRef.update({"activation":1});
          break;
        default:
          break;
      }
    } else {
      element.parentElement.style.backgroundColor="#e6e6e6";
      switch (element.id) {
        case "pump":
          pumpRef.update({"activation":0});
          break;
        case "led":
          ledRef.update({"activation":0});
          break;
        default:
          break;
      }
    }
  }

pumpRef.child('status').on('value', function(snapshot) {
  let query = snapshot.val();
  if(query == 0) {
    soilAutoSwitch.checked = true;
    soilManSwitch.checked = false;
    pumpCheckBox.checked = false;
    pumpCheckBox.parentElement.style.backgroundColor="#e6e6e6";
    pumpCheckBox.disabled = true;
    pumpRef.update({"activation":0});
  }
  else if(query == 1) {
    soilAutoSwitch.checked = false;
    soilManSwitch.checked = true;
    pumpCheckBox.disabled = false;
  }
});

ledRef.child('status').on('value', function(snapshot) {
  let query = snapshot.val();
  if(query == 0) {
    lightAutoSwitch.checked = true;
    lightManSwitch.checked = false;
    ledCheckBox.checked = false;
    ledCheckBox.parentElement.style.backgroundColor="#e6e6e6";
    ledCheckBox.disabled = true;
    ledRef.update({"activation":0});
  }
  else if(query == 1) {
    lightAutoSwitch.checked = false;
    lightManSwitch.checked = true;
    ledCheckBox.disabled = false;
  }
});

pumpRef.child('activation').on('value', function(snapshot) {
  let query = snapshot.val();
  if(query == 0) {
    pumpCheckBox.checked = false;
    pumpCheckBox.parentElement.style.backgroundColor="#e6e6e6";
  }
  else if(query == 1) {
    pumpCheckBox.checked = true;
    pumpCheckBox.parentElement.style.backgroundColor="#5d78ff";
  }
});

ledRef.child('activation').on('value', function(snapshot) {
  let query = snapshot.val();
  if(query == 0) {
    ledCheckBox.checked = false;
    ledCheckBox.parentElement.style.backgroundColor="#e6e6e6";
  }
  else if(query == 1) {
    ledCheckBox.checked = true;
    ledCheckBox.parentElement.style.backgroundColor="#5d78ff";
  }
});

function autoMoisture() {
  if(soilAutoSwitch.checked) {
    pumpRef.update({"status":0, "activation":0});
  }
  else {
    pumpRef.update({"status":1});
  }
}

function manMoisture() {
  if(soilManSwitch.checked) {
    pumpRef.update({"status":1});
  }
  else {
    pumpRef.update({"status":0});
  }
}

function autoLight() {
  if(lightAutoSwitch.checked) {
    ledRef.update({"status":0, "activation":0});
  }
  else {
    ledRef.update({"status":1});
  }
}

function manLight() {
  if(lightManSwitch.checked) {
    ledRef.update({"status":1});
  }
  else {
    ledRef.update({"status":0});
  }
}

// // Show data by day, month, or year in graph
// function selectTypeOfObs(evt, chosentype) {
//   let alltypes = document.getElementsByClassName('day-month-year');
//   let currentroom = document.getElementById("current-room").textContent;
//   let tempdata = [], humiddata = [], co2data = [], timestamp = [];

//   for(let i = 0; i < alltypes.length; i++) {
//     alltypes[i].style.color = "#646c9a";
//     alltypes[i].style.backgroundColor = "#f0f0f0";
//   }

//   chosentype.style.color = "#5867dd";
//   chosentype.style.backgroundColor = "#f0f3ff";

//   firebase.firestore().collection("data"+currentroom[5]).orderBy('timestamp', 'desc').get().then(function(querySnapshot) {
//     querySnapshot.forEach(function(doc) {
//       tempdata.push(doc.data().temperature);
//       humiddata.push(doc.data().humidity);
//       co2data.push(doc.data().co2);
//       timestamp.push(doc.data().timestamp);
//     });

//     if (chosentype.id == "day-observation") {
//       let dataday = 0;    // the number of data document of lastest day
//       let lastdate = timestamp[0].substring(0, 10);
//       chart.data.labels.length = 0;
//       chart.data.datasets[0].data.length = 0;
//       chart.data.datasets[1].data.length = 0;
//       chart.data.datasets[2].data.length = 0;
//       while (dataday < timestamp.length && timestamp[dataday].substring(0, 10) == lastdate) dataday++;
//       for (let i = dataday-1; i >= 0; i--) {
//         chart.data.labels.push(timestamp[i].substring(13));
//         chart.data.datasets[0].data.push(tempdata[i]);
//         chart.data.datasets[1].data.push(humiddata[i]);
//         chart.data.datasets[2].data.push(co2data[i]);
//         chart.update();
//       }
//     }
//     else if (chosentype.id == "month-observation") {
//       let datamonth = 0;  // the number of data document of the latest month
//       let lastmonth = timestamp[0].substring(0,7);
//       chart.data.labels.length = 0;
//       chart.data.datasets[0].data.length = 0;
//       chart.data.datasets[1].data.length = 0;
//       chart.data.datasets[2].data.length = 0;
//       while (datamonth < timestamp.length && timestamp[datamonth].substring(0, 7) == lastmonth) datamonth++;
//       for (let i = datamonth-1; i >= 0; i--) {
//         chart.data.labels.push(timestamp[i].substring(5,10));
//         chart.data.datasets[0].data.push(tempdata[i]);
//         chart.data.datasets[1].data.push(humiddata[i]);
//         chart.data.datasets[2].data.push(co2data[i]);
//         chart.update();
//       }
//     }
//     else {
//       let datayear = 0;   // the number of data document of the latest year
//       let lastyear = timestamp[0].substring(0, 4);
//       chart.data.labels.length = 0;
//       chart.data.datasets[0].data.length = 0;
//       chart.data.datasets[1].data.length = 0;
//       chart.data.datasets[2].data.length = 0;
//       while (datayear < timestamp.length && timestamp[datayear].substring(0, 4) == lastyear) datayear++;
//       for (let i = datayear-1; i >= 0; i--) {
//         chart.data.labels.push(timestamp[i].substring(0,7));
//         chart.data.datasets[0].data.push(tempdata[i]);
//         chart.data.datasets[1].data.push(humiddata[i]);
//         chart.data.datasets[2].data.push(co2data[i]);
//         chart.update();
//       }
//     }
//   });

// }

var ctx = document.getElementById('chart-3').getContext('2d');
var chart = new Chart(ctx, {
  // The type of chart
  type: 'line',
  // Data of chart
  data: {
    labels: [],
    datasets: [{
        label: 'Temperature',
        borderColor: '#fa7ba5',
        data: [],
        fill: false,
    },  {
        label: 'Humidity',
        borderColor: '#21ecec',
        data: [],
        fill: false,
    },  {
        label: 'Light Intensity',
        borderColor: '#f8d451',
        data: [],
        fill: false,
    }]
  },
  // Configuration options
  options: {
    scales: {
      xAxes: [{
          gridLines: {
              borderDash: [8, 4],
          }
      }],

      yAxes: [{
          gridLines: {
              borderDash: [8, 4],
          }
      }]
    },
    legend: {
      align: 'end',
      labels: {
        padding: 10
      }
    }
  },

  plugins: [{
    beforeInit: function(chart) {
      chart.legend.afterFit = function() {
        this.height = this.height + 10;
      };
    }
  }]
});