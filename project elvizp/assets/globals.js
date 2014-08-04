/*

  Vehicle information.

*/
var vehicle = {
  cd:0.29,
  cr:0.012,
  area:2.7435,
  mass:1521
}

evenergy.config(vehicle);

var travelled_distance = 0;
var route_distance;
var route_time;
var route_energy;
var start_energy;
var consumed_energy = 1;
var scale = 1;

var bullet_data = [
  {"css":"current", "title":"Consumption","subtitle":"kWh/km","ranges":[0.08,0.11,0.2],"measures":[0.1],"markers":[0.1]},
  {"css":"bullet","title":"Route energy","subtitle":"kWh","ranges":[10,15,20],"measures":[13],"markers":[12]},
  {"css":"bullet","title":"Distance","subtitle":"km","ranges":[80,95,100],"measures":[0.1],"markers":[51]}
];

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}
