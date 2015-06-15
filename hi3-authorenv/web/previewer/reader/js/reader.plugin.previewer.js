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
 *  http://www.apache.org/licenses/LICENSE-2.0
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

// Avoid `console` errors in browsers that lack a console.
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());


/********************************
 * HyperImage PreViewer Plug-In *
 ********************************/

function isValidUUID(uuid) {
	return /^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$/.test(uuid);
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

function GetQuickInfoType(element) {
	switch (element.getContentType()) {
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
			if ( value == null ) value = '';
			if ( key != 'literal' ) value = value.replace(/\n/g, '<br>');
			switch (key) {
				case 'bold':
					previewText += "<b>"+value+"</b>";
					break;
				case 'italic':
					previewText += "<i>"+value+"</i>";
					break;
				case 'underline':
					previewText += "<u>"+value+"</u>";
					break;
				case 'subscript':
					previewText += "<sub>"+value+"</sub>";
					break;
				case 'superscript':
					previewText += "<sup>"+value+"</sup>";
					break;
				case 'link':
					previewText += "<a onclick=\"javascript:checkExternalLink(this);\" class=\"HIExternalLink\" href=\"#"+ref+"\">"+value+"</a>";
					break;
				case 'literal':
				case 'regular':
					previewText += value;
					break;
			}	
		}
	
	return previewText;
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

function loadSkin(skin) {
	$.ajax({ url: 'reader/skins/'+skin+'/hi_prefs.xml', dataType: 'xml',
		success: function(data, success) {
			loadPrefs(data, success);
		},
		error: null
	});
}

function HIExceptionHandler(status, exception) {
	console.log("HIServiceException:", exception);
	if ( exception.type == "HISessionExpiredException" ) reportError("Session Expired / Sitzung abgelaufen");
}

reader.version = "v3.0.beta2";
reader.productID = 'Reader PreViewer';


// @override
function scanExternalLinks() {
	$('.HIExternalLink').each(function(index, item) {
		if ($(item).attr("href").substring(0,2) == '#U') {
			var href = $(item).attr("href").substring(1, $(item).attr("href").length);
			if ( href.substring(href.length-1, href.length) == '/' ) href = href.substring(0,href.length-1);

			if ( reader.project.items[href] == null ) {
				var baseElement = null;
				reader.service.HIEditor.getBaseElement(function(result) {
					baseElement = result.getReturn();
					parseItem(baseElement, 'success');
					if ( reader.project.items[href] != null ) attachExternalLinkHandler(item, reader.project.items[href]);
				}, HIExceptionHandler, href.substring(1));
			} else attachExternalLinkHandler(item, reader.project.items[href]);

			
		}
		
	});
}

// @override
function checkExternalLink(link) {
	var href = typeof(link) == 'string' ? link : link.getAttribute('href');
	var xmlData = null;
	if ( href.substring(0,2) == '#U' ) {
		href = href.substring(1, href.length);
		if ( href.substring(href.length-1, href.length) == '/' ) href = href.substring(0,href.length-1);
		if ( reader.project.items[href] == null ) {
			var quickinfo = null;
			reader.service.HIEditor.getBaseQuickInfo(function(result) {quickinfo = result.getReturn();}, HIExceptionHandler, href.substring(1));
			if ( quickinfo ) {
				window.open(quickinfo.getPreview(), '_newtab');
				event.preventDefault();
				return true;				
			}
		} else {
			window.open(eader.project.items[href].url, '_newtab');
			event.preventDefault();
			return true;
			
		}
	}
	return false;
}

// @override
function loadViewFileData(element, view, ref) {
    reader.service.HIEditor.synchronous = false;
    element.attr('data-viewwidth', view.files['images'][0].width);
    element.attr('data-viewheight', view.files['images'][0].height);
    reader.service.HIEditor.getImage(
        function (cb) { 
            element.attr('href', 'data:image/jpeg;base64,'+cb.getReturn());
            element.show(); 
        }, 
	function (cb,msg) { console.log("getImage error --> ", cb); HIExceptionHandler(cb, msg);},
            view.id.substring(1),
            ref
    );

    // load view nav image if needed
    if ( (view.files.original.width / view.files.original.height) > 5 ) {
        console.log("loading nav...");
        reader.service.HIEditor.getImage(
            function (cb) { 
                $('#navImage').attr('href', 'data:image/jpeg;base64,'+cb.getReturn());
                reader.canvas.navsvg.change(reader.canvas.navimage, {x:0, y:4, width:reader.zoom.navwidth, height:reader.zoom.navheight});
                reader.canvas.navsvg.change(reader.canvas.navsvg.root(), {x:0, y:0, width:reader.zoom.navwidth, height:reader.zoom.navheight});
                setNavAvailable(true);
                scaleImageTo(reader.zoom.nav); // set initial nav size for canvas
                // DEBUG only for Hachiman / HDH --> start at righthand side of image
                // remove --> move to plugin
                $('#canvas').scrollLeft(reader.zoom.width*reader.zoom.cur);
                updateNavRect();
            }, 
            function (cb,msg) { console.log("nav getImage error --> ", cb); HIExceptionHandler(cb, msg);},
                view.id.substring(1),
            'HI_NAV'
        );        
    
    } else {
        setNavAvailable(false);
    }


    reader.service.HIEditor.synchronous = true;
}

// @override
function loadContentThumbnail(imgTag, indicatorTag, ref, type) {
	reader.service.HIEditor.synchronous = false;
	reader.service.HIEditor.getImage(
		function (cb) { imgTag.attr('src', 'data:image/jpeg;base64,'+cb.getReturn()); indicatorTag.remove(); }, 
		function (cb, msg) { 
			console.log("getImage error --> ", cb, msg);			  
			switch (type) {
				case 'object': imgTag.attr('src', 'reader/icons/preview-noview.png'); break;

				default: imgTag.attr('src', 'reader/icons/preview-loaderror.png');
			}
			HIExceptionHandler(cb, msg);
		},
		ref.substring(1),
		'HI_THUMBNAIL'
	);
	reader.service.HIEditor.synchronous = true;
}


// @override
function findHigherRes(view, element, width, height) {
	if ( view == null || view.type != 'view' ) return;
	var imgWidth = ( element == null ) ? parseInt($("#canvasImage").attr('data-viewwidth')) : parseInt(element.attr('data-viewwidth'));
	var imgHeight = ( element == null ) ? parseInt($("#canvasImage").attr('data-viewheight')) : parseInt(element.attr('data-viewheight'));
	var curRes = null;
	for ( var i=0; i < view.files['images'].length; i++ )
		if ( view.files['images'][i].width == imgWidth && view.files['images'][i].height == imgHeight ) curRes = view.files['images'][i];
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
		if ( newRes.width != imgWidth && newRes.height != imgHeight ) return newRes;
	}
	return null;	
}


// @override
function loadHiResIfNeeded() {
	var view = reader.project.items[reader.viewID];
	var hiRes = findHigherRes(view);
	if ( hiRes == null ) return; // no higher image resolution needed or found

	if ( hiRes.width != view.files['images'][0].width ) setImageLoadingIndicator(true);
	reader.service.HIEditor.synchronous = false;
	$("#canvasImage").attr('data-viewwidth', hiRes.width);
	$("#canvasImage").attr('data-viewheight', hiRes.height);

	if ( hiRes.href != 'HI_SCALE' ) {
		reader.service.HIEditor.getImage(
				function(cb) {
					$("#canvasImage").attr('href', 'data:image/jpeg;base64,'+cb.getReturn());
					$("#canvasImage").show();
					setImageLoadingIndicator(false);
				}, function(cb,msg) { 
					console.log("getImage error --> ", cb); 
					$("#canvasImage").attr('href', 'reader/icons/preview-loaderror.png');
					$("#canvasImage").show();
					setImageLoadingIndicator(false);
					HIExceptionHandler(cb, msg);
				},
				view.id.substring(1),
				hiRes.href
		);
	} else {
		var scale = Math.round(Math.min(view.files['original'].width / hiRes.width, view.files['original'].height / hiRes.height));
		reader.service.HIEditor.getScaledImage(
				function(cb) {
					$("#canvasImage").attr('href', 'data:image/jpeg;base64,'+cb.getReturn());
					$("#canvasImage").show();
					setImageLoadingIndicator(false);
				}, function(cb,msg) { 
					console.log("getImage error --> ", cb); 
					$("#canvasImage").attr('href', 'reader/icons/preview-loaderror.png');
					$("#canvasImage").show();
					setImageLoadingIndicator(false);
					HIExceptionHandler(cb, msg);
				},
				view.id.substring(1),
				scale
		);
	}
	reader.service.HIEditor.synchronous = true;
}


function saveServerTable() {
	if ( reader.table.id == null ) return;
	setLoadingIndicator(true);
	
	window.syncTableToModel();
	// construct XML --> QnD
	var xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	xmlText += "<petal id=\""+reader.project.id+"\">\n"	;
	xmlText += "  <subject ref=\""+reader.table.id+"\" petalType=\"lita\" />\n";
	xmlText += "  <lita id=\""+reader.table.id+"\">\n";
	for (var i=0; i < reader.project.langs.length; i++) {
		var title = '';
		if ( reader.table.title[reader.project.langs[i]] != null ) title = reader.table.title[reader.project.langs[i]];
		xmlText += "    <title xml:lang=\""+reader.project.langs[i]+"\">"+title+"</title>\n";	
	}
	$(reader.table.frames).each(function(index, frame) {
            // convert coordinates to int
            frame.x = Math.round(frame.x);
            frame.y = Math.round(frame.y);
            frame.width = Math.round(frame.width);
            frame.height = Math.round(frame.height);
            // convert frame content to int
            frame.imagePos.x = Math.round(frame.imagePos.x);
            frame.imagePos.y = Math.round(frame.imagePos.y);
            frame.imagePos.width = Math.round(frame.imagePos.width);
            frame.imagePos.height = Math.round(frame.imagePos.height);
            
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
		if ( anString == null ) anString = '';

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
	
	reader.service.HIEditor.getBaseElement(function(result) { 
		var serverTable = result.getReturn();
		var title = '-';
		if ( reader.table.title[reader.project.defaultLang] != null && reader.table.title[reader.project.defaultLang].length > 0 ) title = reader.table.title[reader.project.defaultLang];
		// persist changes to server
		serverTable.setTitle(title);
		serverTable.setXml(xmlText);
		reader.service.HIEditor.updateLightTable(function(result) { console.log("update light table on server: "+result.getReturn());setLoadingIndicator(false);}, HIExceptionHandler, serverTable);
	}, HIExceptionHandler, reader.table.id.substring(1));
	
}


// @override
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
		setMenuItem('saveTableLink', 'javascript:saveServerTable();', reader.table.id != null );

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
	
	function getCaretPosition(element) {
		var caretOffset = 0;
		var doc = element.ownerDocument || element.document;
		var win = doc.defaultView || doc.parentWindow;
		var sel;
		if ( typeof win.getSelection != "undefined") {
			var range = win.getSelection().getRangeAt(0);
			var preCaretRange = range.cloneRange();
			preCaretRange.selectNodeContents(element);
			preCaretRange.setEnd(range.endContainer, range.endOffset);
			caretOffset = preCaretRange.toString().length;
		} else if (( sel = doc.selection) && sel.type != "Control") {
			var textRange = sel.createRange();
			var preCaretTextRange = doc.body.createTextRange();
			preCaretTextRange.moveToElementText(element);
			preCaretTextRange.setEndPoint("EndToEnd", textRange);
			caretOffset = preCaretTextRange.text.length;
		}
		return caretOffset;
	}


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
			$('#'+id+' .ltText').bind('paste', function(e) {
				/*
				e.preventDefault();
				var text = (e.originalEvent || e).clipboardData.getData('text/html') || null;
				if (text == null) text = (e.originalEvent || e).clipboardData.getData('text');
				var html = $.parseHTML(text);
				var pos = getCaretPosition($('.ltText')[0]);
				var contents = $('.ltText').prop('innerHTML');
				if (pos == null) pos = 0;
				
				$.each(html, function(idx, val) {

	                var item = $(val);
	                var insert = '';
					if (item.length > 0) {
                    	item.removeAttr('style');
                    	item.removeAttr('class');
						var tagName = item[0].tagName;
						if ( tagName != null ) tagName = tagName.toLowerCase();
						switch (tagName) {
							case 'div':
							case 'span':
								insert = item.text();
								break;
							case 'b':
								insert = '<b>'+item.text()+"</b>";
								break;
							case 'i':
								insert = '<i>'+item.text()+"</i>";
								break;
							case 'u':
								insert = '<u>'+item.text()+"</u>";
								break;
							case 'br':
								insert = "<br>";
								break;
							case 'a':
								var href = item.attr('href');
								var loc = window.location.origin+window.location.pathname+window.location.search;
								console.log(loc);
								if ( href.startsWith(loc) ) href = href.substring(loc.length);
								insert = '<a onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="'+href+'">'+item.text()+'</a>';
								break;
							default:
								if ( item[0].tagName == null ) insert = item.text();
								else insert = item.prop('outerHTML');
								console.log("unsupported: ", item[0]);
						}
						if ( insert.length > 0 ) {
							contents = [contents.slice(0, pos), insert, contents.slice(pos)].join('');
							pos += insert.length;
						}
    	            }
	            });
        	    $(html).children('style').remove();
    	        $(html).children('meta').remove();
	            $(html).children('link').remove();
	            $('.ltText').html(contents);
	            */
			});
			
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
			$('#'+id+"_content").append('<img data-viewwidth="'+reader.project.items[item.href].files['images'][0].width+'" data-viewheight="'+reader.project.items[item.href].files['images'][0].height+'" src="#" />');
			$('#'+id+"_content img").hide();
			$('#' + id + '_loading').show();
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
		
		if ( view != null ) loadFrameHiRes(id, view, item); // trigger image update
		
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

function convertLegacyLineFormat(field) {
	var htmlText = '';
	var lines = field.getElementsByTagName("line");
	for (var i = 0; i < lines.length; i++)
		if (lines[i].firstChild != null) {
			var line = lines[i];
			if (i > 0)
				htmlText += '<br>';
			$(line).contents().each(function(index, node) {
				if (node.nodeType == 1) {
					switch (node.nodeName) {
						case "b":
							if (node.firstChild != null) {
								htmlText += '<b>';
								$(node).contents().each(function(index, insidenode) {
									if (insidenode.nodeType == 1 && insidenode.nodeName == 'link')
										htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#' + insidenode.getAttribute("ref") + '">' + insidenode.firstChild.nodeValue + '</a>';
									if (insidenode.nodeType == 3)
										htmlText += insidenode.nodeValue;
								});
								htmlText += '</b>';
							}
							break;
						case "i":
							if (node.firstChild != null) {
								htmlText += '<i>';
								$(node).contents().each(function(index, insidenode) {
									if (insidenode.nodeType == 1 && insidenode.nodeName == 'link')
										htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#' + insidenode.getAttribute("ref") + '">' + insidenode.firstChild.nodeValue + '</a>';
									if (insidenode.nodeType == 3)
										htmlText += insidenode.nodeValue;
								});
								htmlText += '</i>';
							}
							break;
						case "u":
							if (node.firstChild != null) {
								htmlText += '<u>';
								$(node).contents().each(function(index, insidenode) {
									if (insidenode.nodeType == 1 && insidenode.nodeName == 'link')
										htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#' + insidenode.getAttribute("ref") + '">' + insidenode.firstChild.nodeValue + '</a>';
									if (insidenode.nodeType == 3)
										htmlText += insidenode.nodeValue;
								});
								htmlText += '</u>';
							}
							break;
						case "link":
							htmlText += '<a  onclick="javascript:checkExternalLink(this);" class="HIExternalLink" href="#' + node.getAttribute("ref") + '">' + node.firstChild.nodeValue + '</a>';
							break;
					}
				} else if (node.nodeType == 3)
					htmlText += node.nodeValue;
			});
		} else if (i > 0)
			htmlText += '<br>';

	return htmlText;
}

// @override
function attachTags(item) {
    if ( item != null && item.tags == null ) {
        // attach tag info
        item.tags = [];
        reader.service.HIEditor.getTagIDsForBase(function (result) {
            item.tags = result.getReturn();
            $(item.tags).each(function(index, tag) { item.tags[index] = reader.project.tags['G'+tag]; });
        }, HIExceptionHandler, item.id.substring(1));
    }
}

// @override
function parseItem(data, success, shouldSetGUI) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);

	var itemType = GetElementType(data);
	var id;
	var item = null;
	var unloaded = null;
	var viewID = null;

	var quickinfo = null;
	if ( data.getUUID() == null )
		reader.service.HIEditor.getBaseQuickInfo(function(result) {quickinfo = result.getReturn();}, HIExceptionHandler, data.getId());
	else
		reader.service.HIEditor.getBaseQuickInfoByUUID(function(result) {quickinfo = result.getReturn();}, HIExceptionHandler, data.getUUID());

	switch (itemType) {
		case "view":
			// get object id
			id = 'O'+quickinfo.getRelatedID();
			unloaded = id; // load object
			viewID = 'V'+data.getId();
			if ( reader.project.items[viewID] == null ) {
				item = new HIView(viewID, data.getUUID());

				// parse view metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue();
								break;
							case "HIBase.source":
								item.source[lang] = content.getValue();
								break;
							case "HIBase.comment":
								item.annotation[lang] =  parseServiceMDField(content.getValue());
								break;
				}}}
				
				// FIXME: sites and refs not implemented in live view
				// parse back refs (sites)
				item.sites = new Object();
				// parse refs
				item.refs = new Object();
				// parse siblings
				// TODO: implement sibling quick view
				if ( item.parent != null )
					item.parent.siblings = new Object();
				else item.siblings = new Object();

				// parse image files
				item.files['images'] = new Array();
				// full size			
				var newFile = new Object();
				newFile.width = data.getWidth();
				newFile.height = data.getHeight();
				newFile.href = 'HI_FULL';
				item.files['images'].push(newFile);

				// scaled sizes
				var previewWidth = Math.round(data.getWidth()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				var previewHeight = Math.round(data.getHeight()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				var previewCount = 1;
				while ( data.getWidth() > (3*previewWidth) ) {
					previewHeight *= 2;
					previewWidth *= 2;
					newFile = new Object();
					newFile.width = previewWidth;
					newFile.height = previewHeight;
					newFile.href = 'HI_SCALE';
					item.files['images'].push(newFile);
					previewCount++;
				}
				// preview size			
				newFile = new Object();
				newFile.width = Math.round(data.getWidth()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				newFile.height = Math.round(data.getHeight()*Math.min(400/data.getWidth(), 400/data.getHeight()));
				newFile.href = 'HI_PREVIEW';
				item.files['images'].push(newFile);
				// thumbnail size
				newFile = new Object();
				newFile.width = Math.round(data.getWidth()*Math.min(128/data.getWidth(), 128/data.getHeight()));
				newFile.height = Math.round(data.getHeight()*Math.min(128/data.getWidth(), 128/data.getHeight()));
				newFile.href = 'HI_THUMBNAIL';
				item.files['thumb'] = newFile; 

				// sort image files
				item.files['images'].sort(function(x, y) {
					return (x.width*x.height)-(y.width*y.height);
				});
				item.files['original'] = item.files['images'][item.files['images'].length-1];
				
				// sort layers
				item.sortedLayers = [];
				item.sortOrder = data.getSortOrder().split(',');
				for (var i in item.sortOrder) {item.sortOrder[i] = parseInt(item.sortOrder[i]);}

				// parse layers
				data.getLayers().sort(function(a, b) {
					return item.sortOrder.indexOf(b.getId()) - item.sortOrder.indexOf(a.getId());
				});
				$(data.getLayers()).each(function (index, xmlLayer) {					
					var layer = new HILayer('L'+xmlLayer.getId(), xmlLayer.getUUID());
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
									layer.title[lang] = content.getValue();
									break;
								case "HIBase.comment":
									layer.annotation[lang] =  parseServiceMDField(content.getValue());
									break;
							}
						}
					}

					layer.parent = item;
					item.layers[layer.id] = layer;
					item.sortedLayers.push(layer);
					reader.project.items[layer.id] = layer;
					if ( layer.uuid != null ) reader.project.items[layer.uuid] = layer;

				});
				reader.project.items[viewID] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
				
			}
			break;
			
		case "inscription":
			// get object id
			id = 'O'+quickinfo.getRelatedID();
			unloaded = id; // load object
			viewID = 'I'+data.getId();
			if ( reader.project.items[viewID] == null ) {
				item = new HIInscription(viewID, data.getUUID());

				// parse inscription content
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						if ( content.getKey() == "HIBase.content" ) item.content[lang] =  parseServiceMDField(content.getValue());
					}
				}

				// FIXME: sites and refs not implemented in live view
				// parse back refs (sites)
				item.sites = new Object();
				// parse refs
				item.refs = new Object();
				// parse siblings
				// TODO: implement siblings quick view
				if ( item.parent != null )
					item.parent.siblings = new Object();
				else item.siblings = new Object();
				
				reader.project.items[viewID] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}		
			break;
			
		case "object":
			id = 'O'+data.getId();
			if ( reader.project.items[id] == null ) {
				item = new HIObject(id, data.getUUID());				
				
				// parse object metadata
				var metadata = data.getMetadata();
				for (var i=0; i < metadata.length; i++) {
					var md = metadata[i];
					var lang = md.getLanguage();
					item.md[lang] = new Object();
					var records = md.getContents();
					for (var recordID=0; recordID < records.length; recordID++) {
						var rec = records[recordID];
						if ( reader.project.templates[rec.getKey().replace(/\./g,'_')].richText )
							item.md[lang][rec.getKey().replace(/\./g,'_')] = parseServiceMDField(rec.getValue());
						else item.md[lang][rec.getKey().replace(/\./g,'_')] = rec.getValue();
					}
				}
				item.defaultViewID = data.getDefaultView();
				if ( item.defaultViewID != null ) item.defaultViewID = (data.getDefaultView().typeMarker == 'ws_service_hyperimage_org__hiView' ? 'V'+item.defaultViewID.getId() : 'I'+item.defaultViewID.getId());
				reader.project.items[id] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}
			// check if object has unloaded view
			var siblings  = new Object();
			item.siblings = siblings;
			for ( var i=0; i < data.getViews().length; i++ ) {
				parseItem(data.getViews()[i], 'success', false);
				var dataID = (data.getViews()[i].typeMarker == 'ws_service_hyperimage_org__hiView' ? 'V'+data.getViews()[i].getId() : 'I'+data.getViews()[i].getId());
				if ( reader.project.items[dataID].parent == null ) {
					reader.project.items[dataID].parent = reader.project.items[id];
					reader.project.items[id].views[dataID] = reader.project.items[dataID];
					var content = new HIContent(dataID, data.getViews()[i].getUUID());
					content.type = GetElementType(data.getViews()[i]);
					if ( content.type == 'view' ) content.title[reader.project.defaultLang] = reader.project.items[dataID].title[reader.project.defaultLang];
					if ( content.type == 'inscription' ) content.content[reader.project.defaultLang] = reader.project.items[dataID].content[reader.project.defaultLang];
					else content.content[reader.project.defaultLang] = '';
					if ( content.type == 'view' ) content.image=dataID;
					siblings[dataID] = content;
					reader.project.items[dataID].siblings = siblings;
				}
			}
			if ( item.defaultViewID == null ) item.defaultViewID = item.views[Object.keys(item.views)[0]].id;

			/*
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
			*/		
			break;

		case "layer":
			unloaded = 'V'+quickinfo.getRelatedID();
			break;

		case "text":
			id = 'T'+data.getId();			
			if ( reader.project.items[id] == null ) {
				item = new HIText(id, data.getUUID());
				// parse text metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue();
								break;
							case "HIBase.content":
								item.content[lang] =  parseServiceMDField(content.getValue());
								break;
						}
					}
				}

				// parse back refs (sites)
				// FIXME: sites and refs not implemented in live view
				item.sites = new Object();
				// parse refs
				item.refs = new Object();
				
				reader.project.items[id] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}
			break;
		
		case "group":
			id = 'G'+data.getId();
			if ( reader.project.items[id] == null ) {
				item = new HIGroup(id, data.getUUID());
                                item.type = "group";
                                if ( data.getType() == "HIGROUP_TAG" ) item.type = "tag";
                                item.sortOrder = data.getSortOrder().split(',');
				// parse group metadata
				for (var i=0; i < data.getMetadata().length; i++) {
					var lang = data.getMetadata()[i].getLanguage();
					for (var c=0; c < data.getMetadata()[i].getContents().length; c++) {
						var content = data.getMetadata()[i].getContents()[c];
						switch (content.getKey()) {
							case "HIBase.title":
								item.title[lang] = content.getValue();
								break;
							case "HIBase.comment":
								item.annotation[lang] =  parseServiceMDField(content.getValue());
								break;
						}
					}
				}
				
				// get group contents
				var groupContents = [];
				var serverContents = null;
				reader.service.HIEditor.getGroupContentQuickInfo(function(result) {serverContents = result.getReturn();}, HIExceptionHandler, data.getId());
				$(serverContents).each(function(index, member) {
					var content = new HIContent(GetIDModifierFromContentType(member.getContentType())+member.getBaseID(), member.getUUID());
					content.type = GetQuickInfoType(member);
					if ( content.type == "lita" ) content.type = "lightTable";
					content.title[reader.project.defaultLang] = member.getTitle();
                                        // fix for URLs, don't i18n title
                                        if ( content.type == "url" ) content.title = member.getTitle(); 
					content.size = member.getCount();
					if ( member.getPreview() != null ) content.content[reader.project.defaultLang] = parseServiceMDField(member.getPreview());
					else content.content[reader.project.defaultLang] = '';
					if ( content.type == 'object' ) content.image='V'+member.getRelatedID();
					if ( content.type == 'view' ) content.image='V'+member.getBaseID();
					if ( content.type == 'layer' ) content.image='L'+member.getBaseID();
						
					groupContents.push(content);
				});
				// sort contents
				groupContents.sort(function(a, b) {
					return item.sortOrder.indexOf(a.target.substring(1)) - item.sortOrder.indexOf(b.target.substring(1));
				});
                                
                                var sortedContents = {};
                                for (var i=0; i < groupContents.length; i++)
                                    sortedContents[groupContents[i].target] = groupContents[i];
				
				item.members = sortedContents;

				// parse back refs (sites)
				// FIXME: sites and refs not implemented in live view
				item.sites = new Object();
				// parse refs
				item.refs = new Object();
								
				reader.project.items[id] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}
			break;

		case "url":
			id = 'U'+data.getId();
			if ( reader.project.items[id] == null ) {
				item = new HIURL(id, data.getUUID());
				// parse URL metadata
				item.title = data.getTitle();
				item.annotation = ''; // TODO

				// FIXME: sites not implemented in live view
				// parse back refs (sites)
				item.sites = new Object();

				item.url = data.getUrl();
				reader.project.items[id] = item;
                                console.log(reader.project.items[id]);
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}
			break;
		
		case 'lita':
			id = 'X'+data.getId();			
			if ( reader.project.items[id] == null ) {
				item = new HILighttable(id, data.getUUID());
				data = $.parseXML(data.getXml().replace(/&nbsp;/g, ''));
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
						fa.annotation[annos[i].getAttribute("xml:lang")] = convertLegacyLineFormat(annos[i]);
					item.frameAnnotation = fa;
				}

				// FIXME: sites and refs not implemented in live view
				// parse back refs (sites)
				item.sites = new Object();
				// parse refs
				item.refs = new Object();

				reader.project.items[id] = item;
				if ( item.uuid != null ) reader.project.items[item.uuid] = item;
			}
			break;

	}
        // attach tag info
        if ( id != null && reader.project.items[id] != null && reader.project.items[id].tags == null  ) {
            reader.project.items[id].tags = [];
            reader.service.HIEditor.getTagIDsForBase(function (result) {
                reader.project.items[id].tags = result.getReturn();
                $(reader.project.items[id].tags).each(function(index, tag) { reader.project.items[id].tags[index] = reader.project.tags['G'+tag]; });
            }, HIExceptionHandler, id.substring(1));
        }
        if ( viewID != null && reader.project.items[viewID] != null && reader.project.items[viewID].tags == null ) {
            reader.project.items[viewID].tags = [];
            reader.service.HIEditor.getTagIDsForBase(function (result) {
                reader.project.items[viewID].tags = result.getReturn();
                $(reader.project.items[viewID].tags).each(function(index, tag) { reader.project.items[viewID].tags[index] = reader.project.tags['G'+tag]; });
            }, HIExceptionHandler, viewID.substring(1));
        }
        
	if ( unloaded != null && reader.project.items[unloaded] == null ) {
		console.log("unloaded item: ", unloaded);
		loadItem(unloaded, true, shouldSetGUI);
	} else if ( shouldSetGUI ) {
		setGUI();
	}
}


// @override
function loadFrame(item) {
	var quickinfo = null;
	if ( isValidUUID(item.href) )
		reader.service.HIEditor.getBaseQuickInfoByUUID(function(result) {quickinfo = result.getReturn();}, HIExceptionHandler, item.href);
	else
		reader.service.HIEditor.getBaseQuickInfo(function(result) {quickinfo = result.getReturn();}, HIExceptionHandler, item.href.substring(1));

    if ( quickinfo != null ) { // check if element exists before proceeding
        reader.service.HIEditor.getBaseElement(function(result) {
		parseItem(result.getReturn(), "success", false);
		setLoadingIndicator(false);
		window.addFrame(item); // display frame		
	}, HIExceptionHandler, quickinfo.getRelatedID());

	loadItem(item.href, true, false);
        
    }	
}


// @override
function loadFrameHiRes(id, view, frame) {
	var hiRes = findHigherRes(view, $('#'+id+"_content img"), $('#'+id+"_content img").width(), $('#'+id+"_content img").height());
	if ( hiRes == null && $('#'+id+"_content img").attr('src') == '#' ) hiRes = view.files['images'][0];

	if (hiRes != null) {
		$('#'+id+"_content img").attr('data-viewwidth', hiRes.width);
		$('#'+id+"_content img").attr('data-viewheight', hiRes.height);
		$('#' + id + '_loading').show();
		reader.service.HIEditor.synchronous = false;
		if (hiRes.href != 'HI_SCALE') {
			reader.service.HIEditor.getImage(function(cb) {
				$('#'+id+"_content img").attr('src', 'data:image/jpeg;base64,' + cb.getReturn());
				$('#'+id+"_content img").load(function() { 
					$('#'+id+"_content img").show();
					if ( frame != null ) $('#'+id+"_content").scrollLeft(Math.abs(frame.imagePos.x)).scrollTop(Math.abs(frame.imagePos.y));
				});
				

				$('#'+id+'_loading').hide();
			}, function(cb, msg) {
				console.log("getImage error --> ", cb);
				$('#'+id+"_content img").attr('src', 'reader/icons/preview-loaderror.png');
				$('#'+id+'_loading').hide();
				HIExceptionHandler(cb, msg);
			}, view.id.substring(1), hiRes.href);
		} else {
			var scale = Math.round(Math.min(view.files['original'].width / hiRes.width, view.files['original'].height / hiRes.height));
			reader.service.HIEditor.getScaledImage(function(cb) {
				$('#'+id+"_content img").load(function() { $('#'+id+"_content img").show(); });
				$('#'+id+"_content img").attr('src', 'data:image/jpeg;base64,' + cb.getReturn());
				$('#'+id+'_loading').hide();
			}, function(cb, msg) {
				console.log("getImage error --> ", cb);
				$('#'+id+"_content img").attr('src', 'reader/icons/preview-loaderror.png');
				$('#'+id+'_loading').hide();
				HIExceptionHandler(cb, msg);
			}, view.id.substring(1), scale);
		}
		reader.service.HIEditor.synchronous = true;
	}
}

// @override
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
		if ( isValidUUID(id) )
			reader.service.HIEditor.getBaseElementByUUID(function(result) {parseItem(result.getReturn(), 'success', shouldSetGUI);}, HIExceptionHandler, id);
		else
			reader.service.HIEditor.getBaseElement(function(result) {parseItem(result.getReturn(), 'success', shouldSetGUI);}, HIExceptionHandler, id.substring(1));
	} else {
		console.log(id+" - already in memory");
		if ( shouldSetGUI ) setGUI();
	}
}

// @override
function performSearch(mode) {
	var term = $('#searchInput').val();
	var advancedSearch = ($('#searchOptions:visible').length > 0 ) ? true : false;

	if (term != null && term.length > 1) {
		$('#basicSearchButton').hide();
		$('#extendedSearchButton').show();
		$('#searchResults').show();
		$('#searchOptions').hide();

		reader.search.term = term;
		reader.search.active = true;
		var searchTerm = term;
		var results = new Array();
		var resultTerms = new Array();
		resultTerms.push(term);
		
		// perform search on server (simple search, all fields)
		reader.service.HIEditor.simpleSearch(function(result) {
			results = result.getReturn();
		}, HIExceptionHandler, searchTerm, reader.lang);
		
		$('#searchResultsContent').empty();
		// fallback compatibility for missing I18N in old flash reader
		var resultText = (reader.strings[reader.lang]['SEARCH_RESULT_BASIC'] == null) ? 'Fundstellen bei der Standardsuche nach' : reader.strings[reader.lang]['SEARCH_RESULT_BASIC'];
		var advResultText = (reader.strings[reader.lang]['SEARCH_RESULT_ADVANCED'] == null) ? 'Fundstellen bei der erweiterten Suche nach' : reader.strings[reader.lang]['SEARCH_RESULT_ADVANCED'];
		var andText = (reader.strings[reader.lang]['SEARCH_AND'] == null) ? 'und' : reader.strings[reader.lang]['SEARCH_AND'];
		var noResultsText = (reader.strings[reader.lang]['SEARCH_NO_RESULTS'] == null) ? 'Kein Suchergebnis für' : reader.strings[reader.lang]['SEARCH_NO_RESULTS'];

		if (results.length > 0) {
			reader.search.resultTerms = resultTerms;
			reader.search.resultTerms = resultTerms.sort(function(a, b) {
				return b.length - a.length;
			});

			if (!advancedSearch)
				$('#searchResultsContent').append(results.length + ' ' + resultText + ' ');
			else
				$('#searchResultsContent').append(results.length + ' ' + advResultText + ' ');
			for (var i = 0; i < resultTerms.length; i++) {
				if (resultTerms.length > 1)
					if (i == (resultTerms.length - 1))
						$('#searchResultsContent').append(' ' + andText + ' ');
					else if (i > 0)
						$('#searchResultsContent').append(', ');
				$('#searchResultsContent').append('"<a href="javascript:setSearch(\'' + resultTerms[i] + '\');">' + resultTerms[i] + '</a>"');
			}
			$('#searchResultsContent').append(':<br><br>');
			$(results).each(function(index, result) {
				var title = result.getTitle();
				if ( title == null || title.length == 0 ) title = GetIDModifierFromContentType(result.getContentType())+result.getBaseID();
				if (title != null && title.length > 31)
					title = title.substring(0, 31) + "...";
				var resultID = GetIDModifierFromContentType(result.getContentType())+result.getBaseID();
				if ( result.getUUID != null ) resultID = result.getUUID();
				$('#searchResultsContent').append('<a id="' + result.getUUID() + '_result" onclick="javascript:reader.fromSearch = true;checkExternalLink(this);" class="HIExternalLink" href="#' + resultID + '/">' + title + '</a><span> ' + reader.strings[reader.lang][searchTypeKeys[GetQuickInfoType(result)]] + '</span><br>');
			});
		} else {
			$('#searchResultsContent').append(noResultsText + ' "' + term + '".');
			reader.search.resultTerms = new Array();
		}

		reader.search.active = false;
	}
}


// @override
function initContextMenus() {
	/* canvas layer context menu */
	$(reader.canvas.layerGroup).contextMenu({
		zIndex: 1000,
		selector: '[id$=_group]',
		callback: function(key, options) {
			var layerID = $(this).attr('id');
			layerID = layerID.substring(0, layerID.length-6);
			switch (key) {
				case 'addLayerToLita':
					addLayerToLightTable(layerID);
					break;
				case 'sitesOfLayer':
					location.hash=layerID+'/sites';
					break;
				case 'refsOfLayer':
					location.hash=layerID+'/refs';
					break;
			}
		},
		items: {
        	addLayerToLita: {name: "<span class='MENU_LAYER_TO_LITA'>&nbsp;</span>" },
			separator1: "-----",
			sitesOfLayer: {name: "<span class='MENU_SITES_LAYER'>&nbsp;</span>" },
			refsOfLayer: {name: "<span class='MENU_REFS_LAYER'>&nbsp;</span>" }
	} });
	
	/* canvas view context menu */
	$.contextMenu({
		zIndex: 1000,
		selector: '#canvasImage',
		callback: function(key, options) { 
			switch (key) {
				case 'addViewToLita':
					addViewToLightTable();
					break;
				case 'sitesOfView':
					location.hash=reader.viewID+'/sites';
					break;
				case 'refsOfView':
					location.hash=reader.viewID+'/refs';
					break;
				}
			},
			items: {
    			addViewToLita: {name: "<span class='MENU_VIEW_TO_LITA'>&nbsp;</span>" },
				separator1: "-----",
			    sitesOfView: {name: "<span class='MENU_SITES_VIEW'>&nbsp;</span>" },
				refsOfView: {name: "<span class='MENU_REFS_VIEW'>&nbsp;</span>" }
	} });
	
	/* contents list context menu */
	$.contextMenu({
		zIndex: 1000,
		selector: '.hasContentsContextMenu',
		callback: function(key, options) {
			var contentID = $(this).attr('id').substring(3);
			var type = $(this).data('contentType');
			switch (type) {
				case 'layer':
					var layer = reader.project.items[contentID];
					if ( layer == null ) {
                                           reader.service.HIEditor.getBaseElement(function(result) {
                                                baseElement = result.getReturn();
                                                parseItem(baseElement, 'success');
						layer = reader.project.items[contentID];
                                            }, HIExceptionHandler, contentID.substring(1));                                            
                                             
					}
					setLoadingIndicator(false);
					addLayerToLightTable(contentID);
					break;

				case 'view':
					var view = reader.project.items[contentID];
					if ( view == null ) {
                                            reader.service.HIEditor.getBaseElement(function(result) {
                                                baseElement = result.getReturn();
                                                parseItem(baseElement, 'success');
						view = reader.project.items[contentID];
                                            }, HIExceptionHandler, contentID.substring(1));                                            
 					}
					setLoadingIndicator(false);
					addViewToLightTable(view.id);
					break;

				case 'object':
					var object = reader.project.items[contentID];
					if ( object == null ) {
                                            reader.service.HIEditor.getBaseElement(function(result) {
                                                baseElement = result.getReturn();
                                                parseItem(baseElement, 'success');
                                                object = reader.project.items[contentID];
                                            }, HIExceptionHandler, contentID.substring(1));
					}
					var view = reader.project.items[object.defaultViewID];
					if ( view == null ) {
                                            reader.service.HIEditor.getBaseElement(function(result) {
                                                baseElement = result.getReturn();
                                                parseItem(baseElement, 'success');
						view = reader.project.items[object.defaultViewID];
                                            }, HIExceptionHandler, object.defaultViewID.substring(1));
                                            
					}
					setLoadingIndicator(false);
					addViewToLightTable(object.defaultViewID);
					break;
			}
			// TODO
				
		},
		items: { addToLita: {name: "<span class='MENU_SEND_TO_LITA'>&nbsp;</span>" } } 
	});
	
	/* light table context menu */
	$.contextMenu({
		zIndex: 1000,
		selector: '.hasLitaContextMenu',
		callback: function(key, options) { 
			switch (key) {
				case 'showInBrowser':
					var viewID = $(this).data('viewID');
					if ( viewID != null ) location.hash = viewID+'/';
					break;
				case 'fitFrame':
					fitToFrame();
					break;
				case 'fitToThumb':
					fitToThumb();
					break;
				case 'duplicateFrame':
					duplicateFrame();
					break;
				case 'removeFrame':
					removeFrame();
					break;
				}
			},
			items: {
    			showInBrowser: {name: "<span class='MENULITA_BROWSER'>&nbsp;</span>" },
				separator1: "-----",
			    fitFrame: {name: "<span class='MENULITA_FIT_FRAME'>&nbsp;</span>" },
				fitToThumb: {name: "<span class='MENULITA_THUMBNAIL'>&nbsp;</span>" },
				duplicateFrame: {name: "<span class='MENULITA_DUPLICATE_ELEMENT'>&nbsp;</span>" },
				separator2: "-----",
				removeFrame: {name: "<span class='MENULITA_DELETE_ELEMENT'>&nbsp;</span>" }
			},
			events: {
				show: function(opt) { $(this).trigger('click'); },
				hide: function(opt) { $(this).trigger('click'); }
			} });
}

