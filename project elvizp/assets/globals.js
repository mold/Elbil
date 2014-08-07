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
var route_distance = 0;
var route_time = 0;
var route_energy = 0;
var start_energy = 0;
var consumed_energy = 0;
var scale = 1;
var consum_limit_data = [{energy: 0.3, distance: 0}, {energy: 0.3, distance: 12000}];
var consum_avg_data = [{energy: 0.5, distance: 0}, {energy: 0.5, distance: 12000}];
var consum_current_data = [{energy: 0, distance: 0}, {energy: 0, distance: 12000}];


var avg_est_consump;

var bullet_data = [
  {"css":"current", "title":"Consumption","subtitle":"kWh/km","ranges":[0.08,0.11,0.2],"measures":[0.1],"markers":[0.1]},
  {"css":"bullet","title":"Route energy","subtitle":"kWh","ranges":[10,15,20],"measures":[13],"markers":[12]},
];

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}
