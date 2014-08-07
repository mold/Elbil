  var margin = {top: 10, right: 15, bottom: 35, left: 35},
  width = window.innerWidth - 15 - margin.left - margin.right,
  height = 0.9 * window.innerHeight - margin.top - margin.bottom;
  
  var svg = d3.select("body").append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
  .attr("class", "linecanvas")
  .append("g")
  .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


  var x = d3.scale.linear()
    .range([0, width]);

  var y = d3.scale.linear()
    .range([height, 0]);

  var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

  var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");

  var simple_line = d3.svg.line()
    .x(function(d) { return x(d.distance); })
    .y(function(d) { return y(d.energy); }) 

  var energy_curve = d3.svg.line()
    .x(function(d) { return x(d.distance); })
    .y(function(d) { return y(d.energy); }) 
    .interpolate("basis");

  var area = d3.svg.area()
    .interpolate("basis")
    .x(function(d) { return x(d.distance); })
    .y1(function(d) { return y(d.energy); });


  var route_index = 0;  

 /**

   Sets the route on which the driver is currently on. routeData
   is assumed to be JSONified output from the RouteDataFetcher
   class. 

  **/
  function setRoute(routeData){
    travelled_distance = 0;
    travel_time = 0;
    route_index = 0;
    var sofar = 0;
    currentProgress.length = 0;
    currentRoute.length = 0;
    vizData.progress = currentProgress;

    for(var i=0; i<routeData.length; i++){
      sofar += routeData[i].distance.value;
      travel_time = routeData[i].duration.value;
      elem = routeData[i];
      elem.overallDistance = sofar;
      currentRoute.push(elem);
    }
    current_step = currentRoute[0];
    route_distance = sofar;
    
    consum_limit_data[1].distance = sofar;
    consum_avg_data[1].distance = sofar;
    consum_current_data[1].distance = sofar;

  }

/**

Cleans the svg and binds the data in values to the
line chart.

*/
function updateSeries(name, values){
  vizData.estimation = values;

  route_energy = d3.sum(vizData.estimation, function(d){
    return d.step/1000 * d.energy;
  });

  avg_est_consump = route_energy / (route_distance / 1000);
  //alert(avg_est_consump);
  consum_avg_data[0].energy = avg_est_consump;
  consum_avg_data[1].energy = avg_est_consump;
  //alert(avg_est_consump +" kWh/km");
  //alert(route_energy +" kWh");

  setupGraph();
}

/**
  * Initializes graph components.
  */
function setupGraph(){
    x.domain([0, d3.max(vizData.estimation, function(d) { return d.distance; })]);
    y.domain(d3.extent(vizData.estimation, function(d) { return d.energy; }));
    
    svg.selectAll("g").remove();
    svg.selectAll("path").remove();
    
    svg.append("g")
      .attr("class", "y axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
      .append("text")
      .attr("y", 6)
      .attr("dx", "2em")
      .attr("dy", "2em")
      .style("text-anchor", "start")
      .text("Distance (m)");
    
    svg.append("g")
      .attr("id", "yaxis")
      .attr("class", "y axis")
      .call(yAxis)
      .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dx", "-4em")
      .attr("dy", "-4.71em")
      .style("text-anchor", "end")
      .text("Energy consumption (kWh/km)");
    
    line = svg.append("path");

    line
      .datum(consum_limit_data)
      .attr("class", "limit line")
      .attr("id", "limit")
      .attr("d", simple_line);

    line_consum = svg.append("path");

    line_consum
      .datum(consum_current_data)
      .attr("class", "limit curr line")
      .attr("id", "limit")
      .attr("d", simple_line);

    svg.append("path")
      .datum(consum_avg_data)
      .attr("class", "limit avg line")
      .attr("id", "avglimit")
      .attr("d", simple_line);

    est_path = svg.append("path");

    est_path
      .datum(vizData.estimation)
      .attr("class", "est line")
      .attr("id", "estimation")
      .attr("d", energy_curve);

    svg.append("path")
      .datum(vizData.progress)
      .attr("id", "progressLine")
      .attr("class", "progressLine")
      .attr("d", energy_curve);
    
    //Difference chart stuff
     svg.append("clipPath")
      .attr("id", "clip-below")
     .append("path")
      .datum(vizData.progress)
      .attr("d", area.y0(height));

    svg.append("clipPath")
      .attr("id", "clip-above")
    .append("path")
      .datum(vizData.progress)
      .attr("d", area.y0(0));

    svg.append("path")
      .datum(vizData.progress)
      .attr("class", "area above")
      .attr("clip-path", "url(#clip-above)")
      .attr("d", area.y0(function(d) { return y(d.est_energy);}));

    svg.append("path")
      .datum(vizData.progress)
      .attr("class", "area below")
      .attr("clip-path", "url(#clip-below)")
      .attr("d", area);

    svg.append("path")
      .datum(vizData.progress)
      .attr("class", "line")
      .attr("d", energy_curve);

}

/*

  Realtime update of energy consumption.

*/
var t = Date.now();
function incrementProgress(data){
  if(route_index >= currentRoute.length)
    return;

  speed = scale * data.speed / 3.6;
  dist = speed * data.time;// meters
  travelled_distance += dist; 
 
  //alert(speed+" m/s, "+data.time+" s");

  if (travelled_distance > current_step.overallDistance) {
    route_index++;
  }
 
  current_step = currentRoute[route_index];
  
  evenergy.reset();
  evenergy.speed(speed/scale,"ms").distance(dist,"m").slope(current_step.slope);
  e = evenergy.kWhPerKm();

  if(isNumber(e)){
    var point = find_point_from_x(travelled_distance, est_path.node());
    var est_y = y.invert(point.y);
    // console.log("check: "+ travelled_distance +" m, "+e+" kwH/km, "+est_y+" kwH/km");
    consumed_energy += e * dist/1000;
    currentProgress.push({distance: travelled_distance, energy: e, est_energy: est_y});
  
    svg.select("#progressLine")
      .attr("d", energy_curve)
   
    svg.select("#clip-below path")
       .attr("d", area.y0(height));

     svg.select("#clip-above path")
       .attr("d", area.y0(0));

     svg.select(".area.above")
       .attr("d", area.y0(function(d) { return y(d.est_energy);}));

     svg.select(".area.below")
       .attr("d", area);
  }
}

/*

  Helper function for the realtime energy consumption estimation.
  Given a path node, determines an approximate point on the path
  from the given x value (assuming that the x scale has been
  defined.).

*/
function find_point_from_x(xval, path_node){
  var target = x(xval);
  var start = 0;
  var end = path_node.getTotalLength();
  var pos = path_node.getPointAtLength((start + end)/2);
  var breakpoint = 500;
  var i=0;
  //console.log("WOOP1: "+target);
  while(target < pos.x - 0.05 || target > pos.x + 0.05 ){
    pos = path_node.getPointAtLength((start + end)/2);
    if (target < pos.x) {
      end = (start + end)/2;
    } else {
      start = (start + end)/2;
    }
    if(i++ > breakpoint)
      break;
  }
  
  return pos;
}