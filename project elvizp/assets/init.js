 //initBullets();

 d3.select("body").append("p");

 var currentProgress = new Array();
 var currentRoute = new Array();
 var currentEstimation = {};
 var vizData = {};
 var prevEst, prevSpeed;

/**
  Request loop to poll cardata from the CarData class.
 */
// function requestLoop(){ 
//   if(travelled_distance > route_distance){
//     window.clearInterval(repeat);
//   }

//   carData = JSON.parse(CarData.toJson(true));
  
//   //carData.speed = 50;
//   //d3.select("body p").html(carData.speed +", "+carData.soc);
//   evenergy.speed(carData.speed,"kmh").distance(route_distance,"m").soc(carData.soc/100*16);
//   //carData.consumption = 3;
//   carData.est_distance = carData.speed !== 0 ? evenergy.estimatedDistance() : 0;
//   carData.consumption = evenergy.energy();
//   carData.consumption = isNumber(carData.consumption) ? carData.consumption : 0;
//   carData.kWhPerKm = evenergy.kWhPerKm();
//   updateBullet(carData)
//   incrementProgress(carData);
// }

d3.select("body").append("p");
var t = Date.now();
/**
 *  Method to update visualization with new CarData contained
 *  in the parameter cd.
 */
function updateData(cd){
  if(travelled_distance > route_distance || route_distance == 0){
    return;
  }

  cd.time = (Date.now() - t) / 1000;
  cd.speed = 80;
  t = Date.now();
  var energy_left = cd.soc/100 * cd.capacity;
  var avg_consump = energy_left / (route_distance / 1000);
  var curr_consump = consumed_energy / (travelled_distance / 1000);

  //alert("avg_consump: "+avg_consump);


  consum_limit_data[0].energy = avg_consump;
  consum_limit_data[1].energy = avg_consump;

  consum_current_data[0].energy = curr_consump;
  consum_current_data[1].energy = curr_consump;


  var new_top_y = Math.max(y.domain()[1], avg_consump+0.1);
  //new_top_y = Math.max(new_top_y, curr_consump+0.1);
  y.domain([y.domain()[0], new_top_y]);
  
  svg.select("#yaxis")
    .call(yAxis);
  
  svg.selectAll(".limit")
    .attr("d", simple_line);

  svg.select("#estimation")
    .attr("d", energy_curve);

  line
    .transition()
    .duration(250)
    .attr("d", simple_line);
 
  line_consum
    .transition()
    .duration(250)
    .attr("d", simple_line);
    //.transition()
    //.duration(250)

  
  console.log("avg_consump: "+ avg_consump);
  console.log("curr_consump: "+ curr_consump);
  
  //console.log(y.domain());
  //carData.speed = 50;
  d3.select("body p").html(cd.speed +" km/h, "+cd.soc+"%, "+cd.capacity+" kWh, "+ cd.time +" s");
  evenergy.speed(cd.speed,"kmh").distance(route_distance,"m").soc(energy_left); 
  //carData.consumption = 3;
  cd.est_distance = cd.speed !== 0 ? evenergy.estimatedDistance() : 0;
  cd.consumption = evenergy.energy();
  cd.consumption = isNumber(cd.consumption) ? cd.consumption : 0;
  cd.kWhPerKm = evenergy.kWhPerKm();
  // updateBullet(cd)
  incrementProgress(cd);
}
