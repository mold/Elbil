(function() {
    var margin = {top: 10, right: 15, bottom: 35, left: 50},
    width = window.innerWidth - 15 - margin.left - margin.right,
    height = 0.1 * window.innerHeight - margin.top - margin.bottom;
    
    var svg = d3.select("body")
    	.append("svg")
    		.attr("width", width + margin.left + margin.right)
    		.attr("height", height + margin.top + margin.bottom)
  			.attr("class", "linecanvas")
    	.append("g")
    		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    var x = d3.scale.linear()
      .range([0, width]);

    var y = d3.scale.linear()
      .range([0, height]);
    
    var y_elevation = d3.scale.linear()
      .range([0, height]);
    
    var xAxis = d3.svg.axis()
      .scale(x)
      .orient("bottom");

    var speedAxis = d3.svg.axis()
      .scale(y)
      .orient("left");

    var elevationAxis = d3.svg.axis()
      .scale(y)
      .orient("left");

    var elevation_curve = d3.svg.line()
      .x(function(d) { return x(d.distance/1000); })
      .y(function(d) { return y(d.elevation); }) 
      .interpolate("basis");

    var elevation_area = d3.svg.area()
      .interpolate("basis")
      .x(function(d) { return x(d.distance/1000); })
      .y1(function(d) { return y_elevation(d.elevation); });

    var speed_curve = d3.svg.line()
      .x(function(d) { return x(d.distance/1000); })
      .y(function(d) { return y(d.speed); }) 
      .interpolate("basis");


    var elevation_curve = d3.svg.line()
      .x(function(d) { return x(d.distance/1000); })
      .y(function(d) { return y_elevation(d.elevation); })
      .interpolate("basis");


    var progress = [];
    var isReady = false;

    function SparkLines(){

    }

  	SparkLines.prototype.ready = function(){
  		return isReady;
  	}

  	/*

	
	
  	*/
  	SparkLines.prototype.updateProgress = function(progress){

  		//Rescale y-axis
  		var max_y = Math.max(y.domain()[1], progress[progress.length-1].speed + 10);
  		var min_y = Math.min(y.domain()[0], progress[progress.length-1].speed - 10);
 
  		if(progress[progress.length-1].speed < min_y)
  			min_y = progress[progress.length-1].speed;
	
		console.log("domain: "+min_y+" ->" + max_y);
 
		y.domain([min_y, max_y]);

		line_speed
        	.datum(progress)
        	.attr("class", "progressLine")
        	.attr("id", "limit")
        	.attr("d", speed_curve);

        rescale();


  	}

  	function rescale() {
  		svg.select("#speed_est")
  			.attr("d", speed_curve);

		svg.select("#elev_est")
  			.attr("d", elevation_curve);

  		line_speed
        	.attr("d", speed_curve);
  	}

  	SparkLines.prototype.setRoute = function(values){
  		var total_distance = d3.sum(values, function(d) { return d.distance.value/1000; });
  		
  		x.domain([0, total_distance]);
    	y.domain(d3.extent(values, function(d) { return 3.6 * d.distance.value / d.duration.value}));
    	y_elevation.domain(d3.extent(values, function(d) { return d.end.elevation}));

    	data = [];
    	var dist = 0;
    	var elem = {};
    		elem.distance = dist; 
    		elem.elevation = values[0].end.elevation;
    		elem.speed = 3.6 * values[0].distance.value/values[0].duration.value;
    	dist += values[0].distance.value;
    		
    	data.push(elem);
    	for(var i=0; i<values.length; i++){
    		elem = {};
    		elem.distance = dist; 
    		elem.elevation = values[i].end.elevation;
    		elem.speed = 3.6 * values[i].distance.value/values[i].duration.value;
    		dist += values[i].distance.value;
    		data.push(elem); 
    		console.log("elem"+i+": distance: "+elem.distance+", speed: "+elem.speed+", elevation: "+elem.elevation);
    	
    	}

    	line_speed = svg.append("path");

        //Speed est.
    	svg.append("path")
        	.datum(data)
        	.attr("class", "limit curr line")
        	.attr("id", "speed_est")
        	.attr("d", speed_curve);

        //Elevation est.
       svg.append("path")
        	.datum(data)
        	.attr("class", "limit avg line")
        	.attr("id", "elev_est")
        	.attr("d", elevation_curve);

        isReady = true;
  	}

  	var sparklines = window.sparklines = new SparkLines();

    
})();