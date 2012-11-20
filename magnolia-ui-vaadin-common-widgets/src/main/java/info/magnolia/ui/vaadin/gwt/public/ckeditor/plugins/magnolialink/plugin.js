/**
 * This file Copyright (c) 2010-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

(function() {
	CKEDITOR.plugins.add('magnolialink', {
		init: function(editor) {
			editor.ui.addButton('InternalLink', {
				label: 'Link to Magnolia page',
				command: 'magnolialink',
				icon: "mlink.png"
			});
			
			editor.addMenuGroup('mlinkgroup');
			
			editor.addMenuItem('magnolialink', { 
				label: 'Edit Magnolia Link',
				command: 'magnolialink',
				group: 'mlinkgroup',
				icon: "mlink.png"
			});
			
			/* Firefox will lose focus when popping up a dialog.
			 * So a variable is needed to restore selection after
			 * dialog has closed.
			 */
			var selectionRangeHack = null;
			
			//Request Pages app dialog
			editor.addCommand('magnolialink', {
				exec: function(editor) {
					selectionRangeHack = editor.getSelection().getRanges(true);
	    	        var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
		            if(isInternalLink(selectedElement)) {
		            	var href = selectedElement.getAttribute('href');
		            	var path = href.match(/handle\:\{([^\}]*)\}/);		            	
		            	editor.fire('reqMagnoliaLink', path[1]);		            	
		            } else {
		            	editor.fire('reqMagnoliaLink');
		            }
		            editor.setReadOnly();
				}
			});
			
			//Respond from Pages app
			editor.on('sendMagnoliaLink', function(e) {
				editor.setReadOnly(false);
		    	editor.getSelection().selectRanges(selectionRangeHack);		    	
    	        var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
    	        var link = eval('('+e.data+')');
    	        var href = '${link:{uuid:{'+link.identifier+
    	        	'},repository:{'+link.repository+
    	        	'},handle:{'+link.path+
    	        	'},nodeData:{},extension:{html}}}';
    	        
	            if(isLink(selectedElement)) {
	            	selectedElement.setAttribute('href', href);
	            } else {
	            	var selectedText = editor.getSelection();
	            	var elem = editor.document.createElement('a');
				    elem.setAttribute('href', href);
				    
				    if(selectedText && selectedText.getSelectedText() != '') {
				        elem.setHtml(selectedText.getSelectedText());
				    } else {
				        elem.setHtml(link.caption);
				    }
				    
				    editor.insertElement(elem);
	            }
			});
			
			editor.on('cancelLink', function(e) {
				editor.setReadOnly(false);
			});

			//Double click
			editor.on('doubleclick', function(ev) {				
				var selected = CKEDITOR.plugins.link.getSelectedLink(editor);
				if(isInternalLink(selected)) {
					ev.data.dialog = null; //Eat the CK link dialog
					editor.getCommand('magnolialink').exec(editor);
				}
			});
			
			//Selection change
			editor.on( 'selectionChange', function( evt ) {
				if ( editor.readOnly )
					return;
				
				var	element = evt.data.path.lastElement && evt.data.path.lastElement.getAscendant( 'a', true );
				var internalLinkState = CKEDITOR.TRISTATE_OFF;
				var externalLinkState = CKEDITOR.TRISTATE_OFF;
				
				if(isLink(element) && !isInternalLink(element)) {
					internalLinkState = CKEDITOR.TRISTATE_DISABLED;					
				}
				
				if(isInternalLink(element)) {
					externalLinkState = CKEDITOR.TRISTATE_DISABLED;
				}
				
				editor.getCommand('magnolialink').setState(internalLinkState);
				editor.getCommand('link').setState(externalLinkState);				
			} );
			
			//Context menu
			editor.contextMenu.addListener( function(element, selection) {		
				if(!isInternalLink(element)) {
					return null;
				} 
				
				return {magnolialink: CKEDITOR.TRISTATE_OFF};
			});
		}
	});
	
	function isInternalLink(element) {
		if (isLink(element) && 
				element.getAttribute('href').substring(0,1) == '$') {
			return true;
		}
		
		return false;
	};
	
	function isLink(element) {
		if(element &&
				element.getName().toLowerCase() == 'a' &&
				element.getAttribute('href') &&
				element.getChildCount()) {
			return true;
		} else {
			return false;
		}
	}
})();