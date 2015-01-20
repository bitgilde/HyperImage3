/*
   Copyright 2013 Leuphana Universität Lüneburg. All rights reserved.
   Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

function reportError(error) {
	$('#errorMessage').html(error);
    $( "#errorDialog" ).dialog({
    	draggable: false,
		resizable: false,
		closeOnEscape: false,
		dialogClass: 'no-close',
		title: "HyperImage Reader Critical Error",
		width: 550,
		height: 200,
		modal: true,
  });
	
	throw new Error(error);
}

function aboutReader() {
    $( "#aboutDialog" ).dialog({
    	draggable: false,
		resizable: false,
		title: reader.strings[reader.lang]['MENU_ABOUT'],
		width: 550,
		height: 415,
		modal: true,
		buttons: { 
			"Learn more...": function() { location.href="http://hyperimage.ws/";}, 
			"OK": function() { $( this ).dialog( "close" );}
		}
  });
}

function isValidUUID(uuid) {
	return /^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$/.test(uuid);
}

function getObject(item) {
	if ( item == null ) return null;
	if ( item.type == 'object' ) return item;
	if ( item.type == 'view' ) return item.parent;
	if ( item.type == 'inscription' ) return item.parent;
	if ( item.type == 'layer' ) return item.parent.parent;
	return null;
}

function parseImprint(data, success) {
	reader.last = reader.load;
	reader.load = 'imprint';
	reader.project.items['imprint'].content[reader.lang] = data;
	setGUI();
}

function loadItem(id, auxLoad, shouldSetGUI) {
	var auxload = auxload;

	// do imprint handling
	if ( id == 'imprint' ) if ( reader.project.items['imprint'].content[reader.lang] == null ) {
		$.get('resource/imprint_'+reader.lang+'.txt', parseImprint);
		return;
	}


	// determine if new item needs to be loaded
	/* FIXME check for different views of same object */
	if ( reader.load == id ) return;

	if ( !auxLoad ) {
		if ( reader.project.items[reader.load] == null || reader.project.items[reader.load].type != 'lita' ) reader.last = reader.load;
		reader.load = id;
	}
	if ( reader.project.items[id] == null ) {
		setLoadingIndicator(true);
		$.get(reader.path+id+'.xml', function (data, success) { parseItem(data, success, shouldSetGUI); });
	} else {
		console.log(id+" - already in memory");
		setGUI();
	}
}


function parseMDField (accessKey, field) {
	if ( field.firstChild == null ) return '';
	if ( accessKey != '' && reader.project.templates[accessKey] == null ) return '';
	
	if ( accessKey == "" || reader.project.templates[accessKey].richText == true ) {

		// parse "rich text" metadata field
		$(field).find('a').each(function(index, link) {
			$(link).attr('onclick', 'javascript:checkExternalLink(this);');
			$(link).attr('class', 'HIExternalLink' );
		});
		var htmlText = $(field).html();

		/*
		var lines = field.getElementsByTagName("line");
		for (var i=0; i < lines.length; i++)
			if ( lines[i].firstChild != null ) {
				var line = lines[i];
				if ( i > 0 ) htmlText += '<br>';
				$(line).contents().each(function(index, node) {
					if ( node.nodeType == 1 ) {
						switch (node.nodeName) {
							case "b":
								if ( node.firstChild != null ) {
									htmlText += '<b>';
									$(node).contents().each(function(index, insidenode) {
										if ( insidenode.nodeType == 1 && insidenode.nodeName == 'link' ) 
											htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+insidenode.getAttribute("ref")+'">'+insidenode.firstChild.nodeValue+'</a>';
										if ( insidenode.nodeType == 3 ) htmlText += insidenode.nodeValue;
									});
									htmlText += '</b>';
								}
								break;
							case "i":
								if ( node.firstChild != null ) {
									htmlText += '<i>';
									$(node).contents().each(function(index, insidenode) {
										if ( insidenode.nodeType == 1 && insidenode.nodeName == 'link' ) 
											htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+insidenode.getAttribute("ref")+'">'+insidenode.firstChild.nodeValue+'</a>';
										if ( insidenode.nodeType == 3 ) htmlText += insidenode.nodeValue;
									});
									htmlText += '</i>';
								}
								break;
							case "u":
								if ( node.firstChild != null ) {
									htmlText += '<u>';
									$(node).contents().each(function(index, insidenode) {
										if ( insidenode.nodeType == 1 && insidenode.nodeName == 'link' ) 
											htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+insidenode.getAttribute("ref")+'">'+insidenode.firstChild.nodeValue+'</a>';
										if ( insidenode.nodeType == 3 ) htmlText += insidenode.nodeValue;
									});
									htmlText += '</u>';
								}
								break;
							case "link":
								htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+node.getAttribute("ref")+'">'+node.firstChild.nodeValue+'</a>';
								break;
						}
					} else if ( node.nodeType == 3 ) htmlText += node.nodeValue;
				});
			} else if ( i > 0 ) htmlText += '<br>';
*/
		return htmlText;
	} else return field.firstChild.nodeValue;
}


function collectContent(items) {
	var contents = new Object();
	$(items).each(function (index, member) {
		var content = new HIContent(member.getAttribute("ref"));
		content.type = member.getAttribute("petalType");
		$(member).find("title").each (function (i, title) {
			if ( title.firstChild != null ) content.title[title.getAttribute("xml:lang")] = title.firstChild.nodeValue;
			else content.title[title.getAttribute("xml:lang")] = '';
		});
		if ( content.type == 'url' ) {
			if ( $(member).find("title")[0].firstChild != null ) content.title = $(member).find("title")[0].firstChild.nodeValue;
			else content.title = '';
		}
		$(member).find("meta").each (function (i, meta) {
			if ( meta.firstChild != null ) content.title[meta.getAttribute("xml:lang")] = meta.firstChild.nodeValue;
			else content.title[meta.getAttribute("xml:lang")] = '';
		});
		$(member).find("content").each (function (i, ins) {
			if ( ins.firstChild != null ) content.content[ins.getAttribute("xml:lang")] = ins.firstChild.nodeValue;
			else content.content[ins.getAttribute("xml:lang")] = '';
		});
		if ( member.getElementsByTagName("members").length > 0 )
			content.size = parseInt(member.getElementsByTagName("members")[0].getAttribute("size"));
		if ( member.getElementsByTagName("img").length > 0 )
			content.image = member.getElementsByTagName("img")[0].getAttribute("src");				
	
		contents[content.target] = content;
	});
	
	return contents;
}


function parseItem(data, success, shouldSetGUI) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);

	var itemType = data.getElementsByTagName("subject")[0].getAttribute("petalType");
	var id;
	var item = null;
	var unloaded = null;
	var viewID = null;
	var viewUUID = null;

	switch (itemType) {
		case "view":
			id = data.getElementsByTagName("object")[0].getAttribute("id");
			viewID = data.getElementsByTagName("subject")[0].getAttribute("ref");
			if ( reader.project.items[viewID] == null ) {
				item = new HIView(viewID);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				viewUUID = item.uuid;

				// parse view metadata
				titles = $(data).find("view > title");
				for (var i=0; i < titles.length; i++)
					if ( titles[i].firstChild != null ) item.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
					else item.title[titles[i].getAttribute("xml:lang")] = '';
				sources = $(data).find("view > source");
				for (var i=0; i < sources.length; i++)
					if ( sources[i].firstChild != null ) item.source[sources[i].getAttribute("xml:lang")] = sources[i].firstChild.nodeValue;
					else item.source[titles[i].getAttribute("xml:lang")] = '';
				annos = $(data).find("view > annotation");
				for (var i=0; i < annos.length; i++)
					item.annotation[annos[i].getAttribute("xml:lang")] = parseMDField('', annos[i]);
				
				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));
				// parse refs
				item.refs = collectContent($(data).find("references > reference"));
				// parse siblings
				if ( item.parent != null )
					item.parent.siblings = collectContent($(data).find("siblings > sibling"));
				else item.siblings = collectContent($(data).find("siblings > sibling"));
				
				// parse image files
				item.files['images'] = new Array();
				$(data).find('view > img').each(function (index, file) {					
					var newFile = new Object();
					newFile.width = parseInt(file.getAttribute("width"));
					newFile.height = parseInt(file.getAttribute("height"));
					newFile.href = file.getAttribute("src");
					if ( file.getAttribute("use") == 'thumb' ) item.files['thumb'] = newFile; 
					else item.files['images'].push(newFile);
				});
				// sort image files
				item.files['images'].sort(function(x, y) {
					return (x.width*x.height)-(y.width*y.height);
				});
				item.files['original'] = item.files['images'][item.files['images'].length-1];
				
				// parse layers
				item.sortedLayers = [];
				$(data).find('view > layer').each(function (index, xmlLayer) {					
					var layer = new HILayer(xmlLayer.getAttribute("id"));
					layer.uuid = xmlLayer.getAttribute("uuid");
					layer.color = xmlLayer.getAttribute("color");
					layer.opacity = parseFloat(xmlLayer.getAttribute("opacity"));
					layer.ref = xmlLayer.getAttribute("ref");
					layer.order = parseInt(xmlLayer.getAttribute("order"));
					// parse polygons
					$(xmlLayer).find("polygon").each(function(pindex, poly) { layer.polygons.push(poly.getAttribute("points")); });

					// parse layer title and annotation
					titles = $(xmlLayer).find("> title");
					for (var i=0; i < titles.length; i++)
						if ( titles[i].firstChild != null ) layer.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
						else layer.title[titles[i].getAttribute("xml:lang")] = '';
					annos = $(xmlLayer).find("> annotation");
					for (var i=0; i < annos.length; i++)
						layer.annotation[annos[i].getAttribute("xml:lang")] = parseMDField('', annos[i]);

					layer.parent = item;
					item.layers[layer.id] = layer;
					item.sortedLayers.push(layer);
					reader.project.items[layer.id] = layer;
					reader.project.items[layer.uuid] = layer;

				});
				// sort layers
				item.sortedLayers.sort(function(x, y) {
					return y.order-x.order;
				});
				reader.project.items[viewID] = item;
				reader.project.items[viewUUID] = item;
				
			}
		case "inscription":
			id = data.getElementsByTagName("object")[0].getAttribute("id");
			viewID = data.getElementsByTagName("subject")[0].getAttribute("ref");
			if ( reader.project.items[viewID] == null ) {
				item = new HIInscription(viewID);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				viewUUID = item.uuid;

				// parse inscription content
				contents = $(data).find("inscription > content");
				for (var i=0; i < contents.length; i++)
					item.content[contents[i].getAttribute("xml:lang")] = parseMDField('', contents[i]);
				
				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));
				// parse refs
				item.refs = collectContent($(data).find("references > reference"));
				// parse siblings
				if ( item.parent != null )
					item.parent.siblings = collectContent($(data).find("siblings > sibling"));
				else item.siblings = collectContent($(data).find("siblings > sibling"));
				
				reader.project.items[viewID] = item;
				reader.project.items[viewUUID] = item;
			}		
			
		case "object":
			id = data.getElementsByTagName("object")[0].getAttribute("id");
			
			if ( reader.project.items[id] == null ) {
				item = new HIObject(id);
				item.uuid = data.getElementsByTagName("object")[0].getAttribute("uuid");
				
				if ( viewID != null && reader.project.items[viewID] != null )
					item.siblings = reader.project.items[viewID].siblings;
				
				// parse object metadata
				var metadata = $(data).find("object > metadata");
				for (var i=0; i < metadata.length; i++) {
					var md = metadata[i];
					var lang = md.getAttribute("xml:lang");
					item.md[lang] = new Object();
					var records = md.getElementsByTagName("record");
					for (var recordID=0; recordID < records.length; recordID++) {
						var rec = records[recordID];
						var template = rec.getAttribute("template");
						var values = rec.getElementsByTagName("value");
						for (var valueID=0; valueID < values.length; valueID++)
							item.md[lang][template+"_"+values[valueID].getAttribute("key")] = parseMDField(template+"_"+values[valueID].getAttribute("key"), values[valueID]);
					}
				}
				item.defaultViewID = data.getElementsByTagName("standardView")[0].getAttribute("ref");
				reader.project.items[id] = item;
				reader.project.items[item.uuid] = item;
			}
			// check if object has unloaded view
			if ( reader.project.items[reader.project.items[id].defaultViewID] == null ) unloaded = reader.project.items[id].defaultViewID;
			else {
				reader.project.items[reader.project.items[id].defaultViewID].parent = reader.project.items[id]; // set object parent of view
				reader.project.items[id].siblings = reader.project.items[reader.project.items[id].defaultViewID].siblings;
				reader.project.items[id].views[reader.project.items[id].defaultViewID] = reader.project.items[reader.project.items[id].defaultViewID]; // attach view to object
			}
			// attach view to object if neccessary
			if ( viewID != null && reader.project.items[viewID].parent == null ) {
				reader.project.items[viewID].parent = reader.project.items[id];
				reader.project.items[id].views[viewID] = reader.project.items[viewID];
			}
						
			break;

		case "layer":
			id = data.getElementsByTagName("object")[0].getAttribute("id");
			layerID = data.getElementsByTagName("subject")[0].getAttribute("ref");
			unloaded = data.getElementsByTagName("view")[0].getAttribute("id");
			
			var xmlLayer = $(data).find('layer')[0];
			var layer = new HILayer(xmlLayer.getAttribute("id"));
			layer.uuid = xmlLayer.getAttribute("uuid");
			layer.color = xmlLayer.getAttribute("color");
			layer.opacity = parseFloat(xmlLayer.getAttribute("opacity"));
			layer.ref = xmlLayer.getAttribute("ref");

			// parse polygons
			$(xmlLayer).find("polygon").each(function(pindex, poly) { layer.polygons.push(poly.getAttribute("points")); });

			// parse layer title and annotation
			titles = $(xmlLayer).find("> title");
			for (var i=0; i < titles.length; i++)
				if ( titles[i].firstChild != null ) layer.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
				else layer.title[titles[i].getAttribute("xml:lang")] = '';
			annos = $(xmlLayer).find("> annotation");
			for (var i=0; i < annos.length; i++)
				layer.annotation[annos[i].getAttribute("xml:lang")] = parseMDField('', annos[i]);

			if ( reader.project.items[layer.id] == null ) {
				reader.project.items[layer.id] = layer;
				reader.project.items[layer.uuid] = layer;
			}
			break;

		case "text":
			id = data.getElementsByTagName("text")[0].getAttribute("id");
			if ( reader.project.items[id] == null ) {
				item = new HIText(id);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				// parse text metadata
				titles = $(data).find("text > title");
				for (var i=0; i < titles.length; i++)
					if ( titles[i].firstChild != null ) item.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
					else item.title[titles[i].getAttribute("xml:lang")] = '';
				content = $(data).find("text > content");
				for (var i=0; i < content.length; i++)
					item.content[content[i].getAttribute("xml:lang")] = parseMDField('', content[i]);

				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));
				// parse refs
				item.refs = collectContent($(data).find("references > reference"));
				
				reader.project.items[id] = item;
				reader.project.items[item.uuid] = item;
			}
			break;
		
		case "group":
			id = data.getElementsByTagName("group")[0].getAttribute("id");			
			if ( reader.project.items[id] == null ) {
				item = new HIGroup(id);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				// parse group metadata
				titles = $(data).find("group > title");
				for (var i=0; i < titles.length; i++)
					if ( titles[i].firstChild != null ) item.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
					else item.title[titles[i].getAttribute("xml:lang")] = '';
				annos = $(data).find("group > annotation");
				for (var i=0; i < annos.length; i++)
					item.annotation[annos[i].getAttribute("xml:lang")] = parseMDField('', annos[i]);
				
				// get group contents
				item.members = collectContent($(data).find("group > member"));

				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));
				// parse refs
				item.refs = collectContent($(data).find("references > reference"));
								
				reader.project.items[id] = item;
				reader.project.items[item.uuid] = item;
			}
			break;

		case "url":
			id = data.getElementsByTagName("url")[0].getAttribute("id");			
			if ( reader.project.items[id] == null ) {
				item = new HIURL(id);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				// parse URL metadata
				tempTitle = $(data).find("url > title")[0];
				if ( tempTitle != null && tempTitle.firstChild != null )
					item.title = tempTitle.firstChild.nodeValue; else item.title = '';
				tempAnno = $(data).find("url > annotation")[0];
				if ( tempAnno != null && tempAnno.firstChild != null )
					item.annotation = parseMDField('', tempAnno); else item.annotation = '';

				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));

				item.url = data.getElementsByTagName("url")[0].getAttribute("ref");
				reader.project.items[id] = item;
				reader.project.items[item.uuid] = item;
			}
			break;
		
		case 'lita':
			id = data.getElementsByTagName("lita")[0].getAttribute("id");			
			if ( reader.project.items[id] == null ) {
				item = new HILighttable(id);
				item.uuid = data.getElementsByTagName("subject")[0].getAttribute("uuid");
				// parse light table metadata
				var titles = $(data).find("lita > title");
				for (var i=0; i < titles.length; i++)
					if ( titles[i].firstChild != null ) item.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
					else item.title[titles[i].getAttribute("xml:lang")] = '';

				// parse light table
				var frames = $(data).find("lita > frame");
					for (var i=0; i < frames.length; i++) {
						var frame = new HIFrame(
							parseInt(frames[i].getAttribute("x")),
							parseInt(frames[i].getAttribute("y")),
							parseInt(frames[i].getAttribute("width")),
							parseInt(frames[i].getAttribute("height"))
						);
						frame.imagePos.x = parseInt($(frames[i]).find("frameContent")[0].getAttribute("x"));
						frame.imagePos.y = parseInt($(frames[i]).find("frameContent")[0].getAttribute("y"));
						frame.imagePos.width = parseInt($(frames[i]).find("frameContent")[0].getAttribute("width"));
						frame.imagePos.height = parseInt($(frames[i]).find("frameContent")[0].getAttribute("height"));
						frame.href = $(frames[i]).find("frameContent")[0].getAttribute("ref");
						item.frames[i] = frame;
						// frames[i].getAttribute("order") --> TODO preserve order --> PeTAL export XML is broken
					}
				var frameAnnotation = $(data).find("lita > frameAnn");
				if ( frameAnnotation.length > 0 ) {
					frameAnnotation = frameAnnotation[0];
					var fa = new Object();
					fa.annotation = new Object();
					fa.x = parseInt(frameAnnotation.getAttribute("x"));
					fa.y = parseInt(frameAnnotation.getAttribute("y"));
					fa.width = parseInt(frameAnnotation.getAttribute("width"));
					fa.height = parseInt(frameAnnotation.getAttribute("height"));
					if ( frameAnnotation.getAttribute('visible') == 'true' ) fa.visible = true;
					var annos = $(frameAnnotation).find("annotation");
					for (var i=0; i < annos.length; i++)
						fa.annotation[annos[i].getAttribute("xml:lang")] = parseMDField('', annos[i]);
					item.frameAnnotation = fa;
				}

				// parse back refs (sites)
				item.sites = collectContent($(data).find("sites > site"));
				// parse refs
				item.refs = collectContent($(data).find("references > reference"));				

				reader.project.items[id] = item;
				reader.project.items[item.uuid] = item;
			}
			break;

	}
	
	if ( unloaded != null ) {
		console.log("unloaded item: ", unloaded);
		loadItem(unloaded, true, shouldSetGUI);
	} else if ( shouldSetGUI ) setGUI();
}

function persistBookmarks() {
	try {
		if ( window.localStorage != null )
			window.localStorage["bookmarks_"+reader.project.id+"_"+location.host+location.pathname] = JSON.stringify(reader.project.bookmarks);
		} catch (e) {
			// silent fail
			console.log("persist error: ", e);
		}
}

function persistLocalTables() {
	try {
		if ( window.localStorage != null )
			window.localStorage["lighttables_"+reader.project.id+"_"+location.host+location.pathname] = JSON.stringify(reader.project.localLitas);
		} catch (e) {
			// silent fail
			console.log("persist error: ", e);
		}
}