// @override
function initReader() {	
	// set up main loading indicator
	$('#guiInitIndicator').activity({
		segments: 12,
		width: 6,
		space: 6,
		length: 14,
		speed: 1.2
	});

	// set up mouse position handler
	$(window).mousemove(function(e) {
		reader.pageX = e.pageX;
		reader.pageY = e.pageY;
	});
	
	// store web service endpoint
	reader.service = {};
	HIService.soap.endpoint = GetURLParameter('session');
	
	if ( !HIService.soap.endpoint )
		reportError('Could not initialize PreViewer. Required connection parameter "session" is missing. Please restart PreViewer from the HyperImage Editor.');

	// init web service (SOAP) API
	reader.service.HIEditor = new ws_service_hyperimage_org__HIEditor();
	reader.service.HIEditor.synchronous = true;
	reader.service.HIEditor.url = location.protocol+'//'+location.host+location.pathname+'../HIEditorService'; 
	// DEBUG remove
//	reader.service.HIEditor.url = "http://127.0.0.1:8080"+'/HI3Author/HIEditorService';

	
	/* Load User Preferences from XML */
	$.ajax({ url: 'resource/hi_prefs.xml', dataType: 'xml',
	success: function(data, success) {
		loadPrefs(data, success);

		/* Load User Strings from XML */
		$.ajax({ url: 'resource/hi_strings.xml', dataType: 'xml',
		success: function(data, success) {
			loadStrings(data, success);
				
			/* load initial project data from web service */		
			var project = null;
			reader.service.HIEditor.getProject(function(result) {project = result.getReturn();},
			function(cb, error) {
				HIExceptionHandler(cb, error);
			});
			reader.project.id = project.getId();

			/* extract project languages */	
			reader.project.defaultLang = project.getDefaultLanguage().getLanguageId();
			var langs = project.getLanguages();
			for (i=0; i < langs.length; i++) {
				reader.project.langs[i] = langs[i].getLanguageId();
				if ( !reader.strings[reader.project.langs[i]] ) 
					reader.strings[reader.project.langs[i]] = reader.strings['en']; // use english as default on-screen language for unknown languages
			}

			if ( reader.project.defaultLang == null || reader.project.defaultLang.length == 0 )
				reader.project.defaultLang = reader.project.langs[0];

			/* extract project title */
			for (i = 0; i < project.getMetadata().length; i++)
				if ( project.getMetadata()[i].getTitle() != null ) 
					reader.project.title[project.getMetadata()[i].getLanguageID()] = project.getMetadata()[i].getTitle();
				else reader.project.title[project.getMetadata()[i].getLanguageID()] = '';

			/* extract start element */
			var importGroup = null;
			reader.service.HIEditor.getImportGroup(function(result) {importGroup = result.getReturn()}, HIExceptionHandler);
			reader.project.groups['G'+importGroup.getId()] = new Object();
			for (lang in reader.project.langs)
				reader.project.groups['G'+importGroup.getId()][reader.project.langs[lang]] = '(Import)';
			if ( project.getStartObjectInfo() == null ) reader.start = 'G'+importGroup.getId();
			else reader.start = GetIDModifierFromContentType(project.getStartObjectInfo().getContentType())+project.getStartObjectInfo().getBaseID();

			/* extract and sort template fields */
			templates = project.getTemplates();
			sortedFields = new Array();
			var htmlFields = "";
			for (i = 0; i < templates.length; i++) {
				var templateID = templates[i].getNamespacePrefix();
		
				keys = templates[i].getEntries();
				for (keyID = 0; keyID < keys.length; keyID++) {
					// todo sorting
					sortedFields[keyID] = templateID+"_"+keys[keyID].getTagname();
					var tempKey = reader.project.templates[templateID+"_"+keys[keyID].getTagname()] = new Object();
					tempKey.key = keys[keyID].getTagname();
					tempKey.template = templateID;
					tempKey.richText = keys[keyID].getRichText();
					tempLangs = keys[keyID].getDisplayNames();
					for (langID = 0; langID<tempLangs.length; langID++) {
						if ( tempLangs[langID].getDisplayName() != null && tempLangs[langID].getDisplayName().length > 0 )
							tempKey[tempLangs[langID].getLanguage()] = tempLangs[langID].getDisplayName();
						else tempKey[tempLangs[langID].getLanguage()] = templateID+'.'+tempKey.key;
					}
				}
			}
			reader.project.sortedFields = sortedFields;
		
			/*
			 * extract visible groups, tags, project texts and light tables
			 */
			// groups
			var serverGroups = null;
			reader.service.HIEditor.getGroups(function(result) {serverGroups = result.getReturn();}, HIExceptionHandler);
			for (groupID in serverGroups) {
				var group = serverGroups[groupID];
				reader.project.groups['G'+group.getId()] = new Object();

				for ( mdID in group.getMetadata() ) {
					var record = group.getMetadata()[mdID];
					for ( cID in record.getContents() ) {
						var entry = record.getContents()[cID];
						if ( entry.getKey() == 'HIBase.title' )
							if ( entry.getValue().length > 0 ) reader.project.groups['G'+group.getId()][record.getLanguage()] = entry.getValue();
							else reader.project.groups['G'+group.getId()][record.getLanguage()] = '(G'+group.getId()+')';
					}
				}			
			}
			// sort group entries
			var sortedGroups = {};
			var groupSortOrder = "";
			sortedGroups['G'+importGroup.getId()] = reader.project.groups['G'+importGroup.getId()];
			for (var prefID in project.getPreferences()) {
				var pref = project.getPreferences()[prefID];
				if ( pref.getKey() == 'groupSortOrder' ) groupSortOrder = pref.getValue();
			}
			$(groupSortOrder.split(',')).each(function(index, groupID){
				if ( reader.project.groups['G'+groupID] != null ) sortedGroups['G'+groupID] = reader.project.groups['G'+groupID];
			});
			for (var i=0; i < Object.keys(reader.project.groups).length; i++) {
				var groupID = Object.keys(reader.project.groups)[i];
				if ( sortedGroups[groupID] == null ) sortedGroups[groupID] = reader.project.groups[groupID];
			}
			reader.project.groups = sortedGroups;

			// tags
			var serverTags = null;
			reader.service.HIEditor.getTagGroups(function(result) {serverTags = result.getReturn();}, HIExceptionHandler);
			for (tagID in serverTags) {
				var tag = serverTags[tagID];
				reader.project.tags['G'+tag.getId()] = new Object();
                                reader.project.tags['G'+tag.getId()].id = 'G'+tag.getId();
                                reader.project.tags['G'+tag.getId()].uuid = tag.getUUID();
                                reader.project.tags['G'+tag.getId()].type = "tag";
                                

				for ( mdID in tag.getMetadata() ) {
					var record = tag.getMetadata()[mdID];
					for ( cID in record.getContents() ) {
						var entry = record.getContents()[cID];
						if ( entry.getKey() == 'HIBase.title' )
							if ( entry.getValue().length > 0 ) reader.project.tags['G'+tag.getId()][record.getLanguage()] = entry.getValue();
							else reader.project.tags['G'+tag.getId()][record.getLanguage()] = '(G'+tag.getId()+')';
					}
				}			
			}
                    
			// texts
			var serverTexts = null;
			reader.service.HIEditor.getProjectTextElements(function(result) {serverTexts = result.getReturn();}, HIExceptionHandler);
			for (textID in serverTexts) {
				var text = serverTexts[textID];
				reader.project.texts['T'+text.getId()] = new Object();
				for ( mdID in text.getMetadata() ) {
					var record = text.getMetadata()[mdID];
					for ( cID in record.getContents() ) {
						var entry = record.getContents()[cID];
						if ( entry.getKey() == 'HIBase.title' )
							if ( entry.getValue().length > 0 ) reader.project.texts['T'+text.getId()][record.getLanguage()] = entry.getValue();
							else reader.project.texts['T'+text.getId()][record.getLanguage()] = '(T'+text.getId()+')';
					}
				}			
			}

			// light tables
			var serverLitas = null;
			reader.service.HIEditor.getProjectLightTableElements(function(result) {serverLitas = result.getReturn();}, HIExceptionHandler);
			for (litaID in serverLitas) {
				var lita = serverLitas[litaID];
				reader.project.litas['X'+lita.getId()] = new Object();
				for (langID in reader.project.langs) {
					if ( lita.getTitle().length > 0 ) reader.project.litas['X'+lita.getId()][reader.project.langs[langID]] = lita.getTitle();
					else reader.project.litas['X'+lita.getId()][reader.project.langs[langID]] = '(X'+lita.getId()+')';
				}
			}
			console.log(project);
			/*
	 var menus = data.getElementsByTagName("menu");
	 for (i=0; i < menus.length; i++) {
	 	// switch between groups and texts
	 	if ( menus[i].getAttribute("key") == "text" ) {entries = reader.project.texts;items = serverTexts;}
	 	else if ( menus[i].getAttribute("key") == "group" ) entries = reader.project.groups;
	 	else entries = reader.project.litas;
	 	var lang = menus[i].getAttribute("xml:lang");
	 	// get menu items for lang
	 	items = menus[i].getElementsByTagName("item");
		for (itemID=0; itemID < items.length; itemID++) {
			if ( entries[items[itemID].getAttribute("ref")] == null ) entries[items[itemID].getAttribute("ref")] = new Object();
			entries[items[itemID].getAttribute("ref")][lang] = items[itemID].firstChild.nodeValue;
		}
	 } 
	 */

		

			// restore session if possible
			restoreSession();
			newLocalTable(false);
			// init GUI
			initGUI();

			// init context menus
			initContextMenus();

			// update on screen language
			setLanguage(reader.project.defaultLang);
				
			// load start item or user requested item
			var mode = 'regular';
			if ( location.hash.substring(1).split('/').length > 1 && location.hash.substring(1).split('/')[1].length > 0 )
				mode = location.hash.substring(1).split('/')[1];
			if ( mode != 'regular' && mode != 'all' && mode != 'refs' && mode != 'sites' ) mode = 'regular';
			reader.mode = mode;
			if ( location.hash.length > 0 ) loadItem(location.hash.substring(1).split('/')[0], false, true);
			else location.hash = reader.start+"/";

			// show alpha version info dialog
    		$( "#alphaDialog" ).dialog({
		    	draggable: false,
				resizable: false,
				closeOnEscape: true,
				title: "HyperImage PreViewer - Beta Version ("+reader.version+")",
				width: 580,
				height: 360,
				modal: true,
				buttons: {
        			"OK": function() {
						$(this).dialog( "close" );
					}
				}
		  	});

			
			// init plugins
			initPlugins();
			$(reader.plugins.init).each(function(index, plugin) { plugin(); });
					
		}, error: function(error) {reportError('Required file "resource/hi_strings.xml" missing or load error.')} });
	}, error: function(error) {reportError('Required file "resource/hi_prefs.xml" missing or load error.<br><br><strong>Did you start the Reader from your harddrive?</strong><br>For this online publication to work you need to upload your project to a web server.<br>An offline version is available from our website.')} });

	

	
}


					



// Place any jQuery/helper plugins in here.
