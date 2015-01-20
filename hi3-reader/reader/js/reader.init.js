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

function loadPrefs(data, success) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);

	/* extract pref data from XML elements */
	prefs = data.getElementsByTagName("pref");
	for (prefID=0; prefID < prefs.length; prefID++ ) {
		reader.prefs[prefs[prefID].getAttribute("key")]=prefs[prefID].getAttribute("val");
	}	
	
	// Set Preferences
	
	/* General Preferences */
	$('#info').css("background-color", reader.prefs['INFOLINE_COLOR']); // Farbe der Informationszeile
	$('body').css("font-family", reader.prefs['MAINTEXT_FONT']); // Schriftart
	$('body').css("font-size", reader.prefs['MAINTEXT_SIZE']+'px'); // Größe

	if ( reader.prefs['MAINTEXT_BOLD'] == 'true' ) $.stylesheet('body').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('body').css('font-weight', 'normal');
	if ( reader.prefs['MAINTEXT_ITALIC'] == 'true' ) $.stylesheet('body').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('body').css('font-style', 'normal');
	if ( reader.prefs['MAINTEXT_UNDERLINE'] == 'true' ) $.stylesheet('body').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('body').css('text-decoration', 'none');

	$.stylesheet('a').css("color", reader.prefs['MAINTEXT_LINK_COLOR']); // Link: Farbe
	$.stylesheet('a:visited').css("color", reader.prefs['MAINTEXT_LINK_COLOR']); // Link: Farbe
	if ( reader.prefs['MAINTEXT_LINK_BOLD'] == 'true' ) $.stylesheet('a').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('a').css('font-weight', 'normal');
	if ( reader.prefs['MAINTEXT_LINK_ITALIC'] == 'true' ) $.stylesheet('a').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('a').css('font-style', 'normal');
	if ( reader.prefs['MAINTEXT_LINK_UNDERLINE'] == 'true' ) $.stylesheet('a').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('a').css('text-decoration', 'none');

	$('body').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#canvas').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#groupView').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#textView').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#searchContent').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#searchOptionsContent').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#searchResultsContent').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#metadataBar').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#annotationBar').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#inscriptionBar').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#searchBar').css("background-color", reader.prefs['BG_COLOR']); // allgemeine Hintergrundfarbe
	$('#info').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('body').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe	
	$.stylesheet('body, #info, #canvas, #groupView, #textView, #lighttableView, #sidebar, #metadataContent, #annotationContent, #inscriptionBar, #searchBar, .mdvalue, a, a:visited, ul.tabmenu li a').css('color', reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#canvas').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#groupView').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#textView').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#lighttableView').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#sidebar').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#metadataContent').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#annotationContent').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#inscriptionBar').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$('#searchBar').css("color", reader.prefs['MAINTEXT_COLOR']); // Farbe
	$.stylesheet('.mdvalue').css("color", reader.prefs['MENUTEXT_COLOR']); // Farbe
	$('#textContent').css("max-width", reader.prefs['TEXT_WIDTH']+"px"); // maximale Breite der Projekttexte in Pixel	
	if ( reader.prefs['VIEW_SHOW_LAYER'] == null ) reader.prefs['VIEW_SHOW_LAYER'] = false;
	if ( reader.prefs['INFOLINE_METADATA_NUM'] == null ) reader.prefs['INFOLINE_METADATA_NUM'] = 1; else reader.prefs['INFOLINE_METADATA_NUM'] = parseInt(reader.prefs['INFOLINE_METADATA_NUM']);
	if ( reader.prefs['INFOLINE_METADATA_VIEW'] == null ) reader.prefs['INFOLINE_METADATA_VIEW'] = false; 
	if ( reader.prefs['INFOLINE_METADATA_VIEW'] == 'true' ) reader.prefs['INFOLINE_METADATA_VIEW'] = true; else reader.prefs['INFOLINE_METADATA_VIEW'] = false;


	/* Menu Preferences */	

	$('#mainmenu').css("color", reader.prefs['MENUTEXT_COLOR']); // Farbe
	$.stylesheet('ul.mainmenu a').css("color", reader.prefs['MENUTEXT_COLOR']); // Farbe
	$.stylesheet('#mainmenu, ul.mainmenu a').css("color", reader.prefs['MENUTEXT_COLOR']); // Farbe
	if ( reader.prefs['MENUTEXT_BOLD'] == 'true' ) $.stylesheet('#mainmenu, ul.mainmenu a').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('#mainmenu, ul.mainmenu a').css('font-weight', 'normal');
	if ( reader.prefs['MENUTEXT_ITALIC'] == 'true' ) $.stylesheet('#mainmenu, ul.mainmenu a').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('#mainmenu, ul.mainmenu a').css('font-style', 'normal');
	if ( reader.prefs['MENUTEXT_UNDERLINE'] == 'true' ) $.stylesheet('#mainmenu, ul.mainmenu a').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('#mainmenu, ul.mainmenu a').css('text-decoration', 'none');
	$.stylesheet('#mainmenu, ul.mainmenu a').css("letter-spacing", reader.prefs['MENUTEXT_LETTERSPACING']+'px'); // Spationierung

	$('#mainmenu').css("background-color", reader.prefs['MENU_COLOR']); // Farbe des Menüs
	$.stylesheet('.disabled').css('background-color', reader.prefs['MENU_COLOR']+' !important'); // Farbe des Menüs
	$.stylesheet('.disabled > a').css('background-color', reader.prefs['MENU_COLOR']+' !important'); // Farbe des Menüs
	$.stylesheet('ul.mainmenu li.separator:hover').css("background-color", reader.prefs['MENU_COLOR']+' !important'); // Farbe des Menüs	
	$.stylesheet('ul.mainmenu ul').css("background-color", reader.prefs['MENU_COLOR']); // Farbe des Menüs
	$('#mainmenu').css("font-family", reader.prefs['MENUTEXT_FONT']); // Schriftart
	$('#mainmenu').css("font-size", reader.prefs['MENUTEXT_SIZE']+'px'); // Größe
	$.stylesheet('ul.mainmenu li:hover').css('background-color', reader.prefs['MENU_COLOR_HI']); // Farbe der fokussierten Zeile
	$.stylesheet('.disabled').css('color', reader.prefs['MENUTEXT_COLOR_DEACT']+' !important'); // Farbe deaktivierter Menüzeilen
	$.stylesheet('.disabled > a').css('color', reader.prefs['MENUTEXT_COLOR_DEACT']+' !important'); // Farbe deaktivierter Menüzeilen
	
	
	/* Tab Preferences */

	$.stylesheet('ul.tabmenu li.selected').css('background-color', reader.prefs['TABS_COLOR']); // Farbe der Registerkarte
	$('#tabs').css('background-color', reader.prefs['TABS_COLOR_DESEL']); // Farbe der deselektierten Registerkarten
	$('ul.tabmenu li').css('border-right-color', reader.prefs['TABS_COLOR_BORDER']); // Umrissfarbe der Reiter der Registerkarten
	
	$.stylesheet('ul.tabmenu li a').css('color', reader.prefs['MAINTEXT_COLOR']); // Farbe
	if ( reader.prefs['TABTITLE_BOLD'] == 'true' ) $.stylesheet('.mdkey').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('.mdkey').css('font-weight', 'normal');
	if ( reader.prefs['TABTITLE_ITALIC'] == 'true' ) $.stylesheet('.mdkey').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('.mdkey').css('font-style', 'normal');
	if ( reader.prefs['TABTITLE_UNDERLINE'] == 'true' ) $.stylesheet('.mdkey').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('.mdkey').css('text-decoration', 'none');
	$.stylesheet('.mdkey').css('font-family', reader.prefs['TABTITLE_FONT']); // Schriftart
	$.stylesheet('.mdkey').css('font-size', reader.prefs['TABTITLE_SIZE']+'px'); // Größe
	$.stylesheet('.mdkey').css('color', reader.prefs['TABTITLE_COLOR']); // Farbe
	showTab(reader.prefs['TABS_STANDARD']); // standardmäßig selektierte Registerkarte
	
	$.stylesheet('#searchOptionsList, #searchOptionsList ul').css("font-family", reader.prefs['SEARCH_ITEM_FONT']); // Schriftart
	$.stylesheet('#searchOptionsList, #searchOptionsList ul').css("font-size", reader.prefs['SEARCH_ITEM_SIZE']+'px'); // Größe
	$.stylesheet('#searchOptionsList, #searchOptionsList ul').css("color", reader.prefs['SEARCH_ITEM_COLOR']); // Farbe
	if ( reader.prefs['SEARCH_ITEM_BOLD'] == 'true' ) $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('font-weight', 'normal');
	if ( reader.prefs['SEARCH_ITEM_ITALIC'] == 'true' ) $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('font-style', 'normal');
	if ( reader.prefs['SEARCH_ITEM_UNDERLINE'] == 'true' ) $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('#searchOptionsList, #searchOptionsList ul').css('text-decoration', 'none');
	$.stylesheet('#searchOptionsList, #searchOptionsList ul').css("letter-spacing", reader.prefs['SEARCH_ITEM_LETTERSPACING']+'px'); // Spationierung



	/* Tooltip Preferences */

	var tooltipStyle = $.stylesheet('.tooltip');
	var tooltipBGStyle = $.stylesheet('.tooltipBackground');
	var tooltipFGStyle = $.stylesheet('.tooltipContent');
	tooltipBGStyle.css('background-color', reader.prefs['TOOLTIP_COLOR']); // Farbe des Tooltipfensters
	tooltipBGStyle.css('opacity', parseFloat(reader.prefs['OVERLAYS_OPACITY'])); // Farbe des Tooltipfensters + Deckkraft der überlagernden Elemente
	tooltipStyle.css('width', reader.prefs['TOOLTIP_WIDTH']+"px"); // Breite des Tooltipfensters in Pixel
	tooltipStyle.css('max-width', reader.prefs['TOOLTIP_WIDTH']+"px");
	tooltipFGStyle.css('font-family', reader.prefs['TOOLTIPTEXT_FONT']); // Schriftart
	tooltipFGStyle.css('font-size', reader.prefs['TOOLTIPTEXT_SIZE']+"px"); // Größe
	tooltipFGStyle.css('color', reader.prefs['TOOLTIPTEXT_COLOR']); // Farbe
	if ( reader.prefs['TOOLTIPTEXT_BOLD'] == 'true' ) tooltipFGStyle.css('font-weight', 'bold'); // fett [true | false]
	else tooltipFGStyle.css('font-weight', 'normal');
	if ( reader.prefs['TOOLTIPTEXT_ITALIC'] == 'true' ) tooltipFGStyle.css('font-style', 'italic'); // kursiv [true | false]
	else tooltipFGStyle.css('font-style', 'normal');
	if ( reader.prefs['TOOLTIPTEXT_UNDERLINE'] == 'true' ) tooltipFGStyle.css('text-decoration', 'underline'); // unterstrichen [true | false]
	else tooltipFGStyle.css('text-decoration', 'none');
	tooltipFGStyle.css("letter-spacing", reader.prefs['TOOLTIPTEXT_LETTERSPACING']+'px'); // Spationierung


	/* Group / List Preferences */

	$.stylesheet('#groupList li.text').css("background-color", reader.prefs['GROUPMEMBER_COLOR']); // Hintergrundfarbe der Gruppenelemente mit Text	
	$.stylesheet('#groupList li.text').css("border-color", reader.prefs['GROUPMEMBER_COLOR_BORDER']); // Randfarbe der Gruppenelemente mit Text
	$.stylesheet('#groupList li.text a').css("color", reader.prefs['GROUP_THUMB_COLOR']); // Farbe
	$.stylesheet('#groupList li.text a').css("font-family", reader.prefs['GROUP_THUMB_FONT']); // Schriftart
	$.stylesheet('#groupList li.text a').css("font-size", reader.prefs['GROUP_THUMB_SIZE']+'px'); // Größe
	if ( reader.prefs['GROUP_THUMB_BOLD'] == 'true' ) $.stylesheet('#groupList li.text a').css('font-weight', 'bold'); // fett [true | false]
	else $.stylesheet('#groupList li.text a').css('font-weight', 'normal');
	if ( reader.prefs['GROUP_THUMB_ITALIC'] == 'true' ) $.stylesheet('#groupList li.text a').css('font-style', 'italic'); // kursiv [true | false]
	else $.stylesheet('#groupList li.text a').css('font-style', 'normal');
	if ( reader.prefs['GROUP_THUMB_UNDERLINE'] == 'true' ) $.stylesheet('#groupList li.text a').css('text-decoration', 'underline'); // unterstrichen [true | false]
	else $.stylesheet('#groupList li.text a').css('text-decoration', 'none');
	$.stylesheet('#groupList li.text a').css("letter-spacing", reader.prefs['GROUP_THUMB_LETTERSPACING']+'px'); // Spationierung

	
	/* Light Table Preferences */
	
	$('#lighttableView').css("background-color", reader.prefs['LITA_COLOR_BG']); // Hintergrundfarbe des Lichttisches
	$.stylesheet('.ltFrameTitle').css("background-color", reader.prefs['LITA_COLOR_HEAD']); // Hintergrundfarbe der Kopfzeilen der Lichttisch-Elemente
	$.stylesheet('.ltSelected .ltFrameTitle').css("background-color", reader.prefs['LITA_COLOR_HEADSEL']); // Hintergrundfarbe der Kopfzeile des selektierten Lichttisch-Elementes
	$.stylesheet('.ltFrameTitle').css("font-family", reader.prefs['LITA_HEAD_FONT']); // Schriftart
	$.stylesheet('.ltFrameTitle').css("font-size", reader.prefs['LITA_HEAD_SIZE']+'px'); // Größe
	$.stylesheet('.ltFrameTitle').css("color", reader.prefs['LITA_HEAD_COLOR']); // Farbe
	$.stylesheet('.ltAnnotation .ltText').css("font-family", reader.prefs['LITA_ANN_FONT']); // Schriftart
	$.stylesheet('.ltAnnotation .ltText').css("font-size", reader.prefs['LITA_ANN_SIZE']+'px'); // Größe
	$.stylesheet('.ltAnnotation .ltText').css("color", reader.prefs['LITA_ANN_COLOR']); // Farbe	
	
	
	/* Dialog / Input / Button Preferences */

	$.stylesheet('.hiButton').css("font-family", reader.prefs['DIALOGTEXT_FONT']); // Schriftart	
	$('#searchInput').css("font-family", reader.prefs['DIALOGTEXT_FONT']); // Schriftart	
	$.stylesheet('.hiButton').css("font-size", reader.prefs['DIALOGTEXT_SIZE']+'px'); // Größe	
	$('#searchInput').css("font-size", reader.prefs['DIALOGTEXT_SIZE']+'px'); // Größe	
	$.stylesheet('.hiButton').css("color", reader.prefs['DIALOGTEXT_COLOR']); // Farbe	
	$('#searchInput').css("color", reader.prefs['DIALOGTEXT_COLOR']); // Farbe	
	$.stylesheet('.hiButton').css("background-color", reader.prefs['DIALOG_BUTTON_COLOR']); // Farbe der Schaltflächen der Eingabefelder	
	$('#searchInput').css("background-color", reader.prefs['DIALOG_INPUT_COLOR']); // Hintergrundfarbe der Eingabefelder	
	
}

function loadStrings(data, success) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);
	/* extract string data from XML elements */
	langs = data.getElementsByTagName("table");

	for (var langID=0; langID<langs.length; langID++) {
		var lang = langs[langID].getAttribute("xml:lang");
		reader.strings[lang] = new Object();
		reader.strings[lang]['VERSION'] = reader.version; // add version
		reader.strings.length++;
    	var strings = langs[langID].getElementsByTagName("str");
    	for (var stringID=0; stringID<strings.length; stringID++)
    		reader.strings[lang][strings[stringID].getAttribute("key")] = strings[stringID].firstChild.nodeValue;
	}
}

function loadStartFile(data, success) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);
	/* extract string data from XML elements */
	reader.path = data.getElementsByTagName("project")[0].getAttribute("path");
	reader.project.id = data.getElementsByTagName("link")[0].getAttribute("ref");	
}

function loadProjectFile(data, success) {
	if ( typeof(data) == 'string' ) data = $.parseXML(data);
	langs = data.getElementsByTagName("language");
	if ( langs.length < 1 ) reportError("No project languages found or load error ("+reader.project.id+".xml)");
	
	/*
	 * extract project languages 
	 */	
	for (i=0; i < langs.length; i++) {
		reader.project.langs[i] = langs[i].firstChild.nodeValue;
		if ( langs[i].getAttribute("standard") == 'true' ) reader.project.defaultLang = reader.project.langs[i];
	}
	if ( reader.project.defaultLang == null || reader.project.defaultLang.length == 0 )
		reader.project.defaultLang = reader.project.langs[0];

	/* 
	 * extract project title 
	 */
	titles = data.getElementsByTagName("title");
	for (i = 0; i < titles.length; i++)
		if ( titles[i].firstChild != null ) 
			reader.project.title[titles[i].getAttribute("xml:lang")] = titles[i].firstChild.nodeValue;
		else reader.project.title[titles[i].getAttribute("xml:lang")] = '';

	/*
	 * extract start element
	 */
	reader.start = data.getElementsByTagName("link")[0].getAttribute("ref");

	/*
	 * extract and sort template fields
	 */
	templates = data.getElementsByTagName("template");
	sortedFields = new Array();
	var htmlFields = "";
	for (i = 0; i < templates.length; i++) {
		var templateID = templates[i].getAttribute("id");
		
		keys = templates[i].getElementsByTagName("key");
		for (keyID = 0; keyID < keys.length; keyID++) {
			sortedFields[keys[keyID].getAttribute("rank")-1] = templateID+"_"+keys[keyID].getAttribute("tagName");
			var tempKey = reader.project.templates[templateID+"_"+keys[keyID].getAttribute("tagName")] = new Object();
			tempKey.key = keys[keyID].getAttribute("tagName");
			tempKey.template = templateID;
			if ( keys[keyID].getAttribute("richText") == "true" ) tempKey.richText = true; else tempKey.richText = false;
			tempLangs = keys[keyID].getElementsByTagName("displayName");
			for (langID = 0; langID<tempLangs.length; langID++) {
				if ( tempLangs[langID].firstChild != null )
					tempKey[tempLangs[langID].getAttribute("xml:lang")] = tempLangs[langID].firstChild.nodeValue;
				else tempKey[tempLangs[langID].getAttribute("xml:lang")] = 'x';
			}
		}
	}
	reader.project.sortedFields = sortedFields;
	
	/*
	 * extract search index file names
	 */
	reader.project.search.files = new Object();
	var files = data.getElementsByTagName("index");
	for (i=0; i < files.length; i++)
		reader.project.search.files[files[i].getAttribute("xml:lang")] = files[i].getElementsByTagName("file")[0].firstChild.nodeValue;
		
	/*
	 * extract visible groups, project texts and light tables
	 */
	 var menus = data.getElementsByTagName("menu");
	 for (i=0; i < menus.length; i++) {
	 	// switch between groups and texts
	 	if ( menus[i].getAttribute("key") == "text" ) entries = reader.project.texts;
	 	else if ( menus[i].getAttribute("key") == "group" ) entries = reader.project.groups;
	 	else entries = reader.project.litas;
	 	var lang = menus[i].getAttribute("xml:lang");
	 	// get menu items for lang
	 	items = menus[i].getElementsByTagName("item");
		for (itemID=0; itemID < items.length; itemID++) {
			if ( entries[items[itemID].getAttribute("ref")] == null ) entries[items[itemID].getAttribute("ref")] = new Object();
			if ( items[itemID].firstChild ) entries[items[itemID].getAttribute("ref")][lang] = items[itemID].firstChild.nodeValue;
			else entries[items[itemID].getAttribute("ref")][lang] = '';
		}
	 }
	 
}

function addSearchSection(lang, head) {
	var section = new Object();
	
	if ( reader.search.titles[head.getAttribute("key")] != null && reader.strings[lang] != null && reader.strings[lang][reader.search.titles[head.getAttribute("key")]] != null )
	section.title = reader.strings[lang][reader.search.titles[head.getAttribute("key")]];
	else section.title = head.getAttribute("key");
	if ( head.getAttribute('caption') != null ) section.title = head.getAttribute('caption');
	section.key = head.getAttribute("key");
	section.items = new Array();
	section.substitute = head.getAttribute('substitute') != null ? parseInt(head.getAttribute('substitute')) : null;
	$(head).find('> item').each(function(index, item) {
		section.items.push(addSearchSection(lang, item));
	});	
	
	return section;
}

function displaySection(section) {
	var list = '';
	list += "<li id=\""+section.key+"\"";
	if ( section.items.length != 0 ) list += ' class="hiBold"';
	list += ">\n";
	list += "<input id=\""+section.key+"_check\" type=\"checkbox\" checked=\"checke\" class=\"searchSection\" />\n";
	list += "<label for=\""+section.key+"_check\" ";
	list += ">"+section.title+"</label>\n";
	list += "</li>\n"; 
	if ( section.items.length > 0 ) {
		list += "<ul id=\""+section.key+"_section\" >\n";
		$(section.items).each(function(index, item) {
			list += displaySection(item);
		});
		list += "<ul>\n";
	}
	return list;
}

