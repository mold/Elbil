 initBullets();

 //d3.select("body").append("p");

 var currentProgress = new Array();
 var currentRoute = new Array();
 var currentEstimation = {};
 var vizData = {};
 var prevEst, prevSpeed;

/**
  Request loop to poll cardata from the CarData class.
 */
function requestLoop(){ 
  carData = JSON.parse(CarData.toJson(true));

  if(start_energy === undefined){
    start_energy = carData.soc;
  }
 
  consumed_energy = start_energy - carData.soc;
  //d3.select("body p").html(carData.speed +", "+carData.soc);
  evenergy.speed(carData.speed,"kmh").distance(route_distance,"m").soc(carData.soc);
  //carData.consumption = 3;
  carData.est_distance = carData.speed !== 0 ? evenergy.estimatedDistance() : 0;
  carData.consumption = evenergy.energy();
  carData.consumption = isNumber(carData.consumption) ? carData.consumption : 0;
  carData.kWhPerKm = evenergy.kWhPerKm();
  updateBullet(carData)
  incrementProgress(carData);
}
