/*
 Hachiman Digital Handscrolls
 Vertical Japanese Font PlugIn
 Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 All Rights Reserved.
 	
 http://bitgilde.de/

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 For further information on HyperImage visit http://hyperimage.ws/
 */

/* @author Jens-Martin Loebel */

var options = {
	'fontsize' : '14px',
	'chars' : 30,
	'lineInterval' : 0.4,
	'showcredit': false,
	'bar': true,
	'barForeColor': '#999',
	'barBackColor': '#eee',
	'multiCols': true,
	'splash': false,
	'auto': true  // do not modify
};


// --------------------------------------------------------------------------


function initHDH() {
	console.log("HDH: plugin init");
}

function formatHDHDiv(div) {
	var hdhId=1;
	$('#canvasTooltip .“hdh-vertical“, .hdh-vertical').each(function(index, item) {
		console.log(item);
		var idString = 'hdh_id'+hdhId;
		$(item).attr('id', idString);
		
		var params = {};
		params[idString] = options;
		h2vconvert.startConvert( params );


		hdhId++;
	});

}

// --------------------------------------------------------------------------
// init plugin

if (!reader.plugins) initPlugins();
// set hooks
reader.plugins.init.push(initHDH);
reader.plugins.tooltip.canvas.show.push(formatHDHDiv);