function searchAllHandler(ev) {
	var isChecked = $(this).is(':checked');	
	$('#searchOptionsList').find('input').prop('checked', isChecked);	
}

function searchSectionHandler(ev) {
	var list = $(this).parent().parent();
	var isChecked = $(this).is(':checked');
	
	if ( $(this).parent().hasClass('hiBold') )
		$(this).parent().next().find('input').prop('checked', isChecked);
	
	do {
		var box = $(list.prev()).find('> input:first');
		if ( !isChecked ) box.prop('checked', false);
		else {
			var allSelected = true;
			list.find('input').each(function(index, section) {
				if ( !$(section).is(':checked') ) allSelected = false;
			});
			if ( allSelected ) box.prop('checked', "checked");
		}
		list = list.parent();
	} while ( list.attr('id') != 'searchOptionsList' );
}

function fillSearchSections(lang) {
	$('#searchSectionsList').empty();
	$(reader.project.search[lang].sections).each(function(index, section) {
		$('#searchSectionsList').append(displaySection(section));
	});
	$('#searchSectionsList input').change(searchSectionHandler);
}

function loadSearchFile(fileLang, success) {
	var data = reader.search.data[fileLang];
	if ( typeof(data) == 'string' ) data = $.parseXML(data);
	
	var lang = data.getElementsByTagName('index')[0].getAttribute('xml:lang');
	if ( lang == null || lang.length == 0 ) reportError('no language index found in XML file "index_'+fileLang+'"');
	
	reader.project.search[lang] = new Object();
	reader.project.search[lang].entries = new Object();
	var subsByNumber = new Array();
	var subsByKey = new Object();
	var subItems = data.getElementsByTagName('item');
	$(subItems).each(function(index, item) {
		if ( item.getAttribute('substitute') != null ) {
			subsByNumber[parseInt(item.getAttribute('substitute'))] = item.getAttribute('key');
			subsByKey[item.getAttribute('key')] = parseInt(item.getAttribute('substitute'));
		}
	});
	
	$(data).find('entry').each(function(index, entry) {
		var id = entry.getAttribute('str');
		reader.project.search[lang].entries[id] = new Object();
		$(entry).find('rec').each(function(index, rec) {
			var ref = rec.getAttribute('ref');
			reader.project.search[lang].entries[id][ref] = new Array();
			$(rec.getAttribute('key').split(',')).each(function(index, key) {
				reader.project.search[lang].entries[id][ref].push(parseInt(key));
			});
		});
	});
		
	reader.project.search[lang].subByNumber = subsByNumber;
	reader.project.search[lang].subByKey = subsByKey;
	reader.project.search[lang].sections = new Array();
	$(data).find('index > item').each(function (index, item) { reader.project.search[lang].sections.push(addSearchSection(lang, item)); });
	if ( fileLang == reader.lang ) fillSearchSections(reader.lang);
	
	reader.search.data[fileLang] = null;
	if ( reader.lang == fileLang && reader.search.active == true ) performSearch();
	console.log("search: "+fileLang);
}

function setLanguage(lang) {
	if ( reader.strings.length < 1 ) reportError("no on-screen languages found or load error for \""+lang+"\"");
	if ( reader.strings[lang] == null ) {
		// find fallback language
		var fallbacklang = reader.strings[reader.project.defaultLang];
		if ( fallbacklang == null ) fallbacklang = reader.strings['en'];
		reader.strings[lang] = fallbacklang;
	}

	// set language menu state
	for (i=0; i < reader.project.langs.length; i++)
		setMenuItem('setLang_'+reader.project.langs[i],"javascript:setLanguage(\'"+reader.project.langs[i]+"\');" , (reader.project.langs[i] != lang));

	for (key in reader.strings[lang]) {
		var elements = $("."+key);
		$.each(elements, function (index, element) {
			$(element).contents().each(function() {
				if (this.nodeType === 3) { // 3 = text node
			      	this.nodeValue = reader.strings[lang][key];
	      			return false;
				}
			});
		});
	}
	
	// metadata display names
	for (i=0; i < reader.project.sortedFields.length; i++)
		$('#'+reader.project.sortedFields[i]+"_key").text(reader.project.templates[reader.project.sortedFields[i]][lang]);
	
	// group menu display names
	for (group in reader.project.groups)
		$('.'+group+"_menuitem").text(reader.project.groups[group][lang]);
	// text menu display names
	for (text in reader.project.texts)
		$('.'+text+"_menuitem").text(reader.project.texts[text][lang]);
	// light table menu display names
	for (lita in reader.project.litas)
		$('.'+lita+"_menuitem").text(reader.project.litas[lita][lang]);
	
	// search UI
	$('#searchButton').attr('value', reader.strings[lang]['SEARCH_SEARCH']);
	$('#basicSearchButton').attr('value', reader.strings[lang]['SEARCH_BASIC']);
	$('#extendedSearchButton').attr('value', reader.strings[lang]['SEARCH_ADVANCED']);
	
	if ( reader.project.search[lang] != null ) fillSearchSections(lang);
	
	// window title
	document.title = (reader.project.title[reader.project.defaultLang] + 
		" - HyperImage 3 ("+reader.productID+" "+reader.version+")");
	
	reader.lang = lang;
	if ( reader.load != null ) setGUI(true);
}