function saveBookmark(id) {
	if ( id == null ) id = reader.load;

	var item = reader.project.items[id];
	var title = null;
	if ( item.type == 'object' ) item = reader.project.items[item.defaultViewID];
	if ( item.title != null && item.title[reader.lang] != null && item.title[reader.lang].length > 0 ) title = item.title[reader.lang];
	else title = item.id;
	
	reader.project.bookmarks.push(new HIBookmark(item.id, title));
	$('#bookmarkmenu').append('<li><a onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+item.id+'/">'+title+'</a></li>');
	// update menu GUI
	setMenuItem("deleteBookmarkLink", "javascript:deleteBookmark();", true);
	setMenuItem("deleteAllBookmarksLink", "javascript:deleteAllBookmarks();", true);

	persistBookmarks();
}

function deleteBookmark() {
	if ( reader.project.bookmarks.length == 0 ) return;
	if ( !$("#bookmarkmenu").children().last().hasClass("separator") ) {
		$("#bookmarkmenu").children().last().remove();
		reader.project.bookmarks.pop();
		if ( $("#bookmarkmenu").children().last().hasClass("separator") ) {
			setMenuItem("deleteBookmarkLink", "javascript:deleteBookmark();", false);
			setMenuItem("deleteAllBookmarksLink", "javascript:deleteAllBookmarks();", false);
		}
	}
	persistBookmarks();
}

function deleteAllBookmarks() {
	while ( $('#beginBookmarks').next().length )
		$('#beginBookmarks').next().remove();
	reader.project.bookmarks.length = 0;
	persistBookmarks();

	setMenuItem("deleteBookmarkLink", "javascript:deleteBookmark();", false);
	setMenuItem("deleteAllBookmarksLink", "javascript:deleteAllBookmarks();", false);
}

function getTitle(item) {
	if ( item == null ) return null;
	var title = item.id;

	if ( item.type == 'view' || item.type == 'layer' || item.type == 'group' || item.type == 'lita' || item.type == 'text' ) title = item.title[reader.lang];
	if ( item.type == 'url' ) title = item.title;
	if ( item.type == 'object' ) title = item.md[reader.lang][[reader.project.sortedFields[1]]];
	if ( item.type == 'inscription' ) {
		var element = document.createElement("div");
		$(element).html(item.content[reader.lang]);
		title = $(element).text();
	}
	if ( title == null || title.length == 0 ) title = item.id;	
	return title;
}

function setSearch(term) {
	$('#searchInput').val(term);
	performSearch('word');
}

function highlightWord(text, word) {
	var newText = new String(text);
	var regex = RegExp("\\b("+word+")", "gi");
	var indices = new Array();
	while ( (result = regex.exec(text)) ) {
		indices.unshift(result.index);
	}
	for (var i=0; i < indices.length; i++)
		newText = newText.substring(0, indices[i])+'<span class="highlight">'+text.substring(indices[i], indices[i]+word.length)+'</span>'+newText.substring(indices[i]+word.length, newText.length);

	return newText;
}

function performSearch(mode) {	
	var term = $('#searchInput').val();
	var advancedSearch = ( $('#searchOptions:visible').length > 0 ) ? true : false;

	if ( term != null && term.length > 1 ) {
		$('#basicSearchButton').hide();
		$('#extendedSearchButton').show();
		$('#searchResults').show();
		$('#searchOptions').hide();
		
		reader.search.term = term;
		reader.search.active = true;
		var searchTerm = term.toLowerCase();
		if ( reader.project.search[reader.lang] == null ) console.log("search delayed");
		else {
			var results = new Array();
			var resultTerms = new Array();
			$(Object.keys(reader.project.search[reader.lang].entries)).each(function(index, word) {
				if ( mode == null ? (word.substring(0, searchTerm.length) == searchTerm) : (word == searchTerm) ) {
					if ( !advancedSearch ) {
						// basic search
						resultTerms.push(word);
						$(Object.keys(reader.project.search[reader.lang].entries[word])).each(function(index, ref) {
							if ( $.inArray(ref, results) < 0 ) results.push(ref);
						});						
					} else {
						// advanced search
						$(Object.keys(reader.project.search[reader.lang].entries[word])).each(function(index, ref) {
							var inKey = false;
							for (var i=0; i < reader.project.search[reader.lang].entries[word][ref].length; i++) {
								var key = reader.project.search[reader.lang].entries[word][ref][i];
								if ( $('#'+reader.project.search[reader.lang].subByNumber[key]+'_check').is(':checked') )  inKey = true;
							}
							if ( inKey && $.inArray(ref, results) < 0 ) results.push(ref); // add result
							if ( $.inArray(word, resultTerms) < 0 ) resultTerms.push(word);
						});
						
					}
					
				}
			});
			
			$('#searchResultsContent').empty();
			// fallback compatibility for missing I18N in old flash reader
			var resultText = (reader.strings[reader.lang]['SEARCH_RESULT_BASIC'] == null) 
				? 'Fundstellen bei der Standardsuche nach' 
				: reader.strings[reader.lang]['SEARCH_RESULT_BASIC'];
			var advResultText = (reader.strings[reader.lang]['SEARCH_RESULT_ADVANCED'] == null) 
				? 'Fundstellen bei der erweiterten Suche nach' 
				: reader.strings[reader.lang]['SEARCH_RESULT_ADVANCED'];
			var andText = (reader.strings[reader.lang]['SEARCH_AND'] == null) 
				? 'und' 
				: reader.strings[reader.lang]['SEARCH_AND'];
			var noResultsText = (reader.strings[reader.lang]['SEARCH_NO_RESULTS'] == null) 
				? 'Kein Suchergebnis für' 
				: reader.strings[reader.lang]['SEARCH_NO_RESULTS'];

			if ( results.length > 0 ) {
				reader.search.resultTerms = resultTerms;
				reader.search.resultTerms = resultTerms.sort(function(a, b) {
					return  b.length - a.length;
				});
				
				
				if ( !advancedSearch ) $('#searchResultsContent').append(results.length+' '+resultText+' ');
				else $('#searchResultsContent').append(results.length+' '+advResultText+' ');
				for (var i=0; i < resultTerms.length; i++) {
					if ( resultTerms.length > 1 ) if ( i == (resultTerms.length-1) ) $('#searchResultsContent').append(' '+andText+' ');
					else if (i > 0) $('#searchResultsContent').append(', ');
					$('#searchResultsContent').append('"<a href="javascript:setSearch(\''+resultTerms[i]+'\');">'+resultTerms[i]+'</a>"');
				}
				$('#searchResultsContent').append(':<br><br>');
				$(results).each(function(index, result) {
					if ( reader.project.items[result] != null ) {
						var item = reader.project.items[result];
						var title = getTitle(item);
						if ( title != null && title.length > 31 ) title=title.substring(0,31)+"...";
						$('#searchResultsContent').append('<a id="'+result+'_result" onclick="javascript:reader.fromSearch = true;checkExternalLink(this);" class="HIExternalLink" href="#'+result+'/">'+title+'</a><span> '+reader.strings[reader.lang][searchTypeKeys[item.type]]+'</span><br>');
					} else {
						$('#searchResultsContent').append('<a id="'+result+'_result" onclick="javascript:reader.fromSearch = true;checkExternalLink(this);" class="HIExternalLink" href="#'+result+'/">'+result+'</a><span>&nbsp;</span><br>');
						$.get(reader.path+result+'.xml', function (data, success) {
							parseItem(data, success, false);
							var item = reader.project.items[result];
							var title = getTitle(item);
							if ( title != null && title.length > 31 ) title=title.substring(0,31)+"...";
							$('#'+result+'_result').text(title);
							$('#'+result+'_result').next().empty().text(' '+reader.strings[reader.lang][searchTypeKeys[item.type]]);
							setLoadingIndicator(false);
						});
					}
				});			
			} else {
				$('#searchResultsContent').append(noResultsText+' "'+term+'".');
				reader.search.resultTerms = new Array();
			}

			reader.search.active = false;
		}
	}
}

/* open external links in new window without alerting pop-up blockers */
function checkExternalLink(link) {
	var href = typeof(link) == 'string' ? link : link.getAttribute('href');
	var xmlData = null;
	if ( href.substring(0,2) == '#U' ) {
		href = href.substring(1, href.length);
		if ( href.substring(href.length-1, href.length) == '/' ) href = href.substring(0,href.length-1);
		if ( reader.project.items[href] == null ) {		
			$.ajax({ url: reader.path+href+'.xml',
				async: false,
				data: null,
				dataType: 'xml',
				success: function(data) { xmlData = data; }
			});
			if ( xmlData != null ) parseItem(xmlData, 'success');
		}

		if ( reader.project.items[href] != null ) {
			window.open(reader.project.items[href].url, '_newtab');
			event.preventDefault();
			return true;
		}
	}
	return false;
}

function attachExternalLinkHandler(element, item) {
	var filetype = item.url.substring(item.url.lastIndexOf('.')+1, item.url.length).toLowerCase();
	switch (filetype) {
		case 'mp3':
			 $(element).mb_miniPlayer({
			 	width: 0,
			 	inLine: true,
			 	addShadow: false,
			 	id3: false,
			 	skin: "black",
			 	playAlone: true,
			 	volume: 100,
			 	mp3: item.url
			 });
			break;

		case 'ogg':
			 $(element).mb_miniPlayer({
			 	width: 0,
			 	inLine: true,
			 	addShadow: false,
			 	id3: false,
			 	skin: "black",
			 	playAlone: true,
			 	volume: 100,
			 	ogg: item.url
			 });
			break;

			
		default:
			break;
	}
	
}

function scanExternalLinks() {
	$('.HIExternalLink').each(function(index, item) {
		if ($(item).attr("href").substring(0,2) == '#U') {
			var href = $(item).attr("href").substring(1, $(item).attr("href").length);
			if ( href.substring(href.length-1, href.length) == '/' ) href = href.substring(0,href.length-1);

			if ( reader.project.items[href] == null ) {		
				$.ajax({ url: reader.path+href+'.xml',
					async: true,
					data: null,
					dataType: 'xml',
					success: function(data) { 
						parseItem(data, 'success');
						if ( reader.project.items[href] != null ) attachExternalLinkHandler(item, reader.project.items[href]);
					}
				});
			} else attachExternalLinkHandler(item, reader.project.items[href]);

			
		}
		
	});
}


function loadContentThumbnail(imgTag, indicatorTag, ref, type) {
	imgTag.attr('src', ref);
	imgTag.load(function(e) {indicatorTag.remove(); }); // remove loading indicator on image load complete
}

function displayContentList(contentsList) {
	setGUIMode(2);
	reader.contentsList = contentsList;

	// load contents list
	$('#groupList > li').remove(); // remove old content
	$("#groupView div[id^=tt-]").remove(); // remove old tooltips

	$(Object.keys(contentsList)).each(function(index, key) {
		var classType = '';
		if ( contentsList[key].type == 'object' || contentsList[key].type == 'view' || contentsList[key].type == 'layer' )
			classType = ' class="hasContentsContextMenu"';
		// don't display image for external URLs
		if ( contentsList[key].type == 'url' ) contentsList[key].image = null;
		
		var html = '<li id="cl-'+key+'"'+classType;
		if ( contentsList[key].image != null ) html += '>'; else html+=' class="text">'; 
		html += '<a onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#'+contentsList[key].target+'/">';
		if ( contentsList[key].image != null ) {
			html += '<div class="contentLoadingIndicator"></div>';
			html += '<img src="#" />';
		}
		else {
			html += '<span class="'+typeKeys[contentsList[key].type]+'">'+reader.strings[reader.lang][typeKeys[contentsList[key].type]]+'</span>';
			if ( contentsList[key].type == 'group' || contentsList[key].type == 'lightTable') html += " ("+contentsList[key].size+")";
			html += '<br /><br />';
		 	if ( contentsList[key].type != 'url' && contentsList[key].type != 'inscription' && contentsList[key].type != 'text') html += '"'+contentsList[key].title[reader.lang]+'"';
		 	else if ( contentsList[key].type == 'text' || contentsList[key].type == 'inscription' ) html += contentsList[key].content[reader.lang];
		 	else html += contentsList[key].title;
		}
		html += "</a></li>\n";
		$('#groupList').append(html);
		$('#cl-'+key).data('contentType', contentsList[key].type ); // store item type with element

		if ( classType.length == 0 ) $('#cl-'+key).bind('contextmenu', function(ev) { ev.preventDefault(); }); // disable browser context menu

		if ( contentsList[key].image != null ) {
			// attach loading indicator and load image thumbnail
			$('#cl-'+key+' .contentLoadingIndicator').activity({ segments: 12, width: 2, space: 1, length: 4, speed: 1.2 });
			loadContentThumbnail($('#cl-'+key+' img'), $('#cl-'+key+' .contentLoadingIndicator'), contentsList[key].image, contentsList[key].type);
		}

		// create content tooltips
		var tooltipDiv = '<div id="tt-cl-'+key+'" class="tooltip"><div class="tooltipContent">';
		tooltipDiv += '<span class="'+typeKeys[contentsList[key].type]+'">'+reader.strings[reader.lang][typeKeys[contentsList[key].type]]+'</span><br /><br />';
		if ( contentsList[key].type == 'url' && contentsList[key].title != null && contentsList[key].title.length > 0 ) tooltipDiv += contentsList[key].title+"<br />";
		else if ( contentsList[key].title[reader.lang] != null && contentsList[key].title[reader.lang].length > 0 ) tooltipDiv += contentsList[key].title[reader.lang]+"<br />";
		else tooltipDiv += 'ID '+contentsList[key].target+'<br />';
		tooltipDiv += '</div><div class="tooltipBackground">&nbsp;</div></div>';
		$('#groupView').append(tooltipDiv);

		$('#cl-'+key).tooltip({
			tip: "#tt-cl-"+key,
			relative: true,
			predelay: 300,
			delay: 0,
			position: 'center left',
			onBeforeShow: function(tt, pos) {
				$(tt.target).tooltip().getConf().offset[1] = Math.round(reader.pageX-pos.left)+64;
			},
			onBeforeHide: function(tt, pos) {
				tt.currentTarget.getConf().offset[1] = 0;
			},
		});
		 
	});
}

function addLayerToLightTable(layerID) {
	if ( layerID == null ) return;
	var layer = reader.project.items[layerID];
	var bounds = getLayerBounds(layerID);
	if ( layer == null ) return;
	var view = layer.parent;
	if ( view == null ) return;

	var layerWidth = (bounds.maxX - bounds.minX) * view.files['original'].width;
	var layerHeight = (bounds.maxY - bounds.minY) * view.files['original'].height;
	
	var factor = 128.0 / Math.max(layerWidth, layerHeight);
	var displayWidth = Math.ceil(view.files['original'].width * factor);
	var displayHeight = Math.ceil(view.files['original'].height * factor);
	var frameWidth = Math.min(128, displayWidth); var frameHeight = Math.min(128, displayHeight);
	var xPos = Math.min(0, Math.round(frameWidth / 2.0 - ((bounds.minX + bounds.maxX) * displayWidth / 2.0)));
	var yPos = Math.min(0, Math.round(frameHeight / 2.0 - ((bounds.minY + bounds.maxY) * displayHeight / 2.0)));

	var frame = new HIFrame(
		20+(16*reader.table.frames.length), 
		66+(16*reader.table.frames.length), 
		frameWidth, 
		frameHeight);
	frame.href = view.id;
	frame.imagePos.x = xPos;
	frame.imagePos.y = yPos;
	frame.imagePos.width = displayWidth;
	frame.imagePos.height = displayHeight;
	reader.table.frames.push(frame);
}

function addViewToLightTable(viewID) {
	if ( viewID == null ) viewID = reader.viewID;
	if ( viewID == null ) return;
	var view = reader.project.items[viewID];
	if ( view == null ) return;
	var frame = new HIFrame(
		20+(16*reader.table.frames.length), 
		66+(16*reader.table.frames.length), 
		view.files['thumb'].width, 
		view.files['thumb'].height);
	frame.href = view.id;
	frame.imagePos.x = 0;
	frame.imagePos.y = 0;
	frame.imagePos.width = view.files['thumb'].width;
	frame.imagePos.height = view.files['thumb'].height;
	reader.table.frames.push(frame);
}

function duplicateFrame() {
	var isAnnotation = false;
	var selectedFrame = $("#lighttableContent > div.ltSelected");
	if ( selectedFrame.length == 0 ) selectedFrame = null; else selectedFrame = selectedFrame[0];
	if ( selectedFrame != null ) isAnnotation = $(selectedFrame).hasClass('ltAnnotation');
	
	if ( selectedFrame != null && !isAnnotation ) {
		var frame = new HIFrame(
			$('#lighttableContent').scrollLeft() + $(selectedFrame).position().left+16,
			$('#lighttableContent').scrollTop() + $(selectedFrame).position().top+32+reader.zoom.yOffset,
			$(selectedFrame).width(),
			$(selectedFrame).height()
		);
		frame.href = $(selectedFrame).data('viewID');
		var img = $('#'+selectedFrame.id+' img');
		frame.imagePos.x = $(img).position().left;
		frame.imagePos.y = $(img).position().top;
		frame.imagePos.width = $(img).width();
		frame.imagePos.height = $(img).height();
		reader.table.frames.push(frame);
		var guiFrame = window.addFrame(frame);
		var sortedFrames = $.makeArray($('.ltFrame')).sort(function(a,b) {
				return (parseInt($(a).css("zIndex"),10) || 0) - (parseInt($(b).css("zIndex"),10) || 0);
		});
		min = parseInt($(sortedFrames[0]).css("zIndex"), 10) || 0;
		$(guiFrame).css("zIndex", (min + sortedFrames.length));
		
		window.syncTableToModel(); // sync changes
	}
}

function removeFrame() {
	var isAnnotation = false;
	var selectedFrame = $("#lighttableContent > div.ltSelected");
	if ( selectedFrame.length == 0 ) selectedFrame = null; else selectedFrame = selectedFrame[0];
	if ( selectedFrame != null ) isAnnotation = $(selectedFrame).hasClass('ltAnnotation');
	$(selectedFrame).remove();
	window.setLightTableMenuGUI();

	window.syncTableToModel(); // sync changes
}

function fitToFrame() {
	var isAnnotation = false;
	var selectedFrame = $("#lighttableContent > div.ltSelected");
	if ( selectedFrame.length == 0 ) selectedFrame = null; else selectedFrame = selectedFrame[0];
	if ( selectedFrame != null ) isAnnotation = $(selectedFrame).hasClass('ltAnnotation');
	var img = $('#'+selectedFrame.id+' img');
	var view = reader.project.items[$(selectedFrame).data('viewID')];
	var scale = Math.max( $(selectedFrame).width() / view.files['original'].width, ($(selectedFrame).height()-16) / view.files['original'].height );
	$(img).css('top', '0px'); $(img).css('left', '0px');
	$(img).width(view.files['original'].width*scale); $(img).height(view.files['original'].height*scale);
	$(selectedFrame).width(view.files['original'].width*scale); $(selectedFrame).height((view.files['original'].height*scale)+16);

	window.syncTableToModel(); // sync changes
}

function fitToThumb() {
	var isAnnotation = false;
	var selectedFrame = $("#lighttableContent > div.ltSelected");
	if ( selectedFrame.length == 0 ) selectedFrame = null; else selectedFrame = selectedFrame[0];
	if ( selectedFrame != null ) isAnnotation = $(selectedFrame).hasClass('ltAnnotation');
	var img = $('#'+selectedFrame.id+' img');
	var view = reader.project.items[$(selectedFrame).data('viewID')];
	$(img).css('top', '0px'); $(img).css('left', '0px');
	$(img).width(view.files['thumb'].width); $(img).height(view.files['thumb'].height);
	$(selectedFrame).width(view.files['thumb'].width); $(selectedFrame).height(view.files['thumb'].height+16);

	window.syncTableToModel(); // sync changes
}

function addAnnotation() {
	reader.table.frameAnnotation.visible = true;
	for (var i=0; i < reader.project.langs.length; i++)
		if (reader.table.frameAnnotation.annotation[reader.project.langs[i]] == null || reader.table.frameAnnotation.annotation[reader.project.langs[i]].length == 0)
			reader.table.frameAnnotation.annotation[reader.project.langs[i]] = '...';

	window.addFrame();
	window.setLightTableMenuGUI();
}

function removeAnnotation() {
	var annotation = $('.ltAnnotation');
	if ( annotation == null || annotation.length == 0 ) return;
	$('.ltAnnotation').remove();
	reader.table.frameAnnotation.visible = false;
	window.setLightTableMenuGUI();
}

function insertAnnotationLink() {
	var linkField = $('#insertLinkField');
	var selection = window.getSelection();
	linkField.val('');

	var link = document.createElement('a');
    var href = document.createAttribute('href');
    var clattr = document.createAttribute('class');
    clattr.nodeValue = 'HIExternalLink';
	link. setAttributeNode(href);
	link. setAttributeNode(clattr);
	var sel = window.getSelection();
	var range = null;
	if (sel.rangeCount) range = sel.getRangeAt(0);

	var diagButtons = {};
	diagButtons[reader.strings[reader.lang]['STR_CANCEL']] = function() { $( this ).dialog( "close" ); };
	diagButtons[reader.strings[reader.lang]['STR_OK']] = function() {
		if ( $.trim(linkField.val()).length > 0 ) {
           	href.nodeValue = "#"+$.trim(linkField.val());
           	if ( range != null ) {
				var text = $(range.startContainer).text().substring(range.startOffset, range.endOffset);
				link.appendChild(document.createTextNode(text));
    	       	range.deleteContents();
    	       	/* text link handler */
				$(link).click(function(ev) { followLiTaLink(this); });

				range.insertNode(link);
			}
		}
		$(this).dialog("close");
	};
	
	$( "#insertLinkDialog" ).dialog({
    	draggable: false,
		resizable: false,
		title: reader.strings[reader.lang]['MENULITA_INSERT_LINK'],
		width: 400,
		height: 170,
		modal: true,
		buttons: diagButtons
  	});
}


function returnFromLightTable() {
	if ( reader.project.items[reader.load].type == 'lita' ) {
		if ( reader.last != null )  location.hash = reader.last+'/'; else location.hash = reader.start+'/';
	} else setGUI();
}

function nameTable() {
	$('#tableNameInput').val(reader.table.title[reader.lang]);
    $( "#nameTableDialog" ).dialog({
    	draggable: false,
		resizable: false,
		title: reader.strings[reader.lang]['MENULITA_NAME'],
		width: 300,
		height: 175,
		modal: true,
		buttons: { 
			"Abbrechen": function() { $( this ).dialog( "close" ); }, 
			"OK": function() { 
				reader.table.title[reader.lang] = $('#tableNameInput').val();
				window.setLightTableMenuGUI();
				$( this ).dialog( "close" );
			} 
		}
  });
}

function showTableXML() {
	window.syncTableToModel();
	// construct XML --> QnD
	var xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	xmlText += "<petal id=\""+reader.project.id+"\">\n"	;
	xmlText += "  <subject ref=\"none\" petalType=\"lita\" />\n";
	xmlText += "  <lita id=\"none\">\n";
	for (var i=0; i < reader.project.langs.length; i++) {
		var title = '';
		if ( reader.table.title[reader.project.langs[i]] != null ) title = reader.table.title[reader.project.langs[i]];
		xmlText += "    <title xml:lang=\""+reader.project.langs[i]+"\">"+title+"</title>\n";	
	}
	$(reader.table.frames).each(function(index, frame) {
		xmlText += "    <frame order=\""+(index+1)+"\" x=\""+frame.x+"\" y=\""+frame.y+"\" width=\""+frame.width+"\" height=\""+frame.height+"\">\n";
		xmlText += "      <frameContent ref=\""+frame.href+"\" x=\""+frame.imagePos.x+"\" y=\""+frame.imagePos.y+"\" width=\""+frame.imagePos.width+"\" height=\""+frame.imagePos.height+"\" />\n";
		xmlText += "    </frame>\n";
	});
	xmlText += "<frameAnn visible=\""+
	reader.table.frameAnnotation.visible+
	"\" x=\""+reader.table.frameAnnotation.x+
	"\" y=\""+reader.table.frameAnnotation.y+
	"\" width=\""+reader.table.frameAnnotation.width+
	"\" height=\""+reader.table.frameAnnotation.height+"\" >\n";
	for (var i=0; i < reader.project.langs.length; i++) {
		xmlText += "      <annotation xml:lang=\""+reader.project.langs[i]+"\">\n";
		var anString = reader.table.frameAnnotation.annotation[reader.project.langs[i]];

		anString = anString.replace(/ onclick=\"javascript:checkExternalLink\(this\);\"/g, '');
		anString = anString.replace(/ class=\"HIExternalLink\"/g, '');
		anString = anString.replace(/<a href=/g, '<link ref=');
		anString = anString.replace(/<a  href=/g, '<link ref=');
		anString = anString.replace(/<\/a>/g, '</link>');
		anString = anString.replace(/<link ref=\"#/g, '<link ref="');
		var xmlString = '';
		$(anString.split('<br>')).each(function (index, line) {
			xmlString += '        <line>'+line+"</line>\n";
		});

		xmlText += xmlString;
		xmlText += "      </annotation>\n";
	}
	xmlText += "    </frameAnn>\n";
	xmlText += "  </lita>\n";
	xmlText += "</petal>\n";

	$('#tableXML').val(xmlText);
	var diagButtons = {};
	diagButtons[reader.strings[reader.lang]['STR_OK']] = function() { $( this ).dialog( "close" ); };
	$( "#showTableXMLDialog" ).dialog({
    	draggable: false,
		resizable: false,
		title: reader.strings[reader.lang]['MENULITA_XML_CLIPBOARD'],
		width: 500,
		height: 400,
		modal: true,
		buttons: diagButtons
  	});
	$('#tableXML').select();
}

function newLocalTable(displayTable) {
	reader.table = new HILighttable();
	// set titles
	for (var i=0; i < reader.project.langs.length; i++) {
		var title = '-';
		if ( reader.strings[reader.project.langs[i]] != null && reader.strings[reader.project.langs[i]]['FILE_UNTITLED'] != null ) 
			title = reader.strings[reader.project.langs[i]]['FILE_UNTITLED'];
		reader.table.title[reader.project.langs[i]] = title;
		reader.table.frameAnnotation.annotation[reader.project.langs[i]] = "...";
	}
	if ( displayTable ) displayLightTable();
}

function saveLocalTable() {
	window.syncTableToModel();
	reader.table.id = null;
	reader.table.sites = null;
	reader.table.refs = null;
	reader.project.localLitas.push(JSON.parse(JSON.stringify(reader.table)));
	persistLocalTables();
	$('#endLocalLightTables').before('<li><a href="javascript:loadLocalTable('+(reader.project.localLitas.length-1)+');">'+reader.project.localLitas[reader.project.localLitas.length-1].title[reader.lang]+'</a></li>');
	setMenuItem("deleteLocalTableLink", "javascript:deleteLocalTable();", true);
}

function deleteLocalTable() {
	if ( reader.project.localLitas.length == 0 ) return;
	reader.project.localLitas.pop();
	persistLocalTables();
	$('#endLocalLightTables').prev().remove();
	if ( reader.project.localLitas.length == 0 ) setMenuItem("deleteLocalTableLink", "javascript:deleteLocalTable();", false);
}

function loadLocalTable(index) {
	if ( index < 0 || index > reader.project.localLitas.length ) return;
	reader.table = JSON.parse(JSON.stringify(reader.project.localLitas[index]));
	displayLightTable();
}

function followLiTaLink(link) {
	location.hash = link.getAttribute('href')+'/';
}

function loadFrame(item) {
	$.get(reader.path+item.href+'.xml', function (data, success) { 
		parseItem(data, success, false);
		setLoadingIndicator(false);
		addFrame(item); // display frame
 	});
}

function loadFrameHiRes(id, view) {
	var hiRes = findHigherRes(view, $('#'+id+"_content img").attr('src'), $('#'+id+"_content img").width(), $('#'+id+"_content img").height());
	if ( hiRes != null ) {
		var newImg = new Image();
   		newImg.src = hiRes.href;
   		$('#'+id+'_loading').show();
   		newImg.onload = function() {
			$('#'+id+"_content img").attr('src', hiRes.href);
    		$('#'+id+'_loading').hide();
		};
	}
}

function displayLightTable() {
	var frameCount = 0;
	$('#lighttableContent > div').remove(); // clear old light table elements
	if ( reader.table == null ) return;
	setGUIMode(3);
	setMenuState();
	
	/* 
	 * light table specific functions 
	 */
	function syncTableToModel() {
		var frames = new Array();
		var sortedFrames = $.makeArray($('.ltFrame')).sort(function(a,b) {
				return (parseInt($(a).css("zIndex"),10) || 0) - (parseInt($(b).css("zIndex"),10) || 0);
			});
		$(sortedFrames).each(function(index, item) {
			if ( $(item).hasClass('ltAnnotation') ) {
				reader.table.frameAnnotation.x = $(item).position().left+$('#lighttableContent').scrollLeft();
				reader.table.frameAnnotation.y = $(item).position().top+$('#lighttableContent').scrollTop()+64;
				reader.table.frameAnnotation.width = $(item).width();
				reader.table.frameAnnotation.height = $(item).height()-16;				
				
				// annotation text --> QnD
				$(item).find(' .ltText').get(0).normalize();
				var serialized = $('#'+item.id+' .ltText').html().toString().trim().replace(/\s+/g,' ');
				var anString = '';
				$(serialized.split('<br>')).each(function(index, item) { anString += item.trim()+'<br>'; });
				if ( anString.length >= 4 && anString.substring(anString.length-4, anString.length) == '<br>' )
					anString = anString.substring(0, anString.length-4);
				anString = anString.replace(/<div>/g, '<br>');
				anString = anString.replace(/<\/div>/g, '');
				reader.table.frameAnnotation.annotation[reader.lang] = anString;				
				
			} else {
				var newFrame = new HIFrame(
					$(item).position().left+$('#lighttableContent').scrollLeft(),
					$(item).position().top+$('#lighttableContent').scrollTop()+64,
					$(item).width(),
					$(item).height()-16
				);
				newFrame.href=$(item).data('viewID');
				newFrame.imagePos.x = $('#'+item.id+" img").position().left;
				newFrame.imagePos.y = $('#'+item.id+" img").position().top;
				newFrame.imagePos.width = $('#'+item.id+" img").width();
				newFrame.imagePos.height = $('#'+item.id+" img").height();
				frames.push(newFrame);
			}
			reader.table.frames = frames;
		});
	}
	window.syncTableToModel = syncTableToModel;
	
	function setFramePos(id, x, y, width, height) {
		$('#'+id).css('left', x+'px');
		$('#'+id).css('top', y+'px');
		$('#'+id).css('width', width+'px');
		$('#'+id).css('height', height+'px');
	}
	window.setFramePos = setFramePos;
	
	function setLightTableMenuGUI() {
		var viewLink = null;
		var isAnnotation = false;
		var selectedFrame = $("#lighttableContent > div.ltSelected");
		if ( selectedFrame.length == 0 ) selectedFrame = null; else selectedFrame = selectedFrame[0];
		if ( selectedFrame != null ) isAnnotation = $(selectedFrame).hasClass('ltAnnotation');

		setMenuItem('duplicateLink', 'javascript:duplicateFrame();', false );
		setMenuItem('removeLink', 'javascript:removeFrame();', false );
		setMenuItem('fitFrameLink', 'javascript:fitToFrame();', false );
		setMenuItem('fitToThumbLink', 'javascript:fitToThumb();', false );
		setMenuItem('addAnnotationLink_ON', 'javascript:addAnnotation();', true );
		setMenuItem('addAnnotationLink_OFF', 'javascript:removeAnnotation();', true );
		toggleMenuItem('addAnnotationLink', (!reader.table.frameAnnotation.visible));
		setMenuItem('insertLinkLink', 'javascript:insertAnnotationLink();', false );
		setMenuItem('nameTableLink', 'javascript:nameTable();', true );
		setMenuItem('saveTableLink', 'javascript:saveLocalTable();', true );
		setMenuItem('newTableLink', 'javascript:newLocalTable(true);', true );
		setMenuItem('copyXMLLink', 'javascript:showTableXML();', true );
		if ( selectedFrame != null ) {
			if ( isAnnotation ) {
				setMenuItem('insertLinkLink', 'javascript:insertAnnotationLink();', true );
			} else {
				viewLink = $(selectedFrame).data('viewID'); // TODO if viewID == current hash / reader.load id
				setMenuItem('duplicateLink', 'javascript:duplicateFrame();', true );
				setMenuItem('removeLink', 'javascript:removeFrame();', true );
				setMenuItem('fitFrameLink', 'javascript:fitToFrame();', true );
				setMenuItem('fitToThumbLink', 'javascript:fitToThumb();', true );
			}
		}
		setMenuItem('showViewLink', '#'+viewLink+'/', (viewLink != null) );
		
		// light table title
		var title = reader.table.title[reader.lang];
		if ( title == null || title.length == 0 ) title = reader.strings[reader.lang]['FILE_UNTITLED'];
		if ( title == null ) title='-';
		reader.table.title[reader.lang] = title;
		$('#infotext').text(reader.strings[reader.lang]['INFOLINE_LITA']+' '+title);

		
	}
	window.setLightTableMenuGUI = setLightTableMenuGUI;
	
	function addFrame(item) {
		frameCount++;
		var id="table_"+frameCount;
		if ( item == null ) id = 'table_anno';
		$('#lighttableContent').append('<div id="'+id+'" class="ltFrame ltstack" style="position: absolute;"></div>');

		$('#'+id).append('<div id="'+id+'_title" class="ltFrameTitle">&nbsp;</div><div id="'+id+'_content" class="ltFrameContent"></div>');
		$('#'+id).append('<div id="'+id+'_contain" class="ltResizeContainer"><canvas id="'+id+'_canvas" class="ltResizeCanvas"></canvas></div>');
		var context = document.getElementById(id+'_canvas').getContext('2d');

		if ( item == null ) {
			setFramePos(id, reader.table.frameAnnotation.x, reader.table.frameAnnotation.y-16-reader.zoom.yOffset, reader.table.frameAnnotation.width, reader.table.frameAnnotation.height+16);
			$('#'+id).addClass('ltAnnotation');
			$('#'+id).bind('contextmenu', function(ev) { ev.preventDefault(); }); // disable browser context menu
			$('#'+id+"_content").html('<div class="ltText" contenteditable="true">'+reader.table.frameAnnotation.annotation[reader.lang]+'</div>');
			$('#'+id+'_content .ltText').width($('#'+id).width()-14).height($('#'+id).height()-32);
			$('#'+id+' .ltText').bind('blur keyup paste', function() { window.syncTableToModel(); });
			
			// draw custom widget
			context.fillStyle = reader.prefs['LITA_COLOR_TOOL2'];
			context.beginPath();
			context.moveTo( 0, 150);
			context.lineTo( 300, 150);
			context.lineTo( 300, 0);
			context.fill();
		} else {
			$('#'+id).addClass('hasLitaContextMenu');
			$('#'+id).data('viewID', item.href ); // store view ID with element
			$('#'+id).append('<div id="'+id+'_loading" class="ltLoadingIndicator"></div>');
			$('#'+id+'_loading').activity({ segments: 12, width: 9, space: 6, length: 20, speed: 2.2 });
			setFramePos(id, item.x, item.y-16-reader.zoom.yOffset, item.width, item.height+16);
			$('#'+id+"_content").append('<img src="'+reader.project.items[item.href].files['images'][0].href+'" />');
			setFramePos(id+"_content img", 0, 0, item.imagePos.width, item.imagePos.height);
			$('#'+id+"_content").scrollLeft(Math.abs(item.imagePos.x)).scrollTop(Math.abs(item.imagePos.y));

			var view = reader.project.items[item.href];
			var minScale = Math.max(32.0 / view.files['original'].width, 32.0 / view.files['original'].height);

			$('#'+id).css('max-width', (view.files['original'].width*3)+'px');
			$('#'+id).css('max-height', (view.files['original'].height*3)+'px');
			// draw custom widget
			var startX = 0; var startY = 0; var startWidth = 0; var startHeight = 0;
			context.fillStyle = reader.prefs['LITA_COLOR_TOOL2'];
			context.fillRect(0, 0, 300, 150);
			context.fillStyle = reader.prefs['LITA_COLOR_TOOL1'];
			context.beginPath();
			context.moveTo( 0, 0);
			context.lineTo( 300, 0);
			context.lineTo( 0, 150);
			context.fill();
		
			/* image resize handler */
			$('#'+id+'_canvas').draggable({
				containment: 'parent',
				start: function (e, ui) {
					startX = e.screenX;
					startY = e.screenY;
					startWidth = $('#'+id+"_content img").width();
					startHeight = $('#'+id+"_content img").height();
				},
				drag: function (e, ui) {
					var newScale = Math.abs(e.screenX-startX) > Math.abs(e.screenY-startY) ? (e.screenX-startX) : (e.screenY-startY);
					newScale = (startWidth+(newScale*2)) / view.files['original'].width;
					newScale = Math.min(3.0, newScale);
					newScale = Math.max(minScale, newScale);
					scrollX = $('#'+id+"_content").scrollLeft()+((view.files['original'].width*newScale)-$('#'+id+"_content img").width()) / 2;
					scrollY = $('#'+id+"_content").scrollTop()+((view.files['original'].height*newScale)-$('#'+id+"_content img").height()) / 2;

					// adjust image size
					$('#'+id+"_content img").width(view.files['original'].width*newScale);
					$('#'+id+"_content img").height(view.files['original'].height*newScale);
					// adjust scroll
					$('#'+id+"_content").scrollLeft(scrollX).scrollTop(scrollY);

					// check window bounds				
					if ( $('#'+id).width() > $('#'+id+"_content img").width() ) $('#'+id).width($('#'+id+"_content img").width());
					if ( $('#'+id).height() > $('#'+id+"_content img").height() ) $('#'+id).height($('#'+id+"_content img").height());
					ui.position.left = 0; ui.position.top = 0; // keep widget at bottom right corner
				},
				stop: function (e, ui) {
					loadFrameHiRes(id, view);
					window.syncTableToModel(); // sync changes
				}
			});
			$('#'+id+"_content").dragscrollable({acceptPropagatedEvent: true});
			$('#'+id+"_content").bind('mouseup', function(e) { window.syncTableToModel(); });
			var title = '';
			if ( reader.project.items[item.href].parent.md[reader.lang] != null && reader.project.items[item.href].parent.md[reader.lang][reader.project.sortedFields[1]] != null )
				title = reader.project.items[item.href].parent.md[reader.lang][reader.project.sortedFields[1]];
			$('#'+id+"_title").text(title); // set frame title bar

		}
		
		// attach handlers
		$('#'+id).draggable({
			handle: '#'+id+"_title",
			drag: function (event, ui) { 
				ui.position.top = Math.max (2, ui.position.top); 
				ui.position.left = Math.max (0, ui.position.left);
			},
			stop: function (event, ui) {
				window.syncTableToModel(); // sync changes
			}
		});
		
		/* text link handler */
		$('#'+id+'_content .ltText a').click(function(ev) { followLiTaLink(this); });
		
		/* window resize handler */
		$('#'+id).resizable({
			autoHide: true,
			resize: function (event, ui) {
				if ( view != null ) {
					if ( $('#'+id).width() > $('#'+id+"_content img").width() || $('#'+id).height() > $('#'+id+"_content img").height() ) {
						var scale = Math.max( $('#'+id).width() / view.files['original'].width, $('#'+id).height() / view.files['original'].height );
						$('#'+id+"_content img").width(view.files['original'].width*scale);
						$('#'+id+"_content img").height(view.files['original'].height*scale);
					}
				} else {
					$('#'+id+'_content .ltText').width($('#'+id).width()-14);
					$('#'+id+'_content .ltText').height($('#'+id).height()-32);
				}
			},
			stop: function (event, ui) {
				loadFrameHiRes(id, view);
				window.syncTableToModel(); // sync changes
			}
		});
		
		/* stack / sort order handler */
		$('#'+id+'_content img, #'+id+'_content .ltText, #'+id).mousedown(function(e) {
			$("#lighttableContent > div.ltSelected").removeClass("ltSelected");
			$('#'+id).addClass('ltSelected');
			if ( $('#'+id).hasClass('ltAnnotation') ) $('#table_anno .ltText').focus();
			else $('#emptyInput').focus();
			
			// sort frame order
			var sortedFrames = $.makeArray($('.ltFrame')).sort(function(a,b) {
				return (parseInt($(a).css("zIndex"),10) || 0) - (parseInt($(b).css("zIndex"),10) || 0);
			});
			min = parseInt($(sortedFrames[0]).css("zIndex"), 10) || 0;
			$(sortedFrames).each(function(i) { $(this).css("zIndex", min + i); });
			$('#'+id).css("zIndex", (min + sortedFrames.length));
			setLightTableMenuGUI();
		});
		
		if ( view != null ) loadFrameHiRes(id, view); // trigger image update
		return $('#'+id); 
	}
	window.addFrame = addFrame;

	
	/*
	 * main function display light tables
	 */	
	// add frames to light table desktop
	$(reader.table.frames).each(function(index, item) {
		if ( reader.project.items[item.href] == null ) {
			// load frame view
			loadFrame(item);
		} else addFrame(item); // display frame
	});
	if ( reader.table.frameAnnotation.visible ) addFrame(); // add annotation frame

	// frames de-select handler
	$('#content').mousedown(function(e) { 
		var isOverFrame = false;
		$('.ltFrame').each(function (index, item) {
			if ( e.pageX >= $(item).position().left 
				&& (e.pageY-reader.zoom.yOffset) >= $(item).position().top 
				&& e.pageX <= ($(item).position().left+$(item).width())
				&& (e.pageY-reader.zoom.yOffset) <= ($(item).position().top+$(item).height()) )
				isOverFrame = true;
		});
		if ( !isOverFrame ) {
			$("#lighttableContent > div.ltSelected").removeClass("ltSelected"); 
			$('#emptyInput').focus();
			setLightTableMenuGUI();
		}
	});
	setLightTableMenuGUI();

}

function getLayerBounds(layerID) {
	var bounds = { minX: 1.0, maxX: 0.0, minY: 1.0, maxY: 0.0 };
	if ( layerID == null || reader.project.items[layerID] == null ) return bounds;
	var layer = reader.project.items[layerID];
	for (index in layer.polygons) {
		var polygon = layer.polygons[index];
		for (var a=0; a < polygon.split(' ').length; a++ ) {
			var coord = polygon.split(' ')[a];
			var x = parseFloat(coord.split(',')[0]);
			var y = parseFloat(coord.split(',')[1]);
			if ( bounds.minX > x ) bounds.minX = x;
			if ( bounds.maxX < x ) bounds.maxX = x;
			if ( bounds.minY > y ) bounds.minY = y;
			if ( bounds.maxY < y ) bounds.maxY = y;
		}
	}
	return bounds;
}

function calcImageScales() {
	var view = reader.project.items[reader.load];
	if ( view == null ) return;
	if ( view.type == 'object' ) view = view.views[view.defaultViewID]; else
	if ( view.type == 'layer' ) view = view.parent; else
	if ( view.type != 'view' && view.type != 'layer' && view.type != 'inscription') return;
	if ( reader.mode != 'regular' ) return;
	if ( view.type == 'inscription' ) {
		view = getObject(view).views[getObject(view).defaultViewID];
	}
	
	// calculate minimum, whole image and whole window scales
	reader.zoom.min = 100.0 / Math.max(view.files['original'].width, view.files['original'].height);
	if ( view.files['original'].width > view.files['original'].height )
		reader.zoom.window = ($('#canvas').height()-reader.zoom.yOffset)*1.0/view.files['original'].height;
	else
		reader.zoom.window = 1.0*($('#canvas').width()-reader.zoom.xOffset)/view.files['original'].width;
	reader.zoom.image = Math.min(1.0*($('#canvas').width()-$('#sidebar').width())/view.files['original'].width, ($('#canvas').height()-reader.zoom.yOffset)*1.0/view.files['original'].height);
	reader.zoom.width = view.files['original'].width;
	reader.zoom.height = view.files['original'].height;

}

function zoomIn() {
	if ( ! $('#zoomInLink').parent().is('.disabled') )
		scaleImageTo(reader.zoom.cur * 2.0);
}
function zoomOut() {
	if ( ! $('#zoomOutLink').parent().is('.disabled') )
		scaleImageTo(reader.zoom.cur / 2.0);
}

function scaleImageTo(scale) {
	// make sure scale is within bounds
	scale = Math.max(reader.zoom.min, scale);
	scale = Math.min(reader.zoom.max, scale);

	// update GUI menu
	if ( scale == reader.zoom.min ) setZoomGUI(true, false);
	else if ( scale == reader.zoom.max ) setZoomGUI(false, true);
	else setZoomGUI(true, true);
	
	var xPos = 0;
	var yPos = 0;
	// center image on screen if preference key is set
	if ( reader.prefs['VIEW_CENTER'] != null && reader.prefs['VIEW_CENTER'] == 'true' ) {
		xPos = Math.max(0, Math.round((($(window).width()-reader.zoom.xOffset)-(reader.zoom.width*scale))/2));
		yPos = Math.max(0, Math.round((($(window).height()-reader.zoom.yOffset)-(reader.zoom.height*scale))/2));
	}

	reader.canvas.imageGroup.setAttribute('transform', 'translate('+xPos+','+(reader.zoom.yOffset+yPos)+') scale('+scale+')');
	reader.canvas.svg.change(reader.canvas.svg.root(), {x:0, y:0, width:Math.round(reader.zoom.width*scale)+xPos, height:Math.round(reader.zoom.height*scale)+reader.zoom.yOffset+yPos});
	// update layer outline width;
	$(reader.canvas.layerGroup).find("g").each(function(index, group) {
		if ( $(group).attr("stroke-width") > 0 )
			$(group).attr("stroke-width", 1*(1/reader.zoom.cur)); 
	});

	reader.zoom.cur = scale;
	loadHiResIfNeeded(); // load high res version of image if necessary
}

function setZoomGUI(zoomInEnabled, zoomOutEnabled) {
	$('#zoomInLink').attr("href",location.hash);
	$('#zoomOutLink').attr("href",location.hash);		
	if ( zoomInEnabled ) $('#zoomInLink').parent().removeClass("disabled"); else $('#zoomInLink').parent().addClass("disabled");
	if ( zoomOutEnabled ) $('#zoomOutLink').parent().removeClass("disabled"); else $('#zoomOutLink').parent().addClass("disabled");
}

function showID() {
	var item = reader.project.items[reader.load];
	if ( item == null ) return;
	
	var viewID = null;
	var output = '';
	switch (item.type) {
		case 'object':
			output = reader.strings[reader.lang]['ID_OBJECT']+' '+item.id;
			viewID = item.defaultViewID;
			break;
		case 'view':
			output = reader.strings[reader.lang]['ID_OBJECT']+' '+item.parent.id;
			viewID = item.id;
			break;
		case 'layer':
			output = reader.strings[reader.lang]['ID_LAYER']+' '+item.id;
			viewID = item.parent.id;
			break;
		case 'inscription': output = reader.strings[reader.lang]['ID_INSCRIPTION']+' '+item.id;
			break;
		case 'text': output = reader.strings[reader.lang]['ID_TEXT']+' '+item.id;
			break;
		case 'lita': output = reader.strings[reader.lang]['ID_LITA']+' '+item.id;
			break;
	}
	if ( viewID != null ) output += '&nbsp;&nbsp;|&nbsp;&nbsp;'+reader.strings[reader.lang]['ID_VIEW']+' '+viewID;
	$('#infotext').html(output);
	
}

function highlightAllLayers() {
	$(reader.canvas.layerGroup).find("g").each(function(index, group) {
		var layer = reader.project.items[$(group).attr('id').substring(0, $(group).attr('id').length-6)];
		if ( layer.opacity > 0 ) $(group).attr("fill-opacity", layer.opacity );
		else $(group).attr("stroke-width", 2*(1/reader.zoom.cur));
	});
	toggleMenuItem('showLayersLink', false);
	reader.allLayers = true;
}
function hideAllLayers() {
	$(reader.canvas.layerGroup).find("g").each(function(index, group) {
		$(group).attr("stroke-width", 0); 
		$(group).attr("fill-opacity", 0); 
	});
	toggleMenuItem('showLayersLink', true);
	reader.allLayers = false;
}

function highlightSourceLayer() {
	if ( reader.fromLayer == null ) return;
	highlightLayer(reader.fromLayer);
}

function highlightLayer(id) {
	hideAllLayers();

	$('#'+id+'_group').attr("stroke-width", 3*(1/reader.zoom.cur))
		.fadeTo(1,0).delay(250)
		.fadeTo(1,1).delay(250)
		.fadeTo(1,0).delay(250)
		.fadeTo(1,1).delay(250)
		.fadeTo(1,0).delay(250)
		.fadeTo(1,1).delay(250)
		.fadeTo(1,0).delay(250)
		.fadeTo(1,1);
}

function displayObjectMetadata(object) {
	if ( object == null || object.type != 'object' ) return;
	
	for (var i=1; i < reader.project.sortedFields.length; i++)
		if ( object.md[reader.lang] != null && object.md[reader.lang][reader.project.sortedFields[i]] != null && object.md[reader.lang][reader.project.sortedFields[i]].length > 0 ) {
			$("#"+reader.project.sortedFields[i]+"_field").show();
			// hightlight search terms in object metadata
			var objMetadata = object.md[reader.lang][[reader.project.sortedFields[i]]];
			if ( reader.fromSearch )
				$(reader.search.resultTerms).each(function(index, term) { objMetadata = highlightWord(objMetadata, term); });
			$("#"+reader.project.sortedFields[i]+"_value").html(objMetadata);
	 } else $("#"+reader.project.sortedFields[i]+"_field").hide();
	// object annotation
	if ( object.md[reader.lang] != null && object.md[reader.lang][reader.project.sortedFields[0]] != null && object.md[reader.lang][reader.project.sortedFields[0]].length > 0 ) {
	 	$("#objectannotation_field").show();
		// hightlight search terms in object annotation
		var objAnnotation = object.md[reader.lang][reader.project.sortedFields[0]];
		if ( reader.fromSearch )
			$(reader.search.resultTerms).each(function(index, term) { objAnnotation = highlightWord(objAnnotation, term); });
		$("#objectannotation_value").html(objAnnotation);
	} else $("#objectannotation_field").hide();
}

function displayViewMetadata(view) {
	if ( view == null || view.type != 'view' ) return;
	
	if ( view.title[reader.lang] != null && view.title[reader.lang].length > 0 ) {
 		$("#viewtitle_field").show();
		// hightlight search terms in view title
		var viewTitle = view.title[reader.lang];
		if ( reader.fromSearch )
			$(reader.search.resultTerms).each(function(index, term) { viewTitle = highlightWord(viewTitle, term); });
 		$("#viewtitle_value").html(viewTitle);
	} else $("#viewtitle_field").hide();
	if ( view.source[reader.lang] != null && view.source[reader.lang].length > 0 ) {
		$("#viewsource_field").show();
		// hightlight search terms in view source
		var viewSource = view.source[reader.lang];
		if ( reader.fromSearch )
			$(reader.search.resultTerms).each(function(index, term) { viewSource = highlightWord(viewSource, term); });
		$("#viewsource_value").html(viewSource);
	} else $("#viewsource_field").hide();		
	// view annotation
	if ( view.annotation[reader.lang] != null && view.annotation[reader.lang].length > 0 ) {
		$("#viewannotation_field").show();
		// hightlight search terms in view annotation
		var viewAnnotation = view.annotation[reader.lang];
		if ( reader.fromSearch )
			$(reader.search.resultTerms).each(function(index, term) { viewAnnotation = highlightWord(viewAnnotation, term); });
		$("#viewannotation_value").html(viewAnnotation);
	} else $("#viewannotation_field").hide();
}

function displayLayerMetadata(layer) {
	if ( layer == null || layer.type != 'layer' ) return;
	
	if ( layer.annotation[reader.lang] != null && layer.annotation[reader.lang].length > 0 ) {
 		$("#layerannotation_field").show();
		// hightlight search terms in layer annotation
		var layerAnnotation = layer.annotation[reader.lang];
		if ( reader.fromSearch )
			$(reader.search.resultTerms).each(function(index, term) { layerAnnotation = highlightWord(layerAnnotation, term); });
 		$("#layerannotation_value").html(layerAnnotation);
	} else $("#layerannotation_field").hide();
}

function displayInscription(inscription) {
	$('#inscriptionContent').html('');
	if ( inscription == null || inscription.type != 'inscription' ) return;
	// hightlight search terms in inscription
	var insContent = inscription.content[reader.lang];
	if ( reader.fromSearch )
		$(reader.search.resultTerms).each(function(index, term) { insContent = highlightWord(insContent, term); });
	$('#inscriptionContent').html(insContent);	
	showTab(2);	
}

function findHigherRes(view, href, width, height) {
	if ( view == null || view.type != 'view' ) return;
	var ref = (href == null) ? $("#canvasImage").attr('href') : href;
	var curRes = null;
	for ( var i=0; i < view.files['images'].length; i++ )
		if ( view.files['images'][i].href == ref ) curRes = view.files['images'][i];
	if ( curRes == null ) return;// view.files['images'][0]; // load initial image --> lowest res

	var curWidth = (width == null) ? Math.round(view.files['original'].width*reader.zoom.cur) : width;
	var curHeight = (height == null) ? Math.round(view.files['original'].height*reader.zoom.cur) : height;
	if ( (curWidth*curHeight) > (curRes.width*curRes.height) ) {
		var newRes = null;
		for ( var i=0; i < view.files['images'].length; i++ )
			if ( (view.files['images'][i].width*view.files['images'][i].height) >= (curWidth*curHeight) ) {				
				newRes = view.files['images'][i];
				break;
			}
		if ( newRes == null ) newRes = view.files['original'];
		if ( newRes.href != ref ) return newRes;
	}
	return null;	
}

function loadHiResIfNeeded() {
	var view = reader.project.items[reader.viewID];
	var hiRes = findHigherRes(view);
	if ( hiRes == null ) return; // no higher image resolution needed or found

	if ( hiRes.href != view.files['images'][0].href ) setImageLoadingIndicator(true);
	var newImg = new Image();
    newImg.src = hiRes.href;
    newImg.onload = function() {
		$("#canvasImage").attr('href', hiRes.href);	
		$("#canvasImage").show();
		setImageLoadingIndicator(false);
    };

}

function setMenuItem(id, ref, enabled) {
	if ( enabled ) {
		$('#'+id).parent().removeClass("disabled");
		$('#'+id).attr("href", ref);		
	} else {
		$('#'+id).parent().addClass("disabled");
		$('#'+id).attr("href", location.hash);
	}
}

function toggleMenuItem(id, state) {
	if ( state ) {
		$('#'+id+'_ON').parent().show();
		$('#'+id+'_OFF').parent().hide();	
	} else {
		$('#'+id+'_ON').parent().hide();
		$('#'+id+'_OFF').parent().show();	
	}
} 

function setMenuState() {
	// display menu
	var displayMenuEnabled = false;
	if ( reader.mode == 'regular' && getObject(reader.project.items[reader.load]) != null ) displayMenuEnabled = true;
	setMenuItem('wholePictureLink', "javascript:scaleImageTo(reader.zoom.image);", displayMenuEnabled);
	setMenuItem('wholeWindowLink', "javascript:scaleImageTo(reader.zoom.window);", displayMenuEnabled);
	setMenuItem('bestResolutionLink', "javascript:scaleImageTo(reader.zoom.full);", displayMenuEnabled);
	setZoomGUI(displayMenuEnabled, displayMenuEnabled);
	setMenuItem('showLayersLink_ON', "javascript:highlightAllLayers();", displayMenuEnabled && (Object.keys(reader.project.items[reader.viewID].layers).length > 0));
	setMenuItem('showLayersLink_OFF', "javascript:hideAllLayers();", displayMenuEnabled && (Object.keys(reader.project.items[reader.viewID].layers).length > 0));
	toggleMenuItem('showLayersLink', true);
	reader.allLayers = false;
	if ( displayMenuEnabled && (Object.keys(reader.project.items[reader.viewID].layers).length > 0) && reader.prefs['VIEW_SHOW_LAYER'] == 'true' ) {
		toggleMenuItem('showLayersLink', false);
		highlightAllLayers();
	}
	setMenuItem('targetLayerLink_ON', "javascript:highlightSourceLayer();", displayMenuEnabled && (reader.fromLayer != null));
	toggleMenuItem('targetLayerLink', true);
	
	// navigation menu
	setMenuItem('sitesLink', "#"+reader.load+"/sites", true);
	setMenuItem('refsLink', "#"+reader.load+"/refs", true);
	if ( getObject(reader.project.items[reader.load]) != null && Object.keys(getObject(reader.project.items[reader.load]).siblings).length > 1 )
		setMenuItem('allLink', "#"+reader.load+"/all", true);
	else setMenuItem('allLink', "#"+reader.load+"/all", false);

	// light table menu
	if ( $('#lighttableView').is(':visible') ) {
		// light table on
		$('#displayMenu li').each(function(index, item) { if ( !$(item).hasClass('disabled') ) $(item).addClass('disabled'); });
		setMenuItem('sitesLink', "#"+reader.load+"/sites", (reader.table.id != null));
		setMenuItem('refsLink', "#"+reader.load+"/refs", (reader.table.id != null));
		setMenuItem('allLink', "#"+reader.load+"/all", false);
		setMenuItem('addViewLink', 'javascript:addViewToLightTable();', false);
		toggleMenuItem('showLitaLink', false);
	} else {
		// light table off
		if ( reader.viewID != null ) setMenuItem('addViewLink', 'javascript:addViewToLightTable();', true);
		else setMenuItem('addViewLink', 'javascript:addViewToLightTable();', false);
		toggleMenuItem('showLitaLink', true);
		setMenuItem('showViewLink', 'javascript:void(0);', false);	
		setMenuItem('duplicateLink', 'javascript:void(0);', false);	
		setMenuItem('removeLink', 'javascript:void(0);', false);	
		setMenuItem('fitFrameLink', 'javascript:void(0);', false);	
		setMenuItem('fitToThumbLink', 'javascript:void(0);', false);
		toggleMenuItem('addAnnotationLink', true);
		setMenuItem('addAnnotationLink_ON', 'javascript:void(0);', false); setMenuItem('addAnnotationLink_OFF', 'javascript:void(0);', false);
		setMenuItem('insertLinkLink', 'javascript:void(0);', false);
		setMenuItem('nameTableLink', 'javascript:void(0);', false);
		setMenuItem('saveTableLink', 'javascript:void(0);', false);
		setMenuItem('newTableLink', 'javascript:void(0);', false);
		setMenuItem('copyXMLLink', 'javascript:void(0);', false);
	}
}

function setGUIMode(mode) {
	var modeDIVs = ['#canvas', '#textView', '#groupView', '#lighttableView'];
	for (var i=0; i < modeDIVs.length; i++) $(modeDIVs[i]).hide();
	$(modeDIVs[mode]).show();
	if ( mode == 3 ) {
		// show light table specific elements
		$('#sidebar').hide();
		$('#tabs').hide();
		// info line
		$('#info').css('background-color', reader.prefs['LITA_INFOLINE_COLOR']); // Farbe der Informationszeile
		$('#info').css('color', reader.prefs['LITA_INFO_COLOR']); // Farbe
		$('#info').css('font-family', reader.prefs['LITA_INFO_FONT']); // Schriftart
		$('#info').css('font-size', reader.prefs['LITA_INFO_SIZE']+'px'); // Größe
	} else {
		// hide light table specific elements
		$('#sidebar').show();
		$('#tabs').show();
		// info line
		$('#info').css("background-color", reader.prefs['INFOLINE_COLOR']); // Farbe der Informationszeile
		$('#info').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
		$('#info').css('font-family', reader.prefs['MAINTEXT_FONT']); // Schriftart
		$('#info').css('font-size', reader.prefs['MAINTEXT_SIZE']+'px'); // Größe
	}
}

function loadViewFileData(element, view, ref) {
	element.attr('href', ref);
}

function setGUI(forceLoad) {
	if ( !forceLoad ) forceLoad = false;
	var guiMode = 0; // 0 = canvas view, 1 = text view, 2 = goup/list view, 3 = light table
	var item = reader.project.items[reader.load];
	var loadViewFile = false;
	var list = null;
	
	// set gui display mode
	var newViewID = null;
	if ( getObject(item) != null ) if ( item.type == 'view' ) newViewID = item.id; else 
	if ( item.type == 'layer' ) newViewID = item.parent.id; else newViewID = getObject(item).defaultViewID;	
	if ( getObject(item) == null || reader.mode != 'regular' ) guiMode = 2;
	else if ( (reader.viewID == null || getObject(reader.project.items[reader.viewID]).id != getObject(item).id) && reader.mode == 'regular' || ( reader.mode == 'regular' && reader.viewID != newViewID ) ) 
		loadViewFile = true;
	if ( item.type == 'text' && reader.mode == 'regular' ) guiMode = 1;
	if ( item.type == 'lita' && reader.mode == 'regular' ) guiMode = 3;
	if ( item.type == 'url' ) {
		location.href = item.url; 
		return;
	}
	setGUIMode(guiMode);
		
	// init metadata fields
	for (var i=0; i < reader.project.sortedFields.length; i++) $("#"+reader.project.sortedFields[i]+"_field").hide();	
	$("#objectannotation_field").hide(); $("#viewannotation_field").hide(); $("#groupannotation_field").hide(); $("#layerannotation_field").hide();
	$("#viewtitle_field").hide(); $("#viewsource_field").hide();
	displayInscription(item);
		
	$('#canvasTooltip').hide(); // clear old tooltips
	$('.context-menu-list').hide(); // hide old context menus
	reader.viewID = newViewID;
	switch (guiMode) {
		case 0: // canvas view
			displayObjectMetadata(getObject(item));
			displayViewMetadata(reader.project.items[reader.viewID]);
			if ( item.type == 'layer' ) displayLayerMetadata(item);
			if ( item.type == 'object' ) showTab(reader.prefs['TABS_STANDARD']);
			break;
		
		case 1: // text view
			reader.viewID = null;
			// hightlight search terms in text
			var textContent = item.content[reader.lang];
			if ( reader.fromSearch )
				$(reader.search.resultTerms).each(function(index, term) { textContent = highlightWord(textContent, term); });
			$('#textContent').html(textContent);
			break;
			
		case 2: // group/list view
			if ( reader.mode == 'refs' ) {
				if ( item.refs != null ) list = item.refs; else list = reader.project.items[reader.viewID].refs;
			} else  if ( reader.mode == 'sites' )  {
				if ( item.sites != null ) list = item.sites; else list = reader.project.items[reader.viewID].sites;
			} else if ( reader.mode == 'all' ) list = getObject(item).siblings;
			else {
				list = item.members;
				// group annotation
				if ( item.annotation[reader.lang] != null && item.annotation[reader.lang].length > 0 ) {
			 		$("#groupannotation_field").show();
					// hightlight search terms in group annotation
					var groupAnnotation = item.annotation[reader.lang];
					if ( reader.fromSearch )
						$(reader.search.resultTerms).each(function(index, term) { groupAnnotation = highlightWord(groupAnnotation, term); });
					$("#groupannotation_value").html(groupAnnotation);
		 		}
			}
			displayContentList(list);			
			reader.viewID = null;
			break;
			
		case 3: // light table view
			reader.viewID = null;
			reader.table = JSON.parse(JSON.stringify(item)); // make copy of project light table
			displayLightTable();
			break;
	}
	
	// set info text / title field
	$('#infotext').text('');
	if ( reader.mode == 'regular' ) 
		if ( item.type == 'object' || item.type == 'view' || item.type == 'layer' ) {
			var infotext = '';
			var obj = getObject(item);
			if ( obj.md[reader.lang] != null ) {
				for (var i=1; i <= reader.prefs['INFOLINE_METADATA_NUM']; i++) {
					var mdText = obj.md[reader.lang][[reader.project.sortedFields[i]]];
					if ( mdText != null && mdText.length > 0 ) {
						if ( i > 1 ) infotext += '&nbsp;&nbsp;&ndash;&nbsp;&nbsp;';
						infotext += mdText;
					}
				}
				if ( reader.prefs['INFOLINE_METADATA_VIEW'] 
					 && reader.project.items[reader.viewID] != null 
					 && reader.project.items[reader.viewID].title[reader.lang] != null
					 && reader.project.items[reader.viewID].title[reader.lang].length > 0 ) {
					if ( infotext.length > 0 ) infotext += '&nbsp;&nbsp;&ndash;&nbsp;&nbsp;';
					infotext += reader.project.items[reader.viewID].title[reader.lang];
				}
			}
			$('#infotext').html(infotext);
		}
		else if (item.type == 'inscription')  $('#infotext').text(reader.project.items[reader.viewID].title[reader.lang]);
		else if ( item.type == 'lita' ) $('#infotext').text(reader.strings[reader.lang]['INFOLINE_LITA']+' '+item.title[reader.lang]);
		else $('#infotext').text(item.title[reader.lang]);
	if ( loadViewFile || (forceLoad && guiMode == 0) ) {
		if ( !forceLoad ) reader.fromLayer = null;
		/*
		 * construct view layers and display view preview file
		 */
		var view = reader.project.items[reader.viewID];
		$('#canvasLayerGroup > g').remove(); // remove old layers
		$("#canvasImage").attr('width', view.files['original'].width);
		$("#canvasImage").attr('height', view.files['original'].height);
		loadViewFileData($("#canvasImage"), view, view.files['images'][0].href); // set image preview file
		// scroll back top to left
		$('#canvas').scrollTop(0); $('#canvas').scrollLeft(0);
		calcImageScales();
		scaleImageTo(reader.zoom.image);

		// draw layers
		$(view.sortedLayers).each(function(index, layer) {
			var layerID = layer.id;
			var sWidth = 0;
			var svgLayer = reader.canvas.svg.group(reader.canvas.layerGroup, layer.id+"_group", {class: 'layerContextMenu', strokeWidth: sWidth*(1/reader.zoom.cur), fill: layer.color, stroke: layer.color, fillOpacity: 0.0});
			
			var tooltipDiv = '';
			if ( layer.title[reader.lang] != null && layer.title[reader.lang].length > 0 ) tooltipDiv += layer.title[reader.lang]+"<br />";
			if ( layer.annotation[reader.lang] != null && layer.annotation[reader.lang].length > 0 ) tooltipDiv += "<br />"+layer.annotation[reader.lang];

			if ( tooltipDiv.length > 0 ) $(svgLayer).tooltip({
				tip: "#canvasTooltip",
				relative: true,
				predelay: 300,
				delay: 300,
				position: 'center left',
				onBeforeShow: function(tt, pos) {
					$('#canvasTooltip .tooltipContent').html(tooltipDiv); // set tooltip content for layer
					$(tt.target).tooltip().getConf().offset[1] = Math.round(reader.pageX-pos.left)+64;
					$(tt.currentTarget.getTip()).css("cursor", "move");
				},
				onBeforeHide: function(tt, pos) {
					tt.currentTarget.getConf().offset[1] = 0;
				},
				onShow: function(tt, pos) {
					if ( ($(window).width() - ( $('#canvasTooltip').position().left+$('#canvasTooltip').width() )) < 0  )
						$('#canvasTooltip').css('left', $(window).width()-$('#canvasTooltip').width()-5);
					if ( ($(window).height() - ( $('#canvasTooltip').position().top+$('#canvasTooltip').height()+reader.zoom.yOffset )) < 0  )
						$('#canvasTooltip').css('top', $(window).height()-$('#canvasTooltip').height()-reader.zoom.yOffset-5);
					this.getTip().draggable({ containment: '#canvas' });
				},
				onHide: function(tt, pos) {
					try { this.getTip().draggable("destroy"); } catch (e) {}
				}
			});

			// layer hover handler
			$(svgLayer).hover(
				function(ev,source) {
					var layer = reader.project.items[$(ev.currentTarget).attr('id').substring(0, $(ev.currentTarget).attr('id').length-6)];
					if ( reader.fromLayer != layer.id && !reader.allLayers ) {
						if ( layer.opacity > 0 ) ev.currentTarget.setAttribute("fill-opacity", layer.opacity );
						else ev.currentTarget.setAttribute("stroke-width", 2*(1/reader.zoom.cur));
					}
				},
				function(ev,source) {
					var layer = reader.project.items[$(ev.currentTarget).attr('id').substring(0, $(ev.currentTarget).attr('id').length-6)];
					if ( reader.fromLayer != layer.id && !reader.allLayers ) {
						if ( layer.opacity > 0 ) ev.currentTarget.setAttribute("fill-opacity", 0.0);
						else ev.currentTarget.setAttribute("stroke-width", 0);						
					}
				}
			);
			// layer link handler
			if ( layer.ref != null && layer.ref.length > 0 ) {
				$(svgLayer).css("cursor", "pointer");
				$(svgLayer).click(function(ev) {
					var layer = reader.project.items[ev.currentTarget.id.substring(0, ev.currentTarget.id.length-6)];
					if ( layer != null ) {
						reader.fromLayer = layer.id;
						if ( !checkExternalLink('#'+layer.ref+'/') ) location.hash = layer.ref+"/";
					}
					ev.preventDefault();
				});
			}

			// scale polygons to view size
			var scaledPath = '';
			for (index in view.layers[layerID].polygons) {
				var polygon = layer.polygons[view.layers[layerID].polygons.length-1-index];
				
				for (var a=0; a < polygon.split(' ').length; a++ ) {
					var coord = polygon.split(' ')[a];
					var x = parseFloat(coord.split(',')[0]);
					var y = parseFloat(coord.split(',')[1]);
					x = x * reader.zoom.width;
					y = y * reader.zoom.height;
					// construct path by chaining polygons
					if ( a == 0 ) scaledPath += 'M '+x+" "+y+" "; 
					else scaledPath += 'L '+x+" "+y+" ";
				}
			}
			// add path to image
			if ( scaledPath != '' ) reader.canvas.svg.path(svgLayer, scaledPath+' Z',{fillRule: 'evenodd'});

		});

	}
	
	if ( item.type == 'layer' ) {
		reader.fromLayer = item.id;
		// scale view and center layer if preference key ist set
		if ( reader.prefs['LAYER_CENTER'] != null && reader.prefs['LAYER_CENTER'] == 'true' ) {
			var svgLayer = $('#'+item.id+"_group")[0];
			var rect = svgLayer.getBoundingClientRect();			
			var factorWidth = $(window).width() / (1.2 * (rect.width / (reader.zoom.width*reader.zoom.cur)) * reader.zoom.width);
			var factorHeight = $(window).height() / (1.2 * (rect.height / (reader.zoom.height*reader.zoom.cur)) * reader.zoom.height);
			var factor = Math.min(Math.min(factorWidth, factorHeight), reader.zoom.full);
			scaleImageTo(factor);
			// center layer
			var scrollX = ($('#canvas').scrollLeft()+svgLayer.getBoundingClientRect().left + (svgLayer.getBoundingClientRect().width/2)) - ($(window).width()/2) + (reader.zoom.xOffset/2);
			var scrollY = ($('#canvas').scrollTop()+svgLayer.getBoundingClientRect().top + (svgLayer.getBoundingClientRect().height/2)) - ($(window).height()/2) - (reader.zoom.yOffset/2);
			$('#canvas').scrollTop(scrollY); $('#canvas').scrollLeft(scrollX);
		}

		
		highlightLayer(item.id);
	}
	setMenuState();
	setLoadingIndicator(false);

	// hightlight search terms in info bar
	if ( reader.fromSearch ) {
		var infotext = $('#infotext').text();
		$(reader.search.resultTerms).each(function(index, term) {
			infotext = highlightWord(infotext, term);
		});
		$('#infotext').html(infotext);
	}
	reader.fromSearch = false; // reset state
		
	// show GUI to user
	scanExternalLinks();
	$("#loadingpanel").css('display','none');
}


