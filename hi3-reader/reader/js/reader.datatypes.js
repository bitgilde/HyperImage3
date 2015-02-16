/*
 * Copyright 2013 Leuphana Universität Lüneburg. All rights reserved.
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.
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
 */
function HIReader() {
	this.version = 'v3.0.beta1';
	this.productID = 'Static Reader';
	
	this.xmlserializer = new XMLSerializer();
	this.prefs = new Object();
	this.strings = new Object();
	this.strings.length = 0;
	this.load = null;
	this.viewID = null;
	this.last = null;
	this.fromLayer = null;
	this.allLayers = false;
	this.fromSearch = false;
	this.mode = 'regular';
	this.path = null;
	this.start = null;
	this.table = null;
	this.canvas = new Object();
	this.search = new Object();
	this.search.data = new Object();
	this.search.term = null;
	this.search.resultTerms = new Array();
	this.search.active = false;
	this.search.titles = {
		text: "SEARCH_TEXT",
		text_title: "SEARCH_TEXT_TITLE",
		text_content: "SEARCH_TEXT_CONTENT",
		object: "SEARCH_OBJECT",
		object_inscription: "SEARCH_OBJECT_INSCRIPTION",
		view: "SEARCH_VIEW",
		view_title: "SEARCH_VIEW_TITLE",
		view_annotation: "SEARCH_VIEW_ANNOTATION",
		view_source: "SEARCH_VIEW_SOURCE",
		layer: "SEARCH_LAYER",
		layer_title: "SEARCH_LAYER_TITLE",
		layer_annotation: "SEARCH_LAYER_ANNOTATION",
		lita: "SEARCH_LITA",
		lita_title: "SEARCH_LITA_TITLE",
		lita_annotation: "SEARCH_LITA_ANNOTATION",
		url: "SEARCH_URL",
		url_title: "SEARCH_URL_TITLE",
		url_annotation: "SEARCH_LAYER_ANNOTATION",
		group: "SEARCH_GROUP",
		group_title: "SEARCH_GROUP_TITLE",
		group_annotation: "SEARCH_GROUP_ANNOTATION"
	};
	this.zoom = new Object();
	this.zoom.xOffset = 330;
	this.zoom.yOffset = 48;
	this.zoom.max = 2.0;
	this.zoom.cur = 0.0;
	this.zoom.image = 0.0;
	this.zoom.window = 0.0;
	this.zoom.full = 1.0;
	this.zoom.min = 0.0;
	this.project = new Object();
	this.project.id = null;
	this.project.items = new Object();
	this.project.title = new Object();
	this.project.texts = new Object();
	this.project.groups = new Object();
	this.project.litas = new Object();
	this.project.templates = new Object();
	this.project.langs = new Array();
	this.project.defaultLang = null;
	this.project.search = new Object();
	this.project.bookmarks = new Array();
	this.project.localLitas = new Array();
	
	// imprint
	this.project.items['imprint'] = new HIText('imprint');
}
reader = new HIReader();

function HIObject(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.type = "object";
	this.defaultViewID = null;
	this.md = new Object();
	this.views = new Object();
	this.siblings = new Object();

}
function HIView(id, uuid) {
	this.uuid = uuid;
	this.id = id;
	this.type = "view";
	this.files = new Object();
	this.title = new Object();
	this.source = new Object();
	this.annotation = new Object();
	this.layers = new Object();
	this.sites = new Object();
	this.refs = new Object();
	this.parent = null;	

}
function HIInscription(id, uuid) {
	this.id = id;
	this.type = "inscription";
	this.content = new Object();
	this.sites = new Object();
	this.refs = new Object();
	this.parent = null;	

}
function HILayer(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.type = "layer";
	this.color = null;
	this.opacity = 1.0;
	this.ref = null;
	this.polygons = new Array();
	this.parent = null;
	this.title = new Object();
	this.annotation = new Object();

}
function HIGroup(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.type = "group";
	this.title = new Object();
	this.annotation = new Object();
	this.members = new Object();
	this.sites = new Object();
	this.refs = new Object();
	
}
function HIContent(id, uuid) {
	this.target = id;
	this.uuid = uuid;
	this.type = null;
	this.title = new Object();
	this.content = new Object();
	this.image = null;
	this.size = 0;

}
function HIFrame(x, y, width, height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	this.href = null;
	this.imagePos = new Object();
}
function HIFrameAnnotation() {
	this.x = 14;
	this.y = 64;
	this.width = 200;
	this.height = 200;
	this.annotation = new Object();
	this.visible = false;
}
function HILighttable(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.type = "lita";
	this.title = new Object();
	this.frames = new Array();
	this.frameAnnotation = new HIFrameAnnotation();
	this.sites = new Object();
	this.refs = new Object();
	
}
function HIText(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.type = "text";
	this.title = new Object();
	this.content = new Object();
	this.sites = new Object();
	this.refs = new Object();
	
}
function HIURL(id, uuid) {
	this.id = id;
	this.uuid = uuid;
	this.title = null;
	this.annotation = null;
	this.type = "url";
	this.url = null;
	this.sites = new Object();
	
}
function HIBookmark(id, title) {
	this.id = id;
	this.title = title;
}
var typeKeys = {
	'group': "GRP_GROUP",
	'text': "GRP_TEXT",
	'lightTable': "GRP_LITA",
	'object': "GRP_OBJECT",
	'inscription': "GRP_INSCRIPTION",
	'view': "GRP_VIEW",
	'layer': "GRP_LAYER",
	'url': "GRP_URL"
};

var searchTypeKeys = {
	'group': "SEARCH_RESULT_GROUP",
	'text': "SEARCH_RESULT_TEXT",
	'lita': "SEARCH_RESULT_LITA",
	'object': "SEARCH_RESULT_OBJECT",
	'inscription': "SEARCH_RESULT_INSCRIPTION",
	'view': "SEARCH_RESULT_VIEW",
	'layer': "SEARCH_RESULT_LAYER",
	'url': "GRP_URL"
};
