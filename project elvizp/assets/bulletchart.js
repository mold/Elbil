
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
