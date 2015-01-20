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

// legacy support for old generated html pages in content directory
// redirect from flash to new html5 reader


function startHTML5Reader() {
	if ( document.getElementsByTagName('embed').length > 0 ) {
		var flashElement = document.getElementsByTagName('embed').item(0);
		document.getElementsByTagName('body').item(0).removeChild(flashElement);
		var flashVars = flashElement.getAttribute('flashVars');
		var elementID = flashVars.substring(flashVars.indexOf('ref=')+4, flashVars.length);
		location.href='../index.html#'+elementID+'/';
	}
}
window.onload=startHTML5Reader;
