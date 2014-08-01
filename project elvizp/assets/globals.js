var margin = {top: 10, right: 10, bottom: 10, left: 10},
width = window.innerWidth/1.5 - 50 - margin.left - margin.right,
height = window.innerHeight/1.5 - 50 - margin.top - margin.bottom;

/*

  Vehicle information.

*/
vehicle = {
  cd:0.29,
  cr:0.012,
  area:2.7435,
  mass:1521
}

evenergy.config(vehicle);

var route_distance;
var travel_time;
var route_energy;

var bullet_data = [
  {"css":"current", "title":"Current energy","subtitle":"kWh/km","ranges":[0.08,0.11,0.2],"measures":[0.03,0.01,0.12,0.16,0.18],"markers":[0.1]},
    {"css":"bullet","title":"Total energy","subtitle":"kWh","ranges":[10,15,20],"measures":[2.5,13],"markers":[12]},
   {"css":"bullet","title":"Distance","subtitle":"km","ranges":[80,95,100],"measures":[0,0],"markers":[51]}
];

