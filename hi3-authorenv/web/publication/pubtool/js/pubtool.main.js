/*
 * Copyright 2014 Leuphana Universität Lüneburg. All rights reserved.
 *
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

/* @author Jens-Martin Loebel */

function initPubTool() {
	var isSafari = navigator.userAgent.indexOf("Safari") > -1 && navigator.userAgent.indexOf("Chrome") == -1;
	
	// load i18n
	i18n.init({ 
		preload: ['de', 'en'], 
		fallbackLng: 'en', 
		detectLngQS: 'lang',
		resGetPath: 'pubtool/locales/__ns__.__lng__.json' }, 
	function(t) {
		window.t = t;
		$('body').i18n();
		
		$("#tabs").tabs();
		$("#tabs").tabs( "disable", 1 );
		$("#tabs").tabs( "disable", 2 );
		$("#tabs").tabs( "disable", 3 );
		$( "#metadataSortable" ).sortable();
	    $( "#metadataSortable" ).disableSelection();
		$( "#groupSortable" ).sortable();
		$( "#groupSortable" ).disableSelection();
		$( "#textSortable" ).sortable();
	    $( "#textSortable" ).disableSelection();
		$( "#litaSortable" ).sortable();
		$( "#litaSortable" ).disableSelection();
    	$('#pubtoolwindow').draggable({handle:'#pubtoolwindow_title'});
    	$('#pubtoolwindow').resizable({handles: 's'});
		$('#progressbar').progressbar({value: false});
		$('#startbutton').button({icons: { primary: "ui-icon-arrowthickstop-1-s"}, disabled: true}).click(function(ev) {generatePeTALDocs();});
		$('#zipbutton').button().click(function(ev) {saveZIPFile();});
		
		$('#combobox').ddslick({
			width: 390,
			defaultSelectedIndex: 0
		});


		if ( isSafari ) {
			$('#warningbar').show();
			$('#safarinote').show();
		}		

		var pubtool = {};
		window.pubtool = pubtool;
		pubtool.canvas = document.createElement('canvas');
	    pubtool.stopwords = window.stopwords;
	    pubtool.hiThemes = window.hiThemes;
            
            pubtool.version = 'v3.0.0.alpha-1';
	
		JSZipUtils.getBinaryContent('pubtool/reader-base.zip', function(err, data) {
			if ( err ) {
    			throw err; // or handle err
			}

			pubtool.zip = new JSZip(data);
		});
	
		pubtool.service = {};
		pubtool.serializer = new XMLSerializer(); 
		HIService.soap.endpoint = GetURLParameter('session');
	
		if ( !HIService.soap.endpoint ) reportError(t("endpointMissing"));

		// init web service (SOAP) API
		pubtool.service.HIEditor = new ws_service_hyperimage_org__HIEditor();
		pubtool.service.HIEditor.url = location.protocol+'//'+location.host+location.pathname+'../HIEditorService'; 
		// DEBUG remove
//		pubtool.service.HIEditor.url = "http://127.0.0.1:8080"+'/HI3Author/HIEditorService';

		/* load initial project data from web service */
		pubtool.project = {};
		pubtool.service.HIEditor.synchronous = false;
		pubtool.service.HIEditor.getProject(function(result) { 
			pubtool.service.HIEditor.getImportGroup(function(importGroup) {
				pubtool.project.items = typeof(pubtool.project.items) == 'undefined' ? {} : pubtool.project.items;
				pubtool.project.groups = typeof(pubtool.project.groups) == 'undefined' ? {} : pubtool.project.groups;
				parseItem(importGroup.getReturn());
				pubtool.project.groups['G' + importGroup.getReturn().getId()] = pubtool.project.items['G' + importGroup.getReturn().getId()];
				pubtool.project.importGroup = 'G'+importGroup.getReturn().getId();
				setProjectMetadata(result.getReturn());
				for (lang in pubtool.project.langs) {
					pubtool.project.groups['G'+importGroup.getReturn().getId()].title[pubtool.project.langs[lang]] = '(Import)';
					pubtool.project.groups['G'+importGroup.getReturn().getId()].annotation[pubtool.project.langs[lang]] = '';
				}

			}, HIExceptionHandler);
		}, HIExceptionHandler );
		
	});

}