/*
 * Handle URL change requests
 */
function loadHandler(e) {
		var newhash = location.hash.substring(1);
		e.preventDefault();
		
		// parse special modes
		var mode = 'regular';
		if ( newhash.split('/').length > 1 && newhash.split('/')[1].length > 0 )
			mode = newhash.split('/')[1];
		newhash = newhash.split('/')[0];
		
		var newmode = false;
		if ( mode != 'regular' && mode != 'all' && mode != 'refs' && mode != 'sites' ) mode = 'regular';
		if ( mode != reader.mode )  {
			reader.mode = mode;
			newmode = true;
		}
			
		if ( newhash.length == 0 ) location.hash = reader.start;
		else if ( newhash != reader.load ) loadItem(newhash, false, true); 
		else if ( newmode ) setGUI();

		
		
}

function restoreSession() {
	try {
			if ( window.localStorage != null ) {
				// restore bookmarks
				var bookmarkString = window.localStorage["bookmarks_"+reader.project.id+"_"+location.host+location.pathname];
			
				var bookmarks;
				if ( bookmarkString != null ) bookmarks = JSON.parse(bookmarkString);
				if ( bookmarks != null ) reader.project.bookmarks = bookmarks;
			
				// restore local light tables
				var tableString = window.localStorage["lighttables_"+reader.project.id+"_"+location.host+location.pathname];
				var tables;
				if ( tableString != null ) tables = JSON.parse(tableString);
				if ( tables != null ) reader.project.localLitas = tables;
			}
		} catch (e) {
			// silent fail
			console.log("restore error: ", e);
	}
}

