var t = Date.now();

function reset(){ 
  if(linechart === undefined) 
    return;
  else
  	linechart.reset();
}

/**
 *  Method to update visualization with new CarData contained
 *  in the parameter cd.
 */

function updateData(cd){
  if(linechart === undefined || !linechart.ready()){
    return;
  }

  cd.time = (Date.now() - t) / 1000;
  cd.speed = 80;
  t = Date.now();
  d3.select("body p").html(cd.speed +" km/h, "+cd.soc+"%, "+cd.capacity+" kWh, "+ cd.time +" s");
  //alert(cd.time);
  linechart.updateLimits(cd);
  linechart.updateProgress(cd);

  //console.log(y.domain());
  //carData.speed = 50;
}