if (!String.prototype.encodeXML) {
  String.prototype.encodeXML = function () {
    return this.replace(/&/g, '&amp;')
               .replace(/</g, '&lt;')
               .replace(/>/g, '&gt;')
               .replace(/"/g, '&quot;')
               .replace(/'/g, '&apos;');
  };
}

function HIExceptionHandler(status, exception) {
	console.log("HIServiceException:", exception);
	if ( exception.type == "HISessionExpiredException" ) reportError(t("sessionExpired"));
	if ( exception.type == "HIServerException" ) reportError(t("serverNetworkError")+exception.text);
}

function reportError(error) {
	$('#hiloading').hide(); $('#progressbar').hide(); // hide animated ui elements
	
	$('#errorMessage').html(error);
    $( "#errorDialog" ).dialog({
    	draggable: false,
		resizable: false,
		closeOnEscape: false,
		dialogClass: 'no-close',
		title: t("criticalError"),
		width: 550,
		height: 200,
		modal: true,
  });
	
	throw new Error(error);
}

function GetURLParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

function GetIDModifierFromContentType(contentType) {
	switch (contentType) {
		case 'HIObject': return 'O';
		case 'HIView': return 'V';
		case 'HIInscription': return 'I';
		case 'HILayer': return 'L';
		case 'HIGroup': return 'G';
		case 'HIText': return 'T';
		case 'HILightTable': return 'X';
		case 'HIURL': return 'U';
	}
	return '';
}

function GetElementType(element) {	
	switch (element.typeMarker) {
		case 'ws_service_hyperimage_org__hiObject': return 'object';
		case 'ws_service_hyperimage_org__hiView': return 'view';
		case 'ws_service_hyperimage_org__hiInscription': return 'inscription';
		case 'ws_service_hyperimage_org__hiLayer': return 'layer';
		case 'ws_service_hyperimage_org__hiText': return 'text';
		case 'ws_service_hyperimage_org__hiGroup': return 'group';
		case 'ws_service_hyperimage_org__hiurl': return 'url';
		case 'ws_service_hyperimage_org__hiLightTable': return 'lita';

		case 'HIObject': return 'object';
		case 'HIView': return 'view';
		case 'HIInscription': return 'inscription';
		case 'HILayer': return 'layer';
		case 'HIText': return 'text';
		case 'HIGroup': return 'group';
		case 'HIURL': return 'url';
		case 'HILightTable': return 'lita';
	}
	return 'base';
}

function getPeTALType(item) {
	switch ( item.id.substring(0,1) ) {
		case 'O': return 'object';
		case 'V': return 'view';
		case 'I': return 'inscription';
		case 'L': return 'layer';
		case 'T': return 'text';
		case 'G': return 'group';
		case 'U': return 'url';
		case 'X': return 'lita';
		default: return 'base';
	}
}

function HILighttable(id) {
	this.id = id;
	this.type = "lita";
	this.title = new Object();
	this.xml = '';
	this.sites = new Object();
	this.refs = new Object();
}
function HIText(id) {
	this.id = id;
	this.type = "text";
	this.title = new Object();
	this.content = new Object();
	this.sites = new Object();
	this.refs = new Object();	
}
function HIGroup(id) {
	this.id = id;
	this.type = "group";
	this.title = new Object();
	this.annotation = new Object();
	this.members = new Object();
	this.sites = new Object();
	this.refs = new Object();
}
function HIObject(id) {
	this.id = id;
	this.sites = {};
	this.type = "object";
	this.defaultViewID = null;
	this.md = new Object();
	this.views = new Object();
	this.siblings = new Object();

}
function HIView(id) {
	this.id = id;
	this.type = "view";
	this.files = new Object();
	this.title = new Object();
	this.source = new Object();
	this.annotation = new Object();
	this.layers = new Object();
	this.sortOrder = [];
	this.sites = new Object();
	this.refs = new Object();
	this.parent = null;	

}
function HIInscription(id) {
	this.id = id;
	this.type = "inscription";
	this.content = new Object();
	this.sites = new Object();
	this.refs = new Object();
	this.parent = null;	

}
function HILayer(id) {
	this.id = id;
	this.type = "layer";
	this.color = null;
	this.sites = {};
	this.opacity = 1.0;
	this.ref = null;
	this.polygons = new Array();
	this.parent = null;
	this.title = new Object();
	this.annotation = new Object();

}
function HIURL(id) {
	this.id = id;
	this.title = null;
	this.annotation = null;
	this.type = "url";
	this.url = null;
	this.sites = new Object();
	
}

function parseServiceMDField (field) {
		var previewChunks = field.split('{#}');
		var previewText = '';
		var chunkCount = 0;
		while ( chunkCount < (previewChunks.length-1) ) {
			chunkCount++;
			var key = previewChunks[chunkCount];
			var ref = null;
			if ( key.indexOf(":") != -1 ) {ref = key.substring(key.indexOf(":")+1); key = key.substring(0, key.indexOf(":"));}
			chunkCount++;
			var value = previewChunks[chunkCount];
			switch (key) {
				case 'bold':
					previewText += "<b>"+value.encodeXML()+"</b>";
					break;
				case 'italic':
					previewText += "<i>"+value.encodeXML()+"</i>";
					break;
				case 'underline':
					previewText += "<u>"+value.encodeXML()+"</u>";
					break;
				case 'subscript':
					previewText += "<sub>"+value.encodeXML()+"</sub>";
					break;
				case 'superscript':
					previewText += "<sup>"+value.encodeXML()+"</sup>";
					break;
				case 'link':
					previewText += "<a href=\"#"+ref+"\">"+value.encodeXML()+"</a>";
					break;
				case 'literal':
					previewText += value.replace(/<br>/g, '<br/>'); // fix break tag mismatch between html5 and xhtml syntax [HOTFIX]
					break;
				case 'regular':
					previewText += value.encodeXML();
					break;
			}	
		}

		if ( key != 'literal' ) previewText = previewText.replace(/\n/g, '<br/>');
	
	return previewText;
}

function parseItem(data, relatedID) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);

	var itemType = GetElementType(data);
	var id;
	var item = null;
	var unloaded = null;
	var viewID = null;

	switch (itemType) {
		case "view":
			// get object id
			id = relatedID;
			unloaded = id; // load object
			viewID = 'V'+data.getId();
			if ( pubtool.project.items[viewID] == null ) {
				item = new HIView(viewID);
				item.uuid = data.getUUID();

				// parse view metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue().encodeXML();
								break;
							case "HIBase.source":
								item.source[lang] = content.getValue().encodeXML();
								break;
							case "HIBase.comment":
								item.annotation[lang] =  parseServiceMDField(content.getValue());
								break;
				}}}
				
				item.sites = new Object();
				item.refs = new Object();
				if ( item.parent != null )
					item.parent.siblings = new Object();
				else item.siblings = new Object();

				// parse image files
				item.files['images'] = new Array();
				// full size			
				var newFile = new Object();
				newFile.width = data.getWidth();
				newFile.height = data.getHeight();
				
				newFile.href = item.id+'.jpg';
				item.files['images'].push(newFile);
				
				var previewWidth = Math.round(data.getWidth()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				var previewHeight = Math.round(data.getHeight()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				var previewCount = 1;
				while ( data.getWidth() > (3*previewWidth) ) {
					previewHeight *= 2;
					previewWidth *= 2;
					newFile = new Object();
					newFile.width = previewWidth;
					newFile.height = previewHeight;
					newFile.href = item.id+'_'+previewCount+'.jpg';
					item.files['images'].push(newFile);
					previewCount++;
				}
				
				// preview size
				newFile = new Object();
				newFile.width = Math.round(data.getWidth()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				newFile.height = Math.round(data.getHeight()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				newFile.href = item.id+'_prev.jpg';
				item.files['images'].push(newFile);
                                // nav size if needed
                                if ( (data.getWidth() / data.getHeight()) > 5 ) {
                                    newFile = new Object();
                                    newFile.width = Math.round(data.getWidth()*(128/data.getHeight()));
                                    newFile.height = 128;
                                    newFile.href = item.id+'_nav.jpg';
                                    item.files['nav'] = newFile; 
                                }
				// thumbnail size
				newFile = new Object();
				newFile.width = Math.round(data.getWidth()*Math.min(128/data.getWidth(), 128/data.getHeight()));
				newFile.height = Math.round(data.getHeight()*Math.min(128/data.getWidth(), 128/data.getHeight()));
				newFile.href = item.id+'_thumb.jpg';
				item.files['thumb'] = newFile; 

				// sort image files
				item.files['images'].sort(function(x, y) {
					return (x.width*x.height)-(y.width*y.height);
				});
				item.files['original'] = item.files['images'][item.files['images'].length-1];

				// parse layers
				$(data.getSortOrder().split(',')).each(function(index, layerID) {
					if ( layerID.length > 0 ) item.sortOrder.push('L'+layerID); // layer sort order
				});
				$(data.getLayers()).each(function (index, xmlLayer) {					
					var layer = new HILayer('L'+xmlLayer.getId());
					layer.uuid = xmlLayer.getUUID();
					layer.color = '#'+(xmlLayer.getRed().toString(16).length < 2 ? '0'+xmlLayer.getRed().toString(16) : xmlLayer.getRed().toString(16));
					layer.color += xmlLayer.getGreen().toString(16).length < 2 ? '0'+xmlLayer.getGreen().toString(16) : xmlLayer.getGreen().toString(16);
					layer.color += xmlLayer.getBlue().toString(16).length < 2 ? '0'+xmlLayer.getBlue().toString(16) : xmlLayer.getBlue().toString(16);

					layer.opacity = xmlLayer.getOpacity();
					layer.ref = xmlLayer.getLinkInfo();
					if ( layer.ref != null ) layer.ref = GetIDModifierFromContentType(layer.ref.getContentType())+layer.ref.getBaseID();
					
					// parse polygons
					$(xmlLayer.getPolygons().split('|')).each(
						function(pindex, poly) {
							var pstring = poly.substring(2).replace(/#/g,',').replace(/;/g,' ');
							if ( pstring.length > 0 ) layer.polygons.push(pstring); 
						}
					);
					
					// parse layer title and annotation
					for (var i=0; i < xmlLayer.getMetadata().length; i++) {
						var lang = xmlLayer.getMetadata()[i].getLanguage();
						for (var c=0; c < xmlLayer.getMetadata()[i].getContents().length; c++) {
							var content = xmlLayer.getMetadata()[i].getContents()[c];
							switch (content.getKey()) {
								case "HIBase.title":
									layer.title[lang] = content.getValue().encodeXML();
									break;
								case "HIBase.comment":
									layer.annotation[lang] =  parseServiceMDField(content.getValue());
									break;
							}
						}
					}					

					layer.parent = item;
					item.layers[layer.id] = layer;
					pubtool.project.items[layer.id] = layer;

				});
				pubtool.project.items[viewID] = item;
				
			}
			break;
			
		case "inscription":
			// get object id
			id = relatedID;
			unloaded = id; // load object
			viewID = 'I'+data.getId();
			if ( pubtool.project.items[viewID] == null ) {
				item = new HIInscription(viewID);
				item.uuid = data.getUUID();

				// parse inscription content
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						if ( content.getKey() == "HIBase.content" ) item.content[lang] =  parseServiceMDField(content.getValue());
					}
				}

				item.sites = new Object();
				item.refs = new Object();
				// parse siblings
				// TODO: implement siblings quick view
				if ( item.parent != null )
					item.parent.siblings = new Object();
				else item.siblings = new Object();
				
				pubtool.project.items[viewID] = item;
			}		
			break;
			
		case "object":
			id = 'O'+data.getId();
			if ( pubtool.project.items[id] == null ) {
				item = new HIObject(id);
				item.uuid = data.getUUID();
				
				// parse object metadata
				var metadata = data.getMetadata();
				for (var i=0; i < metadata.length; i++) {
					var md = metadata[i];
					var lang = md.getLanguage();
					item.md[lang] = new Object();
					var records = md.getContents();
					for (var recordID=0; recordID < records.length; recordID++) {
						var rec = records[recordID];
						if ( pubtool.project.templates[rec.getKey().replace(/\./g,'_')].richText )
							item.md[lang][rec.getKey().replace(/\./g,'_')] = parseServiceMDField(rec.getValue());
						else item.md[lang][rec.getKey().replace(/\./g,'_')] = rec.getValue().encodeXML();
					}
				}
				item.defaultViewID = data.getDefaultView();
				if ( item.defaultViewID != null ) item.defaultViewID = (data.getDefaultView().typeMarker == 'ws_service_hyperimage_org__hiView' ? 'V'+item.defaultViewID.getId() : 'I'+item.defaultViewID.getId());
				pubtool.project.items[id] = item;
			}
			// check if object has unloaded view
			for ( var i=0; i < data.getViews().length; i++ ) {
				parseItem(data.getViews()[i], id);
				var dataID = (data.getViews()[i].typeMarker == 'ws_service_hyperimage_org__hiView' ? 'V'+data.getViews()[i].getId() : 'I'+data.getViews()[i].getId());
				if ( pubtool.project.items[dataID].parent == null ) {
					pubtool.project.items[dataID].parent = pubtool.project.items[id];
					pubtool.project.items[id].views[dataID] = pubtool.project.items[dataID];
				}
			}
			if ( item.defaultViewID == null && Object.keys(item.views).length > 0 ) item.defaultViewID = item.views[Object.keys(item.views)[0]].id;

			break;

		case "layer":
			unloaded = 'V'+quickinfo.getRelatedID();
			break;

		case "text":
			id = 'T'+data.getId();			
			if ( pubtool.project.items[id] == null ) {
				item = new HIText(id);
				item.uuid = data.getUUID();
				// parse text metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue().encodeXML();
								break;
							case "HIBase.content":
								item.content[lang] =  parseServiceMDField(content.getValue());
								break;
						}
					}
				}
				item.sites = new Object();
				item.refs = new Object();				
				pubtool.project.items[id] = item;
			}
			break;

		
		case "group":
			id = 'G'+data.getId();
			if ( pubtool.project.items[id] == null ) {
				item = new HIGroup(id);
				item.uuid = data.getUUID();
				item.sortOrder = [];
				$(data.getSortOrder().split(',')).each(function(index, memberID) {
					if ( memberID.length > 0 ) item.sortOrder.push(memberID); // group member sort order
				});
				// parse group metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue().encodeXML();
								break;
							case "HIBase.comment":
								item.annotation[lang] =  parseServiceMDField(content.getValue());
								break;
						}
					}
				}
				
				item.sites = new Object();
				item.refs = new Object();
								
				pubtool.project.items[id] = item;
			}
			break;

		case "url":
			id = 'U'+data.getId();
			if ( pubtool.project.items[id] == null ) {
				item = new HIURL(id);
				item.uuid = data.getUUID();
				// parse URL metadata
				item.title = data.getTitle().encodeXML();
				item.annotation = ''; // TODO
				item.sites = new Object();
				item.refs = new Object();
				item.url = data.getUrl().encodeXML();
				pubtool.project.items[id] = item;
			}
			break;
			

		case 'lita':
			id = 'X'+data.getId();			
			if ( pubtool.project.items[id] == null ) {
				item = new HILighttable(id);
				item.uuid = data.getUUID();
				data = $.parseXML(data.getXml());
				// parse light table metadata
				var titles = $(data).find("lita > title");
				for (var i=0; i < titles.length; i++)
					if ( titles[i].firstChild != null ) item.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
					else item.title[titles[i].getAttribute("xml:lang")] = '';
				
				// TODO replace frameAnnotation <line> with HTML
				item.count = 0;
				$(data).children().children().each(function (index, node) {
					if ( node.tagName == 'frame' ) item.count++;
					item.xml += pubtool.serializer.serializeToString(node);
				});
				
				item.sites = new Object();
				item.refs = new Object();
				pubtool.project.items[id] = item;
			}
			break;			
			
	}
}

