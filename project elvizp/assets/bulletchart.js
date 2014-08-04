
var margin = {top: 5, right: 40, bottom: 20, left: 120},
    bullet_width = 500 - margin.left - margin.right,
    bullet_height = 50 - margin.top - margin.bottom;

var chart = d3.bullet()
    .width(bullet_width)
    .height(bullet_height);

var bullet_svg;

function initBullets(){
  bullet_svg = d3.select("body").selectAll("svg")
        .data(bullet_data)
        .enter().append("svg")
    .attr("class", function(d,i) { return d.css; })
        .attr("width", bullet_width + margin.left + margin.right)
        .attr("height", bullet_height + margin.top + margin.bottom)
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .call(chart);

    var title = bullet_svg.append("g")
        .style("text-anchor", "end")
        .attr("transform", "translate(-6," + bullet_height / 2 + ")");

    title.append("text")
        .attr("class", "title")
        .text(function(d) { return d.title; });

    title.append("text")
        .attr("class", "subtitle")
        .attr("dy", "1em")
        .text(function(d) { return d.subtitle; });

    d3.selectAll("button").on("click", function() {
      bullet_svg.datum(randomize).call(chart.duration(1000)); // TODO automatic transition
    });
}

function updateBullet(cd){ 
 
  //Consumption 
  var cons = cd.consumption;//convert into number
  var soc = cd.soc;
  //console.log("cons "+cons);
  //console.log("soc "+soc);
  bullet_data[1]["ranges"][0] = Math.max(route_energy*2, cons);
  var range = soc < route_energy * 2 ? soc : route_energy*2;
  bullet_data[1]["ranges"][1] = range;
  range = soc < route_energy * 2 ? soc * 1.2: route_energy*2;
  bullet_data[1]["ranges"][2] = Math.max(range, cons);
  bullet_data[1]["measures"][0] = cons + consumed_energy;
  bullet_data[1]["markers"][0] = route_energy;

  //alert(carData.est_distance);
  //Distance 
  var ed = cd.est_distance/1000;
  var d = route_distance / 1000;
  bullet_data[2]["ranges"][0] = ed < d ? ed*0.8 : d;
  bullet_data[2]["ranges"][1] = ed < d ? ed : d;
  bullet_data[2]["ranges"][2] = d;

  bullet_data[2]["measures"][0] = travelled_distance/1000;
  bullet_data[2]["markers"][0] = d;

  //Consumption / km
  var e = isNumber(cd.kWhPerKm) ? cd.kWhPerKm : 0;
  bullet_data[0]["measures"][0] = e;
  bullet_data[0]["markers"][0] = route_energy/d;

  bullet_svg.call(chart.duration(100));
}

function randomize(d) {
  if (!d.randomizer) d.randomizer = randomizer(d);
  d.ranges = d.ranges.map(d.randomizer);
  d.markers = d.markers.map(d.randomizer);
  d.measures = d.measures.map(d.randomizer);
  return d;
}

function randomizer(d) {
  var k = d3.max(d.ranges) * .2;
  return function(d) {
    return Math.max(0, d + k * (Math.random() - .5));
  };
}
