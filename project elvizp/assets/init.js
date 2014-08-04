 initBullets();
 d3.select("body").append("p");

 var carData = {};
 var currentProgress = new Array();
 var currentRoute = new Array();
 var currentEstimation = {};
 var vizData = {};

 var svg = d3.select("body").append("svg")
 .attr("width", width + margin.left + margin.right)
 .attr("height", height + margin.top + margin.bottom)
 .attr("class", "linecanvas")
 .append("g")
 .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

/**
  Request loop to poll cardata from the CarData class.
 */
function requestLoop(){
  carData = JSON.parse(CarData.toJson(true));
  d3.select("body p").html(carData.speed +", "+carData.soc);
  // alert(route_distance);
  evenergy.speed(carData.speed,"kmh").distance(route_distance,"m").acceleration(carData.acceleration);

  var current_consumption = evenergy.energy();

  bullet_data[1]["measures"][1] = current_consumption;
  //alert(bullet_svg);
  bullet_svg.call(chart.duration(10));

  incrementProgress(carData);
}