function getItemLinks(item) {
	var links = {};
	
	function parseFieldLinks(field) {
		for ( var i=0 ; i<pubtool.project.langs.length ; i++ ) {
				var content = field[pubtool.project.langs[i]];
				if ( typeof(content) == 'undefined' ) content = field;
				var clinks = content.match(/<a href="([^\'\"]+)/g);
				for (var i in clinks)
					if ( pubtool.project.items[clinks[i].substring(10)] != null ) links[clinks[i].substring(10)] = pubtool.project.items[clinks[i].substring(10)];
		}
	}

	switch (item.id.substring(0,1)) {
		case 'T':
		case 'I':
			parseFieldLinks(item.content);
			break;

		case 'G':
		case 'U':
		case 'V':
			parseFieldLinks(item.annotation);
			break;
			
		case 'L':
			parseFieldLinks(item.annotation);
			if ( item.ref != null && item.ref.length > 0 ) links[item.ref] = pubtool.project.items[item.ref];
			break;
			
		case 'O':
			for ( var langID=0 ; langID < pubtool.project.langs.length ; langID++ ) {
				for (var i=0; i < Object.keys(item.md[pubtool.project.langs[langID]]); i++)
					parseFieldLinks(item.md[pubtool.project.langs[langID]][Object.keys(item.md[pubtool.project.langs[langID]])[i]]);			
			}
			break;

		case 'X':
			// TODO light tables
			break;
	}

	return links;
}


function showSaveZIPDialog() {
	$('#pageinit').hide();
	
	$('#zipdialog').dialog({
		modal: true,
		dialogClass: "no-close",
		closeOnEscape: false,
		draggable: false,
		resizable: false,
		width: 560
	});
}

function saveZIPFile() {
	console.log("saving...");
	if ( !pubtool.zipContent ) return;
	saveAs(pubtool.zipContent, "HI-Export.zip");
}

function generateZIPFile() {
	if ( $('#progressbar').progressbar( "option", "value" ) != false ) {
		$('#progressbar').progressbar({value: false});
		$('.progress-label').html(t("createZIP"));
		$('#previewImage').attr('src', 'pubtool/img/hyperimage-logo-pubtool.png');
		window.setTimeout(generateZIPFile(), 1000);
	} else {
		pubtool.zipContent = pubtool.zip.generate({type:"blob"});
		showSaveZIPDialog();
		console.log("done");
	}
}



function generateLayerImageFiles() {
	if ( !pubtool.project.layerProcessCounter ) pubtool.project.layerProcessCounter = 0;

	// gather layer thumbnails to load
	if ( !pubtool.project.loadLayers ) {
		var loadLayers = [];
		for (var i=0; i < Object.keys(pubtool.project.items).length; i++) {
			var id = Object.keys(pubtool.project.items)[i];
			var view = pubtool.project.items[id];
			if ( id.substring(0,1) == 'V' ) {
				for (var index=0; index < Object.keys(view.layers).length; index++) {
					if ( view.layers[Object.keys(view.layers)[index]].polygons.length > 0 ) loadLayers.push(view.layers[Object.keys(view.layers)[index]].id);
				}
			}
		}
		pubtool.project.loadLayers = loadLayers;
		var itemcount = loadLayers.length;
		$('#progressbar').progressbar( "option", "max", itemcount );
		$('#progressbar').progressbar( "option", "value", 0 );
		$('.progress-label').html(t("loadingLayers"));
		
		if ( itemcount == 0 ) { // no layers in project
			pubtool.project.layersProcessed = true;
			generateZIPFile();
			return;
		}
	}
	
	// load and process layers from layer load list
	if ( pubtool.project.loadLayers.length > 0 ) {
		var item = pubtool.project.loadLayers.pop();
		pubtool.project.layerProcessCounter++;
		pubtool.service.HIEditor.getImage(
			function (cb) { 
				pubtool.zip.file("img/"+item+".jpg", cb.getReturn(), {base64: true});
				$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
				$('#previewImage').attr('src', 'data:image/jpeg;base64,'+cb.getReturn());
				window.setTimeout(generateLayerImageFiles(), 200);
				pubtool.project.layerProcessCounter--;
				if ( pubtool.project.layerProcessCounter <= 0 ) generateZIPFile();
			}, 
			function (cb,msg) { 
				console.log("getImage error --> ", cb);
				$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
				window.setTimeout(generateLayerImageFiles(), 200);
				pubtool.project.layerProcessCounter--;
				if ( pubtool.project.layerProcessCounter <= 0 ) generateZIPFile();
				HIExceptionHandler(cb, msg);
			},
			item.substring(1),
			'HI_THUMBNAIL'
		);

	} else {
		pubtool.project.layersProcessed = true;
	}
}

function generateImageFiles() {
	
	function imageToDataUri(filename, data, width, height, isPreview) {
		pubtool.project.processCounter++;
		var img = new Image();
		img.onload = function(){
	        var ctx = pubtool.canvas.getContext('2d');

		    pubtool.canvas.width = width;
			pubtool.canvas.height = height;

		    // draw source image into the off-screen canvas:
    		ctx.drawImage(img, 0, 0, width, height);

		    // encode image to data-uri with base64 version of compressed image
			if ( isPreview ) $('#previewImage').attr('src', pubtool.canvas.toDataURL('image/jpeg', 0.8));
			pubtool.zip.file(filename, pubtool.canvas.toDataURL('image/jpeg', 0.8).substring(23), {base64: true});
			pubtool.project.processCounter--;
			if ( pubtool.project.processCounter == 0 && pubtool.project.imagesProcessed ) generateLayerImageFiles();
		};
		img.src = data;
	}

	function processImage(id, data) {		
            var view = pubtool.project.items[id];

            imageToDataUri("img/"+id+"_thumb.jpg", data, view.files.thumb.width, view.files.thumb.height, true);
            if ( view.files.nav != null )
                imageToDataUri("img/"+id+"_nav.jpg", data, view.files.nav.width, view.files.nav.height, true);

            pubtool.zip.file("img/"+id+".jpg", data.substring(23), {base64: true});

            for (var i=0; i < view.files.images.length-1; i++)
                imageToDataUri("img/"+view.files.images[i].href, data, view.files.images[i].width, view.files.images[i].height);
	}
	
	if ( !pubtool.project.processCounter ) pubtool.project.processCounter = 0;

	// gather views to load
	if ( !pubtool.project.loadViews ) {
		var loadViews = [];
		for (var i=0; i < Object.keys(pubtool.project.items).length; i++) {
			var id = Object.keys(pubtool.project.items)[i];
			if ( id.substring(0,1) == 'V' ) loadViews.push(id);
		}
		pubtool.project.loadViews = loadViews;
		var itemcount = loadViews.length;
		$('#progressbar').progressbar( "option", "max", itemcount );
		$('#progressbar').progressbar( "option", "value", 0 );
		$('.progress-label').html(t("loadingViews"));
	}

	// load and process view from view load list
	if ( pubtool.project.loadViews.length > 0 ) {
		var item = pubtool.project.loadViews.pop();
		pubtool.service.HIEditor.getImage(
			function (cb) { 
				processImage(item, 'data:image/jpeg;base64,'+cb.getReturn());
				$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
				window.setTimeout(generateImageFiles(), 200);
			}, 
			function (cb,msg) { 
				console.log("getImage error --> ", cb);
				$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
				window.setTimeout(generateImageFiles(), 200);
				HIExceptionHandler(cb, msg);
			},
			item.substring(1),
			'HI_FULL'
		);
	} else {
		pubtool.project.imagesProcessed = true;
	}


}

function setProjectMetadata(project) {
	pubtool.project.id = 'P'+project.getId();
	pubtool.project.items = typeof(pubtool.project.items) == 'undefined' ? {} : pubtool.project.items;
	pubtool.project.groups = typeof(pubtool.project.groups) == 'undefined' ? {} : pubtool.project.groups;
	pubtool.project.texts = typeof(pubtool.project.texts) == 'undefined' ? {} : pubtool.project.texts;
	pubtool.project.litas = typeof(pubtool.project.litas) == 'undefined' ? {} : pubtool.project.litas;

	/* extract project languages */
	pubtool.project.defaultLang = project.getDefaultLanguage().getLanguageId();
	var langs = project.getLanguages();
	pubtool.project.langs = [];
	for ( i = 0; i < langs.length; i++)
		pubtool.project.langs[i] = langs[i].getLanguageId();

	if (pubtool.project.defaultLang == null || pubtool.project.defaultLang.length == 0)
		pubtool.project.defaultLang = pubtool.project.langs[0];		
            
        /* parse project preferences */
        pubtool.project.prefs = {};
        var projPrefs = project.getPreferences();
        for ( i = 0; i < projPrefs.length; i++ ) {
            var pref  = projPrefs[i];
            pubtool.project.prefs[pref.getKey()] = pref.getValue();
        }

	/* extract project title */
	pubtool.project.title = {};
	for ( i = 0; i < project.getMetadata().length; i++)
		if (project.getMetadata()[i].getTitle() != null)
			pubtool.project.title[project.getMetadata()[i].getLanguageID()] = project.getMetadata()[i].getTitle();
		else
			pubtool.project.title[project.getMetadata()[i].getLanguageID()] = '';

	/* extract start element */
	if (project.getStartObjectInfo() == null)
		pubtool.start = pubtool.project.importGroup.id;
	else
		pubtool.start = GetIDModifierFromContentType(project.getStartObjectInfo().getContentType()) + project.getStartObjectInfo().getBaseID();

	/* extract and sort template fields */
	templates = project.getTemplates();
	sortedFields = new Array();
	pubtool.project.sortedFields = sortedFields;
	pubtool.project.templates = {};
	pubtool.project.templateKeys = [];
	var htmlFields = "";
	for ( var i = 0; i < templates.length; i++) {
		var templateID = templates[i].getNamespacePrefix();
		if ( templateID == 'HIBase' ) continue; // don't include HIBase template
		pubtool.project.templateKeys.push({id: templates[i].getId(), key: templateID});

		keys = templates[i].getEntries();
		for ( keyID = 0; keyID < keys.length; keyID++) {
			// todo sorting
			sortedFields.push(templateID + "_" + keys[keyID].getTagname());
			var tempKey = pubtool.project.templates[templateID + "_" + keys[keyID].getTagname()] = new Object();
			tempKey.key = keys[keyID].getTagname();
			tempKey.template = templateID;
			tempKey.richText = keys[keyID].getRichText();
			tempLangs = keys[keyID].getDisplayNames();
			for ( langID = 0; langID < tempLangs.length; langID++) {
				if ( tempLangs[langID].getDisplayName() != null && tempLangs[langID].getDisplayName().length > 0 )
					tempKey[tempLangs[langID].getLanguage()] = tempLangs[langID].getDisplayName();
				else
					tempKey[tempLangs[langID].getLanguage()] = templateID + '.' + tempKey.key;
			}
			for (var a=0; a < pubtool.project.langs.length; a++)
				if ( tempKey[pubtool.project.langs[a]] == null )
					tempKey[pubtool.project.langs[a]] = templateID + '.' + tempKey.key;

		}
	}
	pubtool.project.sortedFields = sortedFields;
	
	// populate UI
	for ( var i in Object.keys(pubtool.project.sortedFields) ) {
		var field = pubtool.project.templates[sortedFields[i]];
		var fieldTitle = field[pubtool.project.defaultLang];
		if ( fieldTitle == null || fieldTitle.length == 0 ) fieldTitle = '('+sortedFields[i]+')';
		else fieldTitle += ' ('+sortedFields[i].split('_')[0]+')';
		if ( field.template == 'HIInternal' )
			fieldTitle = t('HIInternal'+field.key)+' ('+t('HIInternal')+')'; 
		$('#metadataSortable').append('<li data-key="'+field.template+'_'+field.key+'" class="ui-state-default"><span class="ui-icon ui-icon-arrowthick-2-n-s"></span>'+fieldTitle+'</li>');
	}
	
	$('#pageinit').hide();
	

	/*
	 * extract visible groups, project texts and light tables
	 */	
	pubtool.service.HIEditor.getGroups(function(result) {pubtool.project.groupsLoaded = true;setListTabContent(result.getReturn(), 'G', pubtool.project.groups, '#groupSortable');$('.grouploader').hide();$('#tabs').tabs('enable', 1);loadProjectContents();}, HIExceptionHandler);
	pubtool.service.HIEditor.getProjectTextElements(function(result) {pubtool.project.textsLoaded = true;setListTabContent(result.getReturn(), 'T', pubtool.project.texts, '#textSortable');$('.textloader').hide();$('#tabs').tabs('enable', 2);loadProjectContents();}, HIExceptionHandler);
	pubtool.service.HIEditor.getProjectLightTableElements(function(result) {pubtool.project.litasLoaded = true;setListTabContent(result.getReturn(), 'X', pubtool.project.litas, '#litaSortable');$('.litaloader').hide();$('#tabs').tabs('enable', 3);loadProjectContents();}, HIExceptionHandler);

}

function setListTabContent(serverContents, prefix, contents, tagname) {
	for (contentID in serverContents) {
		var content = serverContents[contentID];
		parseItem(content);
		contents[prefix + content.getId()] = pubtool.project.items[prefix + content.getId()];
	}
	for ( var i in Object.keys(contents) ) {
		var content = contents[Object.keys(contents)[i]];
		var contentTitle = content.title[pubtool.project.defaultLang];
		if ( contentTitle == null || contentTitle.length == 0 ) contentTitle = '('+content.id+')';
		$(tagname).append('<li data-baseid="'+content.id+'" class="ui-state-default"><span class="ui-icon ui-icon-arrowthick-2-n-s"></span>'+contentTitle+'<input style="float: right;" checked="checked" type="checkbox" name="'+content.id+'" value="'+content.id+'" /></li>');
	}
	if ( Object.keys(contents).length == 0 ) $(tagname).parent().append('<strong>Keine Elemente vorhanden.</strong>');
}

function loadProjectContents() {
	if ( !pubtool.project.groupsLoaded || !pubtool.project.textsLoaded || !pubtool.project.litasLoaded ) return;
	$('#startbutton').button("option", "disabled", false);

	if ( !pubtool.project.groupContentsLoaded ) {
		$('#progressbar').progressbar( "option", "max", Object.keys(pubtool.project.groups).length );
		$('#progressbar').progressbar( "option", "value", 0 );
		$('.progress-label').html(t("loadingGroupContents"));
		$('#previewImage').attr('src', 'pubtool/img/hyperimage-logo-pubtool.png');

		// get group contents
		var itemsToLoad = {};
		pubtool.project.itemsToLoad = itemsToLoad;
		$(Object.keys(pubtool.project.groups)).each(
			function(i, groupID) {
				var groupContents = [];
				pubtool.service.HIEditor.getGroupContentQuickInfo(
				function(result) {
					var serverContents = result.getReturn();
					$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
					$(serverContents).each(
						function(index, member) {
							var contentID = GetIDModifierFromContentType(member.getContentType())+member.getBaseID();
							if ( member.getContentType() == 'HIObject' || member.getContentType() == 'HIURL' ) itemsToLoad[contentID] = {};
							groupContents.push(contentID);
					});					
					pubtool.project.groups[groupID].members = groupContents;
				
					// load remaining project items when finished
					if ( $('#progressbar').progressbar( "option", "value" ) == Object.keys(pubtool.project.groups).length ) {
						pubtool.project.groupContentsLoaded = true;
						loadProjectContents();
					}
				}, HIExceptionHandler, groupID.substring(1));		
		});
	} else if ( !pubtool.project.projectContentsLoaded ) {
		// load remaining objects and urls
		$('#progressbar').progressbar( "option", "max", Object.keys(pubtool.project.itemsToLoad).length );
		$('#progressbar').progressbar( "option", "value", 0 );
		$('.progress-label').html(t("loadingProjectContents"));
		
		// load objects and external urls
		var itemcount = Object.keys(pubtool.project.itemsToLoad).length;
		for (var i=0; i < Object.keys(pubtool.project.itemsToLoad).length; i++)
			pubtool.service.HIEditor.getBaseElement(
				function(result) {
					parseItem(result.getReturn());
					$('#progressbar').progressbar( "option", "value", $('#progressbar').progressbar( "option", "value" )+1 ); // update progress bar
					if ( $('#progressbar').progressbar( "option", "value" ) == Object.keys(pubtool.project.itemsToLoad).length ) {
						pubtool.project.projectContentsLoaded = true;
						if ( pubtool.userStartedPPGeneration ) generatePeTALDocs();
						console.log("continue");
					}
				}, HIExceptionHandler, Object.keys(pubtool.project.itemsToLoad)[i].substring(1)
			);

	}

}

function plaintext(htmlText) {
	if ( htmlText == null ) return null;
	var html = $.parseHTML(htmlText);
	if ( html != null ) return $(html).text().trim();
					
	return null;
}

function updateSearchIndex(index, text, key, id, stopwords) {
	if ( stopwords == null ) stopwords = pubtool.stopwords['en']; // fallback to english
	if ( text == null ) return;
	text = $.trim(text).replace(/[\x00-\x40\x5b-\x60\x7b-\x7f]/g, ' ').replace(/\s+/g, ' ').toLowerCase();
	if ( text.length == 0 ) return;
	
	$(text.split(' ')).each(function(count, word) {
		if ( word.length < 3 ) return; // only add words with at least 3 characters
		if ( (stopwords.indexOf(word) > -1) ) return; // check if word isn't a stop word

		if ( index[word] == null ) index[word] = {};
		if ( index[word][id] == null ) index[word][id] = [];
		if ( (index[word][id].indexOf(key) == -1) ) index[word][id].push(key);		
	});

}


function generatePeTALDocs() {
	
	function serializeField(tagname, content, language) {
		var field = '<'+tagname;
		if ( language != null && language.length > 0 ) field += ' xml:lang="'+language+'"';
		field += '>'+content+'</'+tagname+'>';
		return field;
	}
	
	function serializeContentPreview(tagname, item) {
		var preview = '<'+tagname+' ref="'+item.id+'"';
		if ( getPeTALType(item) != 'lita' ) preview += ' petalType="'+getPeTALType(item)+'">';
		else preview += ' petalType="lightTable">';
		if ( getPeTALType(item) != 'object' && getPeTALType(item) != 'url' && getPeTALType(item) != 'inscription' )
			for ( var i=0; i < pubtool.project.langs.length; i++ )
				preview += serializeField('title', item.title[pubtool.project.langs[i]], pubtool.project.langs[i]);
		if ( getPeTALType(item) == 'url' )
			preview += serializeField('title', item.title);
		if ( getPeTALType(item) == 'object' ) {
                    var titleField = 'dc_title';
                    if ( pubtool.project.prefs['ObjectInfoDisplayField'] != null ) 
                        titleField = pubtool.project.prefs['ObjectInfoDisplayField'].replace(/\./g, '_');
                    for ( var i=0; i < pubtool.project.langs.length; i++ )
                        preview += serializeField('title', item.md[pubtool.project.langs[i]][titleField], pubtool.project.langs[i]);
                            
                
                }

		if ( getPeTALType(item) == 'inscription' || getPeTALType(item) == 'text' ) {
			for ( var i=0; i < pubtool.project.langs.length; i++ )
				preview += serializeField('content', $($.parseHTML(item.content[pubtool.project.langs[i]])).text().replace(/\s{2,}/g, ' ').substring(0, 148), pubtool.project.langs[i]);
		} else {
			// generate image tag
			var viewID = null;
			if ( getPeTALType(item) == 'object' ) viewID = item.defaultViewID;
			if ( getPeTALType(item) == 'layer' ) if ( item.polygons.length == 0 ) viewID = item.parent.id; else viewID = item.id;
			if ( getPeTALType(item) == 'view' ) viewID = item.id;
			
			if ( viewID != null && pubtool.project.items[viewID].files != null ) 
				preview += '<img height="'+pubtool.project.items[viewID].files.thumb.height+'" src="img/'+viewID+'_thumb.jpg" use="thumb" width="'+pubtool.project.items[viewID].files.thumb.width+'" />';
			else if ( getPeTALType(item) == 'layer' ) preview += '<img height="128" src="img/'+item.id+'.jpg" use="thumb" width="128" />';
			if ( getPeTALType(item) == 'url' ) preview += '<img height="128" src="img/preview-url.png" use="thumb" width="128" />';
			if ( getPeTALType(item) == 'object' ) preview += '<view ref="'+item.defaultViewID+'" />';
		}
			
		if ( getPeTALType(item) == 'group' ) preview += '<members size="'+item.members.length+'" />';
		if ( getPeTALType(item) == 'lita' ) preview += '<members size="'+item.count+'" />';

		preview += '</'+tagname+'>';
		return preview;
	}
	
	function serializeRefs(tagname, itemTagname, items) {
		var refs = '<'+tagname+'>';		
		for (var i=0; i < Object.keys(items).length; i++) {
			var ref = items[Object.keys(items)[i]];
			refs += serializeContentPreview(itemTagname, ref);
		}				
		refs += '</'+tagname+'>';
		return refs;
	}
	
	function serializeLayer(layer, order) {
		var layerDoc = '<layer id="'+layer.id+'" uuid="'+layer.uuid+'" color="'+layer.color+'" opacity="'+layer.opacity+'" order="'+order+'"';
		if ( layer.ref != null ) layerDoc += ' ref="'+layer.ref+'"';
		layerDoc += '>';
		for ( var i=0; i < pubtool.project.langs.length; i++ ) {
			layerDoc += serializeField('title', layer.title[pubtool.project.langs[i]], pubtool.project.langs[i]);
			layerDoc += serializeField('annotation', layer.annotation[pubtool.project.langs[i]], pubtool.project.langs[i]);
		}
		layerDoc += '<img width="128" height="128" src="img/'+layer.id+'_thumb.jpg" use="thumb" />';		
		for (var i=0; i < layer.polygons.length; i++)
			layerDoc += '<polygon points="'+layer.polygons[i]+'" />';
		
		layerDoc += serializeRefs('sites', 'site', layer.sites);
		layerDoc += serializeRefs('references', 'reference', layer.refs);
		layerDoc += '</layer>';
		return layerDoc;
	}

	function serializeItem(item) {
		var doc = '<?xml version="1.0" encoding="UTF-8"?><petal id="P'+pubtool.project.id+'"><subject ref="'+item.id+'" petalType="'+getPeTALType(item)+'" uuid="'+item.uuid+'"/>';
		
		
		switch (item.id.substring(0,1)) {
			case 'T':
				doc += '<text id="'+item.id+'">';
				for ( var i=0; i < pubtool.project.langs.length; i++ ) {
					doc += serializeField('title', item.title[pubtool.project.langs[i]], pubtool.project.langs[i]);
					doc += serializeField('content', item.content[pubtool.project.langs[i]], pubtool.project.langs[i]);
				}
				break;

			case 'G':
				doc += '<group id="'+item.id+'">';
				for ( var i=0; i < pubtool.project.langs.length; i++ ) {
					doc += serializeField('title', item.title[pubtool.project.langs[i]], pubtool.project.langs[i]);
					doc += serializeField('annotation', item.annotation[pubtool.project.langs[i]], pubtool.project.langs[i]);
				}
				// serialize sorted group contents
				for ( var i=0; i < item.sortOrder.length; i++ ) {
					var sortedItem = item.sortOrder[i];
					var groupMember = null;
					for ( var index=0; index < item.members.length; index++ )
						if ( item.members[index].substring(1) == sortedItem ) groupMember = item.members[index];
					
					if ( groupMember != null )
						doc += serializeContentPreview('member', pubtool.project.items[groupMember]);
				}
	//			for ( var i=0; i < item.members.length; i++ ) {
	//			}
				break;

			case 'O':
			case 'V':
			case 'I':
			case 'L':
				var objectID = item.id;
				var objectUUID = item.uuid;
				if ( getPeTALType(item) == 'view' || getPeTALType(item) == 'inscription' ) {objectID = item.parent.id; objectUUID = item.parent.uuid;}
				if ( getPeTALType(item) == 'layer' ) {objectID = item.parent.parent.id; objectUUID = item.parent.parent.uuid;}
				doc += '<object id="'+objectID+'" uuid="'+objectUUID+'">';
				if ( getPeTALType(item) != 'layer' ) {
					// object metadata
					for (var langID=0; langID < pubtool.project.langs.length; langID++) {
						doc += '<metadata xml:lang="'+pubtool.project.langs[langID]+'">';
						for (var templateID=0; templateID < pubtool.project.templateKeys.length; templateID++) {
							doc += '<record template="T_'+pubtool.project.templateKeys[templateID].id+'_'+pubtool.project.templateKeys[templateID].key+'">';
							for (var keyID=0; keyID < Object.keys(pubtool.project.templates).length; keyID++) {
								var key = pubtool.project.templates[Object.keys(pubtool.project.templates)[keyID]];
								if ( key.template == pubtool.project.templateKeys[templateID].key ) {
									var value = pubtool.project.items[objectID].md[pubtool.project.langs[langID]][Object.keys(pubtool.project.templates)[keyID]];
									if ( value != null && value.length > 0 ) doc += '<value key="'+key.key+'">'+value+'</value>';
								}
							}
							doc += '</record>';
						}
						doc += '</metadata>';
					}
					doc += '<standardView ref="'+pubtool.project.items[objectID].defaultViewID+'" />';
				}
				
				// inscription data
				if ( getPeTALType(item) == 'inscription' ) {
					doc += '<inscription id="'+item.id+'" uuid="'+item.uuid+'">';
					for ( var i=0; i < pubtool.project.langs.length; i++ )
						doc += serializeField('content', item.content[pubtool.project.langs[i]], pubtool.project.langs[i]);
					doc += serializeRefs('siblings', 'sibling', pubtool.project.items[objectID].views);					
					doc += '</inscription>';
				}

				// view data
				if ( getPeTALType(item) == 'view' ) {					
					doc += '<view id="'+item.id+'" uuid="'+item.uuid+'">';
					for ( var i=0; i < pubtool.project.langs.length; i++ ) {
						doc += serializeField('title', item.title[pubtool.project.langs[i]], pubtool.project.langs[i]);
						doc += serializeField('source', item.source[pubtool.project.langs[i]], pubtool.project.langs[i]);
						doc += serializeField('annotation', item.annotation[pubtool.project.langs[i]], pubtool.project.langs[i]);
					}
					for ( var i=0; i < item.files.images.length; i++ )
                                            doc += '<img height="'+item.files.images[i].height+'" src="img/'+item.files.images[i].href+'" use="pict" width="'+item.files.images[i].width+'" />';
                                        if ( item.files.nav != null )
                                            doc += '<img height="'+item.files.nav.height+'" src="img/'+item.files.nav.href+'" use="nav" width="'+item.files.nav.width+'" />';
					doc += '<img height="'+item.files.thumb.height+'" src="img/'+item.files.thumb.href+'" use="thumb" width="'+item.files.thumb.width+'" />';
					
					for ( var i=0; i < item.sortOrder.length; i++ )
						doc += serializeLayer(item.layers[item.sortOrder[i]], (i+1));
					
					doc += serializeRefs('siblings', 'sibling', pubtool.project.items[objectID].views);					
					doc += '</view>';
				}

				// layer data
				if ( getPeTALType(item) == 'layer' ) {
					doc += '<view id="'+item.parent.id+'" uuid="'+item.parent.uuid+'">';
					var sortOrder = 1;
					for ( var i=0; i < item.parent.sortOrder.length; i++ ) if ( item.parent.sortOrder[i] == item.id ) sortOrder = i+1;
					doc += serializeLayer(item, sortOrder);
					doc += '</view>';
				}
				break;

			case 'U':
				doc += '<url id="'+item.id+'" ref="'+item.url+'">';
				doc += serializeField('title', item.title);
				doc += serializeField('annotation', item.annotation);
				doc += '<img width="128" height="128" src="img/'+item.id+'_thumb.jpg" use="thumb" />';
				break;

			case 'X':
				doc += '<lita id="'+item.id+'">';
				doc += item.xml;
				break;
		}
		
		var refItem = item;
		if ( getPeTALType(item) == 'layer' ) refItem = item.parent.parent;
		if ( getPeTALType(item) == 'inscription' || getPeTALType(item) == 'view' ) refItem = item.parent;
		
		doc += serializeRefs('sites', 'site', refItem.sites);
		doc += serializeRefs('references', 'reference', refItem.refs);
//		var ppType = getPeTALType(item); if ( ppType == 'view' || ppType == 'inscription' || ppType == 'layer' ) ppType = 'object';
		doc += '</'+getPeTALType(refItem)+'></petal>';
//		doc = $.parseXML(doc);
		return doc;
	}
	
	// wait for project contents to finish loaded
	if ( !pubtool.project.projectContentsLoaded ) {
		$('#pageinit').show();
		pubtool.userStartedPPGeneration = true;
		return;
	}
	
	$('#pageinit').show();
	$('#progressbar').progressbar({value: false});
	$('.progress-label').html(t("creatingXML"));
	$('#previewImage').attr('src', 'pubtool/img/hyperimage-logo-pubtool.png');

	/*
	 * GENERATE start.xml
	 */
	pubtool.docs = {};
	pubtool.docs.start = $.parseXML('<?xml version="1.0" encoding="UTF-8"?><petal id="start"><subject ref="start" petalType="start" /><project id="'+pubtool.project.id+'" path="postPetal/" /><link ref="'+pubtool.project.id+'" /></petal>');

	/*
	 * GENERATE postPetal / P{id}.xml
	 */
	
	pubtool.docs.project = '<?xml version="1.0" encoding="UTF-8"?><petal id="'+pubtool.project.id+'"><subject ref="'+pubtool.project.id+'" petalType="project" />';
	for ( var i=0; i < pubtool.project.langs.length ; i++  ) {
		pubtool.docs.project += '<language'; if ( pubtool.project.defaultLang == pubtool.project.langs[i] ) pubtool.docs.project += ' standard="true"';
		pubtool.docs.project += '>'+pubtool.project.langs[i]+'</language>';
	}
	for ( var i=0; i < pubtool.project.langs.length ; i++  )
		pubtool.docs.project += '<title xml:lang="'+pubtool.project.langs[i]+'">'+pubtool.project.title[pubtool.project.langs[i]]+'</title>';
	pubtool.docs.project += '<link ref="'+pubtool.start+'" />';

	// persist user selected metadata sort order
	pubtool.project.sortedFields = $('#metadataSortable').sortable('toArray', {attribute: 'data-key'});
	for (var i=0; i < pubtool.project.templateKeys.length; i++) {
		pubtool.docs.project += '<template id="T_'+pubtool.project.templateKeys[i].id+'_'+pubtool.project.templateKeys[i].key+'">';
		for (var rank=0; rank < pubtool.project.sortedFields.length ; rank++) {
			if ( pubtool.project.sortedFields[rank].split('_')[0] == pubtool.project.templateKeys[i].key ) {
				var key = pubtool.project.sortedFields[rank].split('_')[1];
				pubtool.docs.project += '<key tagName="'+key+'" rank="'+(rank+1)+'">';
				for (var lang in pubtool.project.langs)
					if ( pubtool.project.templates[pubtool.project.sortedFields[rank]][pubtool.project.langs[lang]] )
						pubtool.docs.project += '<displayName xml:lang="'+pubtool.project.langs[lang]+'">'+pubtool.project.templates[pubtool.project.sortedFields[rank]][pubtool.project.langs[lang]]+'</displayName>';
				pubtool.docs.project += '</key>';
			}	
		}
		pubtool.docs.project += '</template>';
	}
	for (var lang in pubtool.project.langs)
		pubtool.docs.project += '<index xml:lang="'+pubtool.project.langs[lang]+'"><file>index_'+pubtool.project.langs[lang]+'.xml</file></index>';


	// persist user selected text menu sort order and visibility
	for (var lang in pubtool.project.langs) {
		pubtool.docs.project += '<menu key="text" xml:lang="'+pubtool.project.langs[lang]+'">';
		var sortedTextList = $('#textSortable').sortable('toArray', {attribute: 'data-baseid'});
		for (var i=0; i < sortedTextList.length ; i++)
			if ( $('#textSortable > li > input[name="'+sortedTextList[i]+'"]').prop('checked') ) { // only add visible items to menu
				var id = sortedTextList[i];
				pubtool.docs.project += '<item ref="'+id+'">'+pubtool.project.texts[id].title[pubtool.project.langs[lang]]+'</item>';		
			}
		pubtool.docs.project += '</menu>';
	}

	// persist user selected group menu sort order and visibility
	for (var lang in pubtool.project.langs) {
		pubtool.docs.project += '<menu key="group" xml:lang="'+pubtool.project.langs[lang]+'">';
		var sortedGroupList = $('#groupSortable').sortable('toArray', {attribute: 'data-baseid'});
		for (var i=0; i < sortedGroupList.length ; i++)
			if ( $('#groupSortable > li > input[name="'+sortedGroupList[i]+'"]').prop('checked') ) { // only add visible items to menu
				var id = sortedGroupList[i];
				pubtool.docs.project += '<item ref="'+id+'">'+pubtool.project.groups[id].title[pubtool.project.langs[lang]]+'</item>';		
			}
		pubtool.docs.project += '</menu>';
	}

	// persist user selected light table menu sort order and visibility
	for (var lang in pubtool.project.langs) {
		pubtool.docs.project += '<menu key="lita" xml:lang="'+pubtool.project.langs[lang]+'">';
		var sortedLitaList = $('#litaSortable').sortable('toArray', {attribute: 'data-baseid'});
		for (var i=0; i < sortedLitaList.length ; i++)
			if ( $('#litaSortable > li > input[name="'+sortedLitaList[i]+'"]').prop('checked') ) { // only add visible items to menu
				var id = sortedLitaList[i];
				pubtool.docs.project += '<item ref="'+id+'">'+pubtool.project.litas[id].title[pubtool.project.langs[lang]]+'</item>';		
			}
		pubtool.docs.project += '</menu>';
	}
	pubtool.docs.project += '</petal>';
	pubtool.docs.project = $.parseXML(pubtool.docs.project);
	
	
	
	// gather links (refs) and backlinks (sites)
	for (var i=0; i < Object.keys(pubtool.project.items).length; i++) {
		var item = pubtool.project.items[Object.keys(pubtool.project.items)[i]];
		item.refs = getItemLinks(item);
		for (var linkID=0; linkID < Object.keys(item.refs).length; linkID++ )
			pubtool.project.items[Object.keys(item.refs)[linkID]].sites[item.id] = item;
	}

	// serialize items
	pubtool.docs.items = {};
	for (var i=0; i < Object.keys(pubtool.project.items).length; i++) {
		var item = pubtool.project.items[Object.keys(pubtool.project.items)[i]];
		pubtool.docs.items[item.id] = serializeItem(item);
	}
	
	// generate search index
	pubtool.project.index = {};
	for (lang in pubtool.project.langs) {
		pubtool.project.index[pubtool.project.langs[lang]] = {};
		
		for (var i=0; i < Object.keys(pubtool.project.items).length; i++) {
			var item = pubtool.project.items[Object.keys(pubtool.project.items)[i]];
			switch (item.type) {
				case 'layer':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title[pubtool.project.langs[lang]], 'layer_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.annotation[pubtool.project.langs[lang]]), 'layer_annotation', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;
					
				case 'group':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title[pubtool.project.langs[lang]], 'group_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.annotation[pubtool.project.langs[lang]]), 'group_annotation', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;
					
				case 'view':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title[pubtool.project.langs[lang]], 'view_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.annotation[pubtool.project.langs[lang]]), 'view_annotation', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.source[pubtool.project.langs[lang]], 'view_source', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;

				case 'object':
					for (var key in pubtool.project.sortedFields) {
						var field = pubtool.project.sortedFields[key];
						var template = pubtool.project.templates[field];
						var id="";
						$(pubtool.project.templateKeys).each(function(keyIndex,tKey) {if ( tKey.key == template.template ) id = tKey.id;});
						var fieldContents = item.md[pubtool.project.langs[lang]][field];
						if ( template.richText ) fieldContents = plaintext(fieldContents);
						updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], fieldContents, 'T_'+id+'_'+field, item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					}
					break;
					
				case 'inscription':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.content[pubtool.project.langs[lang]]), 'object_inscription', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;

				case 'text':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title[pubtool.project.langs[lang]], 'text_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.content[pubtool.project.langs[lang]]), 'text_content', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;
					
				case 'url':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title, 'url_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], plaintext(item.annotation), 'url_annotation', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					break;
					
				case 'lita':
					updateSearchIndex(pubtool.project.index[pubtool.project.langs[lang]], item.title[pubtool.project.langs[lang]], 'lita_title', item.id, pubtool.stopwords[pubtool.project.langs[lang]]);
					// TODO lita annotation
					break;
					
					break;

				default:
					console.log('INDEX: unknown item: ', item);
			}
			
		}
	}
	
	// serialize search index
	for (langID in pubtool.project.langs) {
		var submap = ['text_title', 'text_content', 'object_inscription'];
		var count = 4;
		var lang = pubtool.project.langs[langID];
		var xmlIndex = '<?xml version="1.0" encoding="UTF-8"?><petal id="'+pubtool.project.id+'"><subject ref="none" petalType="index" />';
		xmlIndex += '<index xml:lang="'+lang+'">';
		xmlIndex += '<item key="text"><item key="text_title" substitute="1" /><item key="text_content" substitute="2" /></item>';
		xmlIndex += '<item key="object"><item key="object_inscription" substitute="3" />';
		for (var i in pubtool.project.sortedFields) {
			var field = pubtool.project.sortedFields[i];
			var template = pubtool.project.templates[field];
			var id="";
			$(pubtool.project.templateKeys).each(function(keyIndex,tKey) {if ( tKey.key == template.template ) id = tKey.id;});
			xmlIndex += '<item key="T_'+id+'_'+field+'" substitute="'+count+'" caption="'+template[lang]+'" />';
			submap.push('T_'+id+'_'+field);
			count++;
		}
		xmlIndex += '<item key="view">';
		xmlIndex += '<item key="view_title" substitute="'+count+'" />'; count++; submap.push('view_title');
		xmlIndex += '<item key="view_annotation" substitute="'+count+'" />'; count++; submap.push('view_annotation');
		xmlIndex += '<item key="view_source" substitute="'+count+'" />'; count++; submap.push('view_source');
		xmlIndex += '<item key="layer">';
		xmlIndex += '<item key="layer_title" substitute="'+count+'" />'; count++; submap.push('layer_title');
		xmlIndex += '<item key="layer_annotation" substitute="'+count+'" />'; count++; submap.push('layer_annotation');
		xmlIndex += '</item></item></item><item key="lita">';
		xmlIndex += '<item key="lita_title" substitute="'+count+'" />'; count++; submap.push('lita_title');
		xmlIndex += '<item key="lita_annotation" substitute="'+count+'" />'; count++; submap.push('lita_annotation');
		xmlIndex += '</item><item key="url">';
		xmlIndex += '<item key="url_title" substitute="'+count+'" />'; count++; submap.push('url_title');
		xmlIndex += '<item key="url_annotation" substitute="'+count+'" />'; count++; submap.push('url_annotation');
		xmlIndex += '</item><item key="group">';
		xmlIndex += '<item key="group_title" substitute="'+count+'" />'; count++; submap.push('group_title');
		xmlIndex += '<item key="group_annotation" substitute="'+count+'" />'; count++; submap.push('group_annotation');
		xmlIndex += '</item>';
		
		xmlIndex += '<table>';
		for ( var wordIndex=0; wordIndex < Object.keys(pubtool.project.index[lang]).length; wordIndex++ ) {
			var word = Object.keys(pubtool.project.index[lang])[wordIndex];
			xmlIndex += '<entry str="'+word+'">';
			for ( var itemIndex=0; itemIndex < Object.keys(pubtool.project.index[lang][word]).length; itemIndex++) {
				var item = Object.keys(pubtool.project.index[lang][word])[itemIndex];
				var foundFields = pubtool.project.index[lang][word][item];
				xmlIndex += '<rec ref="'+item+'" key="';
				for ( var fIndex=0; fIndex < foundFields.length; fIndex++  ) {
					xmlIndex += submap.indexOf(foundFields[fIndex])+1;
					if ( fIndex < (foundFields.length-1) ) xmlIndex += ',';
				}
				xmlIndex += '" />';
			}
			
			xmlIndex += '</entry>';			
		}
		
		xmlIndex += '</table></index></petal>';

		pubtool.zip.file("postPetal/index_"+lang+".xml", xmlIndex);
	}

	// persist user selected theme to PeTAL xml
	pubtool.zip.file("resource/hi_prefs.xml", pubtool.hiThemes[$("#combobox").data('ddslick').selectedIndex]);
	
	pubtool.zip.file("start.xml", pubtool.serializer.serializeToString(pubtool.docs.start));
	pubtool.zip.folder('img');
	pubtool.zip.folder('postPetal');
	pubtool.zip.file("postPetal/"+pubtool.project.id+".xml", pubtool.serializer.serializeToString(pubtool.docs.project));
	for (var i=0; i < Object.keys(pubtool.docs.items).length; i++) {
		var item = pubtool.project.items[Object.keys(pubtool.docs.items)[i]];
		pubtool.zip.file("postPetal/"+Object.keys(pubtool.docs.items)[i]+".xml", pubtool.docs.items[Object.keys(pubtool.docs.items)[i]]);
		if ( item != null ) if ( item.uuid != null )
			pubtool.zip.file("postPetal/"+item.uuid+".xml", pubtool.docs.items[Object.keys(pubtool.docs.items)[i]]);
		
	}

	generateImageFiles();

}

window.stopwords = {
	de: ["ab", "aber", "all", "alle", "allem", "allen", "aller", "allerdings", "alles", "als", "alsdann", "also", "am", "an", "ander", "andere", "anderem", "anderen", "anderer", "anderes", "andern", "anders", "auch", "auf", "aus", "ausser", "ausserdem", "äußerst", "aeusserst", "bei", "beide", "beiden", "beider", "beides", "beim", "bereits", "besteht", "bevor", "bin", "bis", "bisher", "bist", "bloss", "brauchen", "braucht", "brauchte", "brauchten", "brauchtest", "bsp", "da", "dabei", "dadurch", "dagegen", "daher", "damit", "dann", "dar", "daran", "darf", "darum", "das", "dasjenige", "daß", "dass", "dasselbe", "davon", "dazu", "dein", "deine", "deinem", "deinen", "deiner", "deinerseits", "deines", "dem", "demselben", "demselbigen", "den", "denn", "denselben", "denselbigen", "der", "derer", "derselbe", "derselben", "derselbige", "derselbigen", "des", "deshalb", "desselben", "desselbigen", "dessen", "deswegen", "dich", "die", "dies", "diese", "diesem", "diesen", "dieser", "dieses", "dir", "doch", "dort", "dran", "du", "duerfen", "dürfen", "durch", "durfte", "durften", "ebenfalls", "ebenso", "ein", "eine", "einem", "einen", "einer", "eines", "einig", "einige", "einigem", "einigen", "einiges", "einzig", "entweder", "er", "erst", "es", "etwa", "etwaig", "etwaige", "etwaigen", "etwaiger", "etwas", "eurer", "eure", "eurem", "euren", "eurer", "eures", "falls", "fast", "ferner", "folgende", "folgendem", "folgenden", "folgender", "folgendes", "folglich", "folgt", "für", "fuer", "ganz", "ganze", "ganzem", "ganzen", "ganzer", "gänze", "gänzlich", "gegen", "gehabt", "gekonnt", "gemäß", "gemaess", "getan", "gewesen", "geworden", "gibt", "hab", "habe", "haben", "hätte", "haette", "hätten", "haetten", "hat", "hatte", "hatten", "her", "herauf", "heraus", "herein", "herunter", "hie", "hier", "hin", "hinauf", "hinaus", "hinein", "hinunter", "hinter", "ich", "ihm", "ihn", "ihnen", "ihr", "ihre", "ihrem", "ihren", "ihrer", "ihres", "im", "immer", "immerfort", "immerzu", "in", "indem", "indes", "infolge", "innen", "innerhalb", "ins", "inzwischen", "irgend", "irgendein", "irgendetwas", "irgendwas", "irgendwem", "irgendwen", "irgendwer", "irgendwie", "irgendwo", "ist", "je", "jede", "jedem", "jeden", "jeder", "jedes", "jedoch", "jene", "jenem", "jenen", "jener", "jenes", "kann", "kein", "keine", "keinem", "keinen", "keiner", "keines", "können", "koennen", "könnte", "koennte", "könnten", "koennten", "konnte", "konnten", "machen", "machte", "machten", "man", "manch", "manche", "mancher", "manches", "mehr", "mein", "meine", "meinen", "meinem", "meiner", "meines", "meist", "meiste", "meisten", "mich", "mir", "mit", "möchte", "moechte", "möchten", "moechten", "müssen", "muessen", "müsst", "muesst", "müsste", "muesste", "müssten", "muessten", "muss", "musste", "mussten", "nach", "nachdem", "nacher", "nämlich", "naemlich", "neben", "nebst", "nein", "nicht", "nichts", "noch", "nützt", "nuetzt", "nützte", "nuetzte", "nützten", "nuetzten", "nun", "nur", "nutzt", "ob", "obgleich", "obwohl", "oder", "ohne", "ohnehin", "per", "pro", "schon", "sehr", "sei", "seid", "sein", "seine", "seinem", "seinen", "seiner", "seines", "seinesgleichen", "seit", "seitdem", "seither", "selber", "selbst", "sich", "sie", "siehe", "sind", "so", "sobald", "sofern", "solange", "solch", "solche", "solchem", "solchen", "solcher", "solches", "soll", "sollen", "sollte", "sollten", "somit", "sondern", "sonst", "soweit", "sowie", "später", "spaeter", "stets", "such", "über", "ueber", "übrig", "uebrig", "übrige", "uebrige", "übrigen", "uebrigen", "übrigens", "uebrigens", "um", "ums", "und", "uns", "unser", "unsere", "unserem", "unseren", "unserer", "unseres", "viel", "viele", "vielen", "vieles", "vom", "von", "vor", "vorbei", "vorher", "vorueber", "während", "waehrend", "wäre", "waere", "wären", "waeren", "wann", "war", "waren", "ward", "warst", "warum", "was", "weder", "wegen", "weil", "weiter", "weitere", "weiterem", "weiteren", "weiterer", "weiteres", "weiterhin", "welche", "welchem", "welchen", "welcher", "welches", "wem", "wen", "wenigstens", "wenn", "wenngleich", "wer", "werd", "werde", "werden", "werdet", "weshalb", "wessen", "wie", "wieder", "will", "wir", "wird", "wo", "wodurch", "woher", "wohin", "wohl", "wollen", "wollt", "wollte", "wollten", "wolltest", "wolltet", "worden", "worin", "würde", "wuerde", "würden", "wuerden", "wurde", "wurden", "zb", "zu", "zufolge", "zum", "zusammen", "zur", "zwar", "zwischen"], 
	en: ["able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "ain", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "aren", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "mon", "came", "can", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn", "course", "currently", "definitely", "described", "despite", "did", "didn", "different", "do", "does", "doesn", "doing", "don", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "except", "far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadn", "happens", "hardly", "has", "hasn", "have", "haven", "having", "he", "hello", "help", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isn", "it", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "like", "liked", "likely", "little", "ll", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "non", "none", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldn", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "ve", "very", "via", "viz", "vs", "want", "wants", "was", "wasn", "way", "we", "welcome", "well", "went", "were", "weren", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "won", "wonder", "would", "would", "wouldn", "yes", "yet", "you", "your", "yours", "yourself", "yourselves"]
};
window.hiThemes = [
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#FFFFFF"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#FFFFFF"/><pref key="INFOLINE_COLOR_HI" val="#f0f0a2"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#00a2ff"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#00a2ff"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#FFFFFF"/><pref key="TABS_COLOR_DESEL" val="#eeeeee"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#fff099"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#68b7df"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#EEEEEE"/><pref key="MENU_COLOR_ACTIVE" val="#EEEEEE"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#ffffff"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#FFFFFF"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#ffffff"/><pref key="LITA_INFOLINE_COLOR" val="#f0f0a2"/><pref key="LITA_INFOLINE_COLOR_HI" val="#f0f0a2"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#f0f0a2"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#eeeeee"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#000000"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#000000"/><pref key="INFOLINE_COLOR_HI" val="#495e80"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#555555"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#7d9dd1"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#ffffff"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#7d9dd1"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#000000"/><pref key="TABS_COLOR_DESEL" val="#555555"/><pref key="TABS_COLOR_BORDER" val="#000000"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#555555"/><pref key="TABS_INPUT_COLOR" val="#777777"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#ffffff"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#7d9dd1"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#495e80"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#555555"/><pref key="MENU_COLOR_ACTIVE" val="#555555"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#333333"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#ffffff"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#333333"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#ffffff"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#ffffff"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#000000"/><pref key="LITA_INFOLINE_COLOR" val="#495e80"/><pref key="LITA_INFOLINE_COLOR_HI" val="#495e80"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#ffffff"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#555555"/><pref key="LITA_COLOR_HEADSEL" val="#7d9dd1"/><pref key="LITA_COLOR_TOOL1" val="#000000"/><pref key="LITA_COLOR_TOOL2" val="#ffffff"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#ffffff"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#ffffff"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#555555"/><pref key="DIALOG_INPUT_COLOR" val="#999999"/><pref key="DIALOG_BUTTON_COLOR" val="#777777"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#7d9dd1"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#7d9dd1"/><pref key="INFOLINE_COLOR_HI" val="#495e80"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#dde24d"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#ffffff"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#dde24d"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#7d9dd1"/><pref key="TABS_COLOR_DESEL" val="#617da9"/><pref key="TABS_COLOR_BORDER" val="#7d9dd1"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#617da9"/><pref key="TABS_INPUT_COLOR" val="#7d9dd1"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#495e80"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#ffffff"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#dde24d"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#495e80"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#495e80"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#617da9"/><pref key="MENU_COLOR_ACTIVE" val="#617da9"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#7d9dd1"/><pref key="MENU_COLOR_STROKE" val="#cccccc"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#ffffff"/><pref key="MENUTEXT_COLOR_DEACT" val="#444444"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#7d9dd1"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#ffffff"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#ffffff"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#7d9dd1"/><pref key="LITA_INFOLINE_COLOR" val="#495e80"/><pref key="LITA_INFOLINE_COLOR_HI" val="#495e80"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#ffffff"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#617da9"/><pref key="LITA_COLOR_HEADSEL" val="#495e80"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#ffffff"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#ffffff"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#617da9"/><pref key="DIALOG_INPUT_COLOR" val="#7d9dd1"/><pref key="DIALOG_BUTTON_COLOR" val="#495e80"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#ded6c9"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#b5ada0"/><pref key="INFOLINE_COLOR_HI" val="#852023"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#852023"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#6c6457"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#852023"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#ded6c9"/><pref key="TABS_COLOR_DESEL" val="#ebe6de"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#ded6c9"/><pref key="TABS_INPUT_COLOR" val="#ded6c9"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#6c6457"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#6c6457"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#852023"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#ffffff"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#852023"/><pref key="PROBAR_COLOR_BG" val="#ded6c9"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#ebe6de"/><pref key="MENU_COLOR_ACTIVE" val="#ebe6de"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#ffffff"/><pref key="MENU_COLOR_STROKE" val="#b5ada0"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#6c6457"/><pref key="MENUTEXT_COLOR_DEACT" val="#b5ada0"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#ded6c9"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#6c6457"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#ded6c9"/><pref key="LITA_INFOLINE_COLOR" val="#852023"/><pref key="LITA_INFOLINE_COLOR_HI" val="#852023"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#ffffff"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#b5ada0"/><pref key="LITA_COLOR_HEADSEL" val="#852023"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#ffffff"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#6c6457"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#ded6c9"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#6c6457"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#565656"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#565656"/><pref key="INFOLINE_COLOR_HI" val="#cccc99"/><pref key="SCROLLBAR_COLOR" val="#333333"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#999999"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#cccc99"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#ffffff"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#cccc99"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#565656"/><pref key="TABS_COLOR_DESEL" val="#333333"/><pref key="TABS_COLOR_BORDER" val="#565656"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#333333"/><pref key="TABS_INPUT_COLOR" val="#999999"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#cccccc"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#ffffff"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#cccc99"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#cccc99"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#333333"/><pref key="MENU_COLOR_ACTIVE" val="#333333"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#777777"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#ffffff"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#606060"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#ffffff"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#ffffff"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#565656"/><pref key="LITA_INFOLINE_COLOR" val="#cccc99"/><pref key="LITA_INFOLINE_COLOR_HI" val="#abab80"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#ffffff"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#333333"/><pref key="LITA_COLOR_HEADSEL" val="#777777"/><pref key="LITA_COLOR_TOOL1" val="#000000"/><pref key="LITA_COLOR_TOOL2" val="#ffffff"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#ffffff"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#ffffff"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#333333"/><pref key="DIALOG_INPUT_COLOR" val="#777777"/><pref key="DIALOG_BUTTON_COLOR" val="#565656"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#777777"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#777777"/><pref key="INFOLINE_COLOR_HI" val="#ffcda5"/><pref key="SCROLLBAR_COLOR" val="#333333"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#999999"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#ffcda5"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#ffffff"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#ffcda5"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#777777"/><pref key="TABS_COLOR_DESEL" val="#444444"/><pref key="TABS_COLOR_BORDER" val="#777777"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#444444"/><pref key="TABS_INPUT_COLOR" val="#999999"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#cccccc"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#ffffff"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#ffcda5"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#ffcda5"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#444444"/><pref key="MENU_COLOR_ACTIVE" val="#444444"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#777777"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#ffffff"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#606060"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#ffffff"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#ffffff"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#777777"/><pref key="LITA_INFOLINE_COLOR" val="#ffcda5"/><pref key="LITA_INFOLINE_COLOR_HI" val="#ffcda5"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#cccccc"/><pref key="LITA_COLOR_HEADSEL" val="#ffcda5"/><pref key="LITA_COLOR_TOOL1" val="#000000"/><pref key="LITA_COLOR_TOOL2" val="#ffffff"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#ffffff"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#444444"/><pref key="DIALOG_INPUT_COLOR" val="#777777"/><pref key="DIALOG_BUTTON_COLOR" val="#777777"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#9e0057"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#9e0057"/><pref key="INFOLINE_COLOR_HI" val="#e5007c"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#ffff99"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#FFFFFF"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#ffff99"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#9e0057"/><pref key="TABS_COLOR_DESEL" val="#5d1231"/><pref key="TABS_COLOR_BORDER" val="#9e0057"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#5d1231"/><pref key="TABS_INPUT_COLOR" val="#9e0057"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#5d1231"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#ffffff"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#5d1231"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#ffffff"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#e5007c"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#ffffff"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#5d1231"/><pref key="MENU_COLOR_ACTIVE" val="#5d1231"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#9e0057"/><pref key="MENU_COLOR_STROKE" val="#9e0057"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#ffffff"/><pref key="MENUTEXT_COLOR_DEACT" val="#e5007c"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#9e0057"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#ffffff"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#ffffff"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#9e0057"/><pref key="LITA_INFOLINE_COLOR" val="#e5007c"/><pref key="LITA_INFOLINE_COLOR_HI" val="#e5007c"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#ffffff"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#5d1231"/><pref key="LITA_COLOR_HEADSEL" val="#e5007c"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#ffffff"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#ffffff"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#5d1231"/><pref key="DIALOG_INPUT_COLOR" val="#e5007c"/><pref key="DIALOG_BUTTON_COLOR" val="#9e0057"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#f0f0a2"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#f0f0a2"/><pref key="INFOLINE_COLOR_HI" val="#68b7df"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#00a2ff"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#00a2ff"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#f0f0a2"/><pref key="TABS_COLOR_DESEL" val="#ffffff"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#ffffff"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#68b7df"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#ffffff"/><pref key="MENU_COLOR_ACTIVE" val="#FFFFFF"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#f0f0a2"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#f0f0a2"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#f0f0a2"/><pref key="LITA_INFOLINE_COLOR" val="#68b7df"/><pref key="LITA_INFOLINE_COLOR_HI" val="#68b7df"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#68b7df"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#eeeeee"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#eeeee0"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#ccccc0"/><pref key="INFOLINE_COLOR_HI" val="#68b7df"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#00a2ff"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#00a2ff"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#eeeee0"/><pref key="TABS_COLOR_DESEL" val="#ccccc0"/><pref key="TABS_COLOR_BORDER" val="#FFFFFF"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#68b7df"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#68b7df"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#ffffff"/><pref key="MENU_COLOR_ACTIVE" val="#FFFFFF"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#ccccc0"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#eeeee0"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#eeeee0"/><pref key="LITA_INFOLINE_COLOR" val="#68b7df"/><pref key="LITA_INFOLINE_COLOR_HI" val="#68b7df"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#ccccc0"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#ccccc0"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#f0ecea"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#f0ecea"/><pref key="INFOLINE_COLOR_HI" val="#cfbcbd"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#9e0057"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#9e0057"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#f0ecea"/><pref key="TABS_COLOR_DESEL" val="#ffffff"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#f0ecea"/><pref key="TABS_INPUT_COLOR" val="#f0ecea"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#9e0057"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#ffffff"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#cfbcbd"/><pref key="PROBAR_COLOR_BG" val="#f0ecea"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#ffffff"/><pref key="MENU_COLOR_ACTIVE" val="#ffffff"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#f0ecea"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#f0ecea"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#f0ecea"/><pref key="LITA_INFOLINE_COLOR" val="#cfbcbd"/><pref key="LITA_INFOLINE_COLOR_HI" val="#cfbcbd"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#cfbcbd"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#f0ecea"/><pref key="DIALOG_INPUT_COLOR" val="#ffffff"/><pref key="DIALOG_BUTTON_COLOR" val="#ffffff"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#FFFFFF"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#FFFFFF"/><pref key="INFOLINE_COLOR_HI" val="#ffae6c"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#9e0057"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#9e0057"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#FFFFFF"/><pref key="TABS_COLOR_DESEL" val="#f0ecea"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#f0ecea"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#f0ecea"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#f0ecea"/><pref key="MENU_COLOR_ACTIVE" val="#FFFFFF"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#f0ecea"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#FFFFFF"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#ffffff"/><pref key="LITA_INFOLINE_COLOR" val="#ffae6c"/><pref key="LITA_INFOLINE_COLOR_HI" val="#ffae6c"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#f0ecea"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#f0ecea"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#FFFFFF"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#FFFFFF"/><pref key="INFOLINE_COLOR_HI" val="#dfd0d6"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#9e0057"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#9e0057"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#FFFFFF"/><pref key="TABS_COLOR_DESEL" val="#eeeeee"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#dfd0d6"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#dfd0d6"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#EEEEEE"/><pref key="MENU_COLOR_ACTIVE" val="#EEEEEE"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#ffffff"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#FFFFFF"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#ffffff"/><pref key="LITA_INFOLINE_COLOR" val="#dfd0d6"/><pref key="LITA_INFOLINE_COLOR_HI" val="#9e0057"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#dfd0d6"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#eeeeee"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>',
	'<?xml version="1.0" encoding="UTF-8"?><prefs><pref key="INITIAL_REF" val="start.xml"/><pref key="OVERLAYS_OPACITY" val="0.94"/><pref key="BG_COLOR" val="#FFFFFF"/><pref key="SHADOW_COLOR" val="#000000"/><pref key="INFOLINE_COLOR" val="#FFFFFF"/><pref key="INFOLINE_COLOR_HI" val="#f0f0a2"/><pref key="SCROLLBAR_COLOR" val="#444444"/><pref key="SCROLLBAR_COLOR_SLIDER" val="#AAAAAA"/><pref key="TEXT_WIDTH" val="640"/><pref key="BACKFORTH_COLOR" val="#00a2ff"/><pref key="INFOLINE_METADATA_NUM" val="3"/><pref key="INFOLINE_METADATA_VIEW" val="true"/><pref key="MAINTEXT_FONT" val="Verdana"/><pref key="MAINTEXT_SIZE" val="12"/><pref key="MAINTEXT_COLOR" val="#000000"/><pref key="MAINTEXT_BOLD" val="false"/><pref key="MAINTEXT_ITALIC" val="false"/><pref key="MAINTEXT_UNDERLINE" val="false"/><pref key="MAINTEXT_LETTERSPACING" val="0"/><pref key="MAINTEXT_LINK_COLOR" val="#00a2ff"/><pref key="MAINTEXT_LINK_BOLD" val="false"/><pref key="MAINTEXT_LINK_ITALIC" val="false"/><pref key="MAINTEXT_LINK_UNDERLINE" val="true"/><pref key="TABS_COLOR" val="#FFFFFF"/><pref key="TABS_COLOR_DESEL" val="#eeeeee"/><pref key="TABS_COLOR_BORDER" val="#CCCCCC"/><pref key="TABS_STANDARD" val="1"/><pref key="TABS_BUTTON_COLOR" val="#FFFFFF"/><pref key="TABS_INPUT_COLOR" val="#FFFFFF"/><pref key="TABTITLE_FONT" val="Verdana"/><pref key="TABTITLE_SIZE" val="12"/><pref key="TABTITLE_COLOR" val="#999999"/><pref key="TABTITLE_BOLD" val="true"/><pref key="TABTITLE_ITALIC" val="false"/><pref key="TABTITLE_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_FONT" val="Verdana"/><pref key="SEARCH_ITEM_SIZE" val="10"/><pref key="SEARCH_ITEM_COLOR" val="#000000"/><pref key="SEARCH_ITEM_BOLD" val="false"/><pref key="SEARCH_ITEM_ITALIC" val="false"/><pref key="SEARCH_ITEM_UNDERLINE" val="false"/><pref key="SEARCH_ITEM_LETTERSPACING" val="0"/><pref key="TOOLTIP_COLOR" val="#68b7df"/><pref key="TOOLTIP_WIDTH" val="240"/><pref key="TOOLTIPTEXT_FONT" val="Verdana"/><pref key="TOOLTIPTEXT_SIZE" val="12"/><pref key="TOOLTIPTEXT_COLOR" val="#000000"/><pref key="TOOLTIPTEXT_BOLD" val="false"/><pref key="TOOLTIPTEXT_ITALIC" val="false"/><pref key="TOOLTIPTEXT_UNDERLINE" val="false"/><pref key="TOOLTIPTEXT_LETTERSPACING" val="0"/><pref key="PROBAR_COLOR" val="#68b7df"/><pref key="PROBAR_COLOR_BG" val="#FFFFFF"/><pref key="PROBAR_COLOR_BORDER" val="#777777"/><pref key="PROTEXT_FONT" val="Verdana"/><pref key="PROTEXT_SIZE" val="10"/><pref key="PROTEXT_COLOR" val="#000000"/><pref key="PROTEXT_BOLD" val="false"/><pref key="PROTEXT_ITALIC" val="false"/><pref key="PROTEXT_UNDERLINE" val="false"/><pref key="PROTEXT_LETTERSPACING" val="0"/><pref key="MENU_COLOR" val="#EEEEEE"/><pref key="MENU_COLOR_ACTIVE" val="#EEEEEE"/><pref key="MENU_SHADOW_COLOR" val="#000000"/><pref key="MENU_COLOR_HI" val="#ffffff"/><pref key="MENU_COLOR_STROKE" val="#CCCCCC"/><pref key="MENUTEXT_FONT" val="Verdana"/><pref key="MENUTEXT_SIZE" val="12"/><pref key="MENUTEXT_COLOR" val="#000000"/><pref key="MENUTEXT_COLOR_DEACT" val="#999999"/><pref key="MENUTEXT_BOLD" val="false"/><pref key="MENUTEXT_ITALIC" val="false"/><pref key="MENUTEXT_UNDERLINE" val="false"/><pref key="MENUTEXT_LETTERSPACING" val="0"/><pref key="VIEW_CENTER" val="false"/><pref key="VIEW_WHOLE_PICTURE" val="true"/><pref key="VIEW_BG_OPACITY" val="0.13"/><pref key="VIEW_SCROLL_FACTOR" val="1"/><pref key="VIEW_SHOW_LAYER" val="false"/><pref key="LAYER_CENTER" val="false"/><pref key="GROUPMEMBER_COLOR" val="#FFFFFF"/><pref key="GROUPMEMBER_COLOR_BORDER" val="#000000"/><pref key="GROUP_THUMB_FONT" val="Verdana"/><pref key="GROUP_THUMB_SIZE" val="12"/><pref key="GROUP_THUMB_COLOR" val="#000000"/><pref key="GROUP_THUMB_BOLD" val="false"/><pref key="GROUP_THUMB_ITALIC" val="false"/><pref key="GROUP_THUMB_UNDERLINE" val="false"/><pref key="GROUP_THUMB_LETTERSPACING" val="0"/><pref key="LITA_COLOR_BG" val="#ffffff"/><pref key="LITA_INFOLINE_COLOR" val="#f0f0a2"/><pref key="LITA_INFOLINE_COLOR_HI" val="#f0f0a2"/><pref key="LITA_INFO_FONT" val="Verdana"/><pref key="LITA_INFO_SIZE" val="12"/><pref key="LITA_INFO_COLOR" val="#000000"/><pref key="LITA_INFO_BOLD" val="false"/><pref key="LITA_INFO_ITALIC" val="false"/><pref key="LITA_INFO_UNDERLINE" val="false"/><pref key="LITA_INFO_LETTERSPACING" val="0"/><pref key="LITA_COLOR_HEAD" val="#FFFFFF"/><pref key="LITA_COLOR_HEADSEL" val="#f0f0a2"/><pref key="LITA_COLOR_TOOL1" val="#FFFFFF"/><pref key="LITA_COLOR_TOOL2" val="#000000"/><pref key="LITA_SHADOW_COLOR" val="#000000"/><pref key="LITA_HEAD_FONT" val="Verdana"/><pref key="LITA_HEAD_SIZE" val="10"/><pref key="LITA_HEAD_COLOR" val="#000000"/><pref key="LITA_HEAD_BOLD" val="false"/><pref key="LITA_HEAD_ITALIC" val="false"/><pref key="LITA_HEAD_UNDERLINE" val="false"/><pref key="LITA_HEAD_LETTERSPACING" val="0"/><pref key="LITA_ANN_FONT" val="Verdana"/><pref key="LITA_ANN_SIZE" val="12"/><pref key="LITA_ANN_COLOR" val="#000000"/><pref key="LITA_ANN_BOLD" val="false"/><pref key="LITA_ANN_ITALIC" val="false"/><pref key="LITA_ANN_UNDERLINE" val="false"/><pref key="LITA_ANN_LETTERSPACING" val="0"/><pref key="DIALOG_COLOR" val="#eeeeee"/><pref key="DIALOG_INPUT_COLOR" val="#FFFFFF"/><pref key="DIALOG_BUTTON_COLOR" val="#FFFFFF"/><pref key="DIALOGTEXT_FONT" val="Verdana"/><pref key="DIALOGTEXT_SIZE" val="12"/><pref key="DIALOGTEXT_COLOR" val="#000000"/><pref key="DIALOGTEXT_BOLD" val="false"/><pref key="DIALOGTEXT_ITALIC" val="false"/><pref key="DIALOGTEXT_UNDERLINE" val="false"/><pref key="DIALOGTEXT_LETTERSPACING" val="0"/><pref key="MEMORY_CACHE" val="270"/><pref key="MEMORY_DISPLAY" val="false"/></prefs>'
];