function setImageLoadingIndicator(showIndicator) {
	if ( showIndicator ) $('#imageLoadingIndicator').css("display", "block");
	else $('#imageLoadingIndicator').css("display", "none");
}
function setLoadingIndicator(showIndicator) {
	if ( showIndicator ) $('#loadingIndicator').css("display", "block");
	else $('#loadingIndicator').css("display", "none");
}

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
						$.ajax({ url: reader.path+contentID+'.xml', async: false, data: null, dataType: 'xml', success: function(data) { xmlData = data; } });
						if ( xmlData != null ) parseItem(xmlData, 'success');
						var viewID = $(xmlData).find('view').attr('id');
						$.ajax({ url: reader.path+viewID+'.xml', async: false, data: null, dataType: 'xml', success: function(data) { xmlData = data; } });
						if ( xmlData != null ) parseItem(xmlData, 'success');
					}
					setLoadingIndicator(false);
					addLayerToLightTable(contentID);
					break;

				case 'view':
					var view = reader.project.items[contentID];
					if ( view == null ) {
						$.ajax({ url: reader.path+contentID+'.xml', async: false, data: null, dataType: 'xml', success: function(data) { xmlData = data; } });
						if ( xmlData != null ) parseItem(xmlData, 'success');
						view = reader.project.items[contentID];
					}
					setLoadingIndicator(false);
					addViewToLightTable(view.id);
					break;

				case 'object':
					var object = reader.project.items[contentID];
					if ( object == null ) {
						$.ajax({ url: reader.path+contentID+'.xml', async: false, data: null, dataType: 'xml', success: function(data) { xmlData = data; } });
						if ( xmlData != null ) parseItem(xmlData, 'success');
						object = reader.project.items[contentID];
					}
					var view = reader.project.items[object.defaultViewID];
					if ( view == null ) {
						$.ajax({ url: reader.path+object.defaultViewID+'.xml', async: false, data: null, dataType: 'xml', success: function(data) { xmlData = data; } });
						if ( xmlData != null ) parseItem(xmlData, 'success');
						view = reader.project.items[object.defaultViewID];
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

function initGUI() {
	/*
	 * set up menu bar
	 */
	jQuery('ul.mainmenu').superfish({
		hoverClass:    'sfHover',          // the class applied to hovered list items
		delay:         0,                // the delay in milliseconds that the mouse can remain outside a submenu without it closing
		animation:     {opacity:'show'},   // an object equivalent to first parameter of jQuery’s .animate() method. Used to animate the submenu open
		animationOut:  {opacity:'hide'},   // an object equivalent to first parameter of jQuery’s .animate() method Used to animate the submenu closed
		speed:         0,           // speed of the opening animation. Equivalent to second parameter of jQuery’s .animate() method
		speedOut:      0,             // speed of the closing animation. Equivalent to second parameter of jQuery’s .animate() method
	});
	$('ul.mainmenu').click(function(e) { $('ul.mainmenu').hideSuperfishUl(); });

	/*
	 * set up project metadata fields
	 */
	var mdDIV = document.getElementById("metadataBar");
	var tempHTML = "";
	// skip first field (1st field == annotation)
	for (i=1; i < reader.project.sortedFields.length; i++) {
		tempHTML += '<div id="'+reader.project.sortedFields[i]+'_field" class="mdfield">\n';
		tempHTML += '  <div id="'+reader.project.sortedFields[i]+'_key" class="mdkey">&nbsp;</div>\n';
		tempHTML += '  <div id="'+reader.project.sortedFields[i]+'_value" class="mdvalue">&nbsp;</div>\n';
		tempHTML += '</div>\n';
	}
	// add view metadata fields: title, source
	tempHTML += '<div id="viewtitle_field" class="mdfield">\n';
	tempHTML += '  <div id="viewtitle_key" class="METADATA_TITLE_VIEW mdkey">&nbsp;</div>\n';
	tempHTML += '  <div id="viewtitle_value" class="mdvalue">&nbsp;</div>\n';
	tempHTML += '</div>\n';
	tempHTML += '<div id="viewsource_field" class="mdfield">\n';
	tempHTML += '  <div id="viewsource_key" class="METADATA_SOURCE_VIEW mdkey">&nbsp;</div>\n';
	tempHTML += '  <div id="viewsource_value" class="mdvalue">&nbsp;</div>\n';
	tempHTML += '</div>\n';
	
	mdDIV.innerHTML = tempHTML;

	// set up start item in menus
	$("a.startitem").each(function(index, item) { item.href = '#'+reader.start+"/"; });
	
	// add bookmarks from last session
	var foundBookmarks = false;
	for ( var i=0; i < reader.project.bookmarks.length; i++ )
		$('#bookmarkmenu').append('<li><a href="#'+reader.project.bookmarks[i].id+'/">'+reader.project.bookmarks[i].title+'</a></li>');
	if ( reader.project.bookmarks.length > 0 ) foundBookmarks = true;
	// init bookmark menu
	setMenuItem("deleteBookmarkLink", "javascript:deleteBookmark();", foundBookmarks);
	setMenuItem("deleteAllBookmarksLink", "javascript:deleteAllBookmarks();", foundBookmarks);

	// add local light tables from last session
	var foundTables = false;
	for ( var i=(reader.project.localLitas.length-1); i >=0 ; i-- )
		$('#localLightTables').after('<li><a href="javascript:loadLocalTable('+i+');">'+reader.project.localLitas[i].title[reader.project.defaultLang]+'</a></li>');
	if ( reader.project.localLitas.length > 0 ) foundTables = true;
	// init light table menu
	setMenuItem("deleteLocalTableLink", "javascript:deleteLocalTable();", foundTables);
	
	/*
	 * set up menu languages
	 */
	for (var i=reader.project.langs.length-1; i >= 0 ; i--)
		if ( reader.strings[reader.project.defaultLang]['MENU_LANG_'+reader.project.langs[i]] != null )
			$('#languageMenu').prepend('<li><a id="setLang_'+reader.project.langs[i]+'" href="javascript:setLanguage(\''+reader.project.langs[i]+'\');" class="'+'MENU_LANG_'+reader.project.langs[i]+'">&nbsp;</a></li>');
		else
			$('#languageMenu').prepend('<li><a id="setLang_'+reader.project.langs[i]+'" href="javascript:setLanguage(\''+reader.project.langs[i]+'\');">'+reader.project.langs[i]+'</a></li>');
	
	/*
	 * set up group, text and lighttable menu
	 */
	tempHTML = "";
	groupDIV = $("#groupmenu");
	textDIV = $("#textmenu");
	litaDIV = $("#publicLightTables");
	for (group in reader.project.groups) 
		groupDIV.append('<li><a class="'+group+'_menuitem" href="#'+group+'/">&nbsp;</a></li>');	
	if ( Object.keys(reader.project.groups).length == 0 ) $('#groupmenu').parent().css("display", "none");
	for (text in reader.project.texts) 
		textDIV.append('<li><a class="'+text+'_menuitem" href="#'+text+'/">&nbsp;</a></li>');	
	if ( Object.keys(reader.project.texts).length == 0 ) $('#textmenu').parent().css("display", "none");	
	$(Object.keys(reader.project.litas).reverse()).each(function (index, lita) {
		litaDIV.after('<li><a class="'+lita+'_menuitem" href="#'+lita+'/">&nbsp;</a></li>');			
	});

	/*
	 * set up SVG canvas
	 */
	$('#canvas').svg();
	reader.canvas.svg = $('#canvas').svg('get');
	reader.canvas.svg.root().setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
	reader.canvas.svg.root().setAttribute("xmlns:xhtml", "http://www.w3.org/1999/xhtml");
	reader.canvas.imageGroup = reader.canvas.svg.group(reader.canvas.svg.root(),'canvasImageGroup',{});

	reader.canvas.image = reader.canvas.svg.image(reader.canvas.imageGroup, 0, 0, 100, 100, '#', {id: 'canvasImage'});
	reader.canvas.layerGroup = reader.canvas.svg.group(reader.canvas.imageGroup, 'canvasLayerGroup', {});
	$('#canvas').dragscrollable({acceptPropagatedEvent: true}); // enable mouse scrolling
	// set move cursor handler
	$("#canvas").hover(function(ev) {
		if ( reader.zoom.cur > reader.zoom.image ) $(ev.currentTarget).css('cursor', 'move');
	}, function (ev) { $(ev.currentTarget).css('cursor', 'default'); });
	
	// watch for window size change
	$(window).resize(function (e) { 
		calcImageScales(); 
		$('#sidebar').css('max-height', $(window).height()-reader.zoom.yOffset);
		$('#searchResults').css('max-height', $(window).height()-reader.zoom.yOffset-69);
		$('#searchOptions').css('max-height', $(window).height()-reader.zoom.yOffset-69);
	});
	$('#sidebar').css('max-height', $(window).height()-reader.zoom.yOffset);
	$('#searchResults').css('max-height', $(window).height()-reader.zoom.yOffset-69);
	$('#searchOptions').css('max-height', $(window).height()-reader.zoom.yOffset-69);
		
		
	// set up URL / loading handler
	var ev = $(window).bind('hashchange', loadHandler);
	
	// set up item loading indicator
	$('#loadingIndicator').activity({
		segments: 12,
		width: 2,
		space: 1,
		length: 4,
		speed: 1.2
	});
	// set up image loading indicator
	$('#imageLoadingIndicator').activity({
		segments: 12,
		width: 9,
		space: 6,
		length: 20,
		speed: 2.2
	});
	
	// set up search UI
	$('#basicSearchButton').click(function(e) {
		$('#basicSearchButton').hide();
		$('#extendedSearchButton').show();
		$('#searchResults').show();
		$('#searchOptions').hide();
	});
	$('#extendedSearchButton').click(function(e) {
		$('#basicSearchButton').show();
		$('#extendedSearchButton').hide();
		$('#searchResults').hide();
		$('#searchOptions').show();
	});
	$('#searchButton').click(function(e) {performSearch();});
	$('#searchInput').keypress(function(e) { if (e.which == 13) performSearch(); });
	$('#search_everything').change(searchAllHandler);
		
}

function showTab(tab) {
	// 0 = metadata, 1 = annotation, 2 = inscription, 3 = search
	$('ul.tabmenu li').each(function(index, item) {
		if (index != tab) $(item).removeClass('selected'); else $(item).addClass('selected');
	});
	$('.sidebars').each(function(index, item) {
		if (index == tab) $(item).removeClass('hidden'); else $(item).addClass('hidden');
	});
	if ( tab == 2 && reader.load != null && reader.project.items[reader.load].type == 'inscription' )  highlightSourceLayer();
}

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
	
	/* Load User Preferences from XML */
	$.ajax({ url: 'resource/hi_prefs.xml', dataType: 'xml',
	success: function(data, success) {
		loadPrefs(data, success);

		/* Load User Strings from XML */
		$.ajax({ url: 'resource/hi_strings.xml', dataType: 'xml',
		success: function(data, success) {
			loadStrings(data, success);
			
			/* load initial project info from XML */	
			$.ajax({ url: reader.prefs['INITIAL_REF'], dataType: 'xml',
			success: function(data, success) {
				loadStartFile(data, success);
				
				/* load initial project data from XML */
				$.ajax({ url: reader.path+reader.project.id+'.xml', dataType: 'xml',
				success: function(data, success) {
					loadProjectFile(data, success);
					
					// restore session if possible
					restoreSession();
					newLocalTable(false);
					// init GUI
					initGUI();
					
					// init context menus
					initContextMenus();

					// update on screen language
					setLanguage(reader.project.defaultLang);
					
					// load search index for project languages in background
					// TODO implement as Web Worker
					$(Object.keys(reader.project.search.files)).each(function (index, fileLang) {
						$.get('postPetal/'+reader.project.search.files[fileLang], function(data, success) {
							reader.search.data[fileLang] = data;
							window.setTimeout('loadSearchFile("'+fileLang+'", "+success+")', 2000);
						});
					});

					// load start item or user requested item
					var mode = 'regular';
					if ( location.hash.substring(1).split('/').length > 1 && location.hash.substring(1).split('/')[1].length > 0 )
						mode = location.hash.substring(1).split('/')[1];
					if ( mode != 'regular' && mode != 'all' && mode != 'refs' && mode != 'sites' ) mode = 'regular';
					reader.mode = mode;
					if ( location.hash.length > 0 ) loadItem(location.hash.substring(1).split('/')[0], false, true);
					else location.hash = reader.start+"/";
					
					console.log(reader);
				}, error: function(error) {reportError('Required file "'+reader.path+reader.project.id+'.xml" missing or load error.')} });
			}, error: function(error) {reportError('Required file "'+reader.prefs['INITIAL_REF']+'" (pref key INITIAL_REF) missing or load error.')} });
		}, error: function(error) {reportError('Required file "resource/hi_strings.xml" missing or load error.')} });
	}, error: function(error) {reportError('Required file "resource/hi_prefs.xml" missing or load error.<br><br><strong>Did you start the Reader from your harddrive?</strong><br>For this online publication to work you need to upload your project to a web server.<br>An offline version is included in this package and available from our website.')} });

	

	
}
