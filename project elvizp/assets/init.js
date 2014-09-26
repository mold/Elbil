/*
  Vehicle information.
*/
var vehicle = {
  cd:0.29,
  cr:0.012,
  area:2.7435,
  mass:1521
};

evenergy.config(vehicle);

/*

  Resets visualization.

*/
function reset(){ 
  if(linechart === undefined) 
    return;
  else
  	linechart.reset();
}

/*

  Updates the route data used in the visualization

*/
function updateRoute(route){
  if(linechart !== undefined){
    linechart.setRoute(route);
  }
  if(sparklines !== undefined){
    sparklines.setRoute(route);
  }
}

/*
  Updates the visualization with a new estimation.
*/
function updateEstimation(est){
  if(linechart !== undefined){
    linechart.setValues(est);
  }
}

/*
  Adds a data point to the progress chart-
*/
function addProgress(distance, cd){
  if(linechart === undefined || !linechart.ready()){
    return;
  }
  linechart.addProgress(distance, cd);
  //linechart.updateProgress(cd);
  linechart.updateLimits(cd);
  sparklines.updateProgress(linechart.getProgress());

}

/**
 *  Method to update visualization with new CarData contained
 *  in the parameter cd.
 */
// var t = Date.now();
function updateData(cd){
  if(linechart === undefined || !linechart.ready()){
    return;
  }

  // cd.time = (Date.now() - t) / 1000;
  //cd.speed = 200;
  // t = Date.now();
  //d3.select("body p").html(cd.speed +" km/h, "+cd.soc+"%, "+cd.capacity+" kWh, "+ cd.time +" s");
  //alert(cd.time);
  linechart.updateLimits(cd);
  // linechart.updateProgress(cd);

  if(sparklines === undefined || !sparklines.ready())
    return;

  sparklines.updateProgress(linechart.getProgress());
  //console.log(y.domain());
  //carData.speed = 50;
}