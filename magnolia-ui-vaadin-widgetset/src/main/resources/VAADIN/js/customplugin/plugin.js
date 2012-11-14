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
	CKEDITOR.plugins.add('customplugin', {
		init: function(editor) {
		    
			editor.addCommand('customplugin', {
				exec: function(editor) {
					editor.fire('customStuffHappened');
				}
			});
			
			editor.on('serverSendToCustomPlugin', function(e) {
			    var elem = editor.document.createElement('a');
			    var response = e.data.match(/handle\:\{([^\}]*)\}/);
			    elem.setAttribute('href', e.data);			    
			    elem.setHtml(response[1]);
			    editor.insertElement(elem);
			});

			editor.ui.addButton('CustomPlugin', {
				label: 'Link to custom page',
				command: 'customplugin',
				icon: "mlink.png"
			});
			

			editor.addMenuGroup('ctxgroup');
			editor.addMenuItem('menuitem', { 
				label: 'Do server stuff',
				command: 'customplugin',
				group: 'ctxgroup'				
			});
			
			editor.on('doubleclick', function(ev) {

			});
			
			editor.on( 'selectionChange', function( evt ) {
				if ( editor.readOnly )
					return;

				//var command = editor.getCommand( 'unlink' ),
				var	element = evt.data.path.lastElement && evt.data.path.lastElement.getAscendant( 'a', true );
				if ( element && element.getName() == 'a' && 
						element.getAttribute( 'href' ) && 
						element.getChildCount() && 
						element.getAttribute( 'href').substring(0,1) == '$') {
					
					editor.getCommand('unlink').setState( CKEDITOR.TRISTATE_DISABLED );
					editor.getCommand('link').setState( CKEDITOR.TRISTATE_DISABLED );
					editor.getCommand('customplugin').setState( CKEDITOR.TRISTATE_OFF );
				} else {
					editor.getCommand('link').setState( CKEDITOR.TRISTATE_OFF );
					//editor.getCommand('customplugin').setState( CKEDITOR.TRISTATE_DISABLED );
				}
			} );
			
			editor.contextMenu.addListener( function(element, selection) {
    			function getSelectedElement(editor, type) {
            		try {
            			var selection = editor.getSelection();
            			if ( selection.getType() == CKEDITOR.SELECTION_ELEMENT ) {
            				var selectedElement = selection.getSelectedElement();
            				if ( selectedElement.is(type) ) {
            					return selectedElement;
            				}
            			}
            
            			var range = selection.getRanges(true)[0];
            			range.shrink(CKEDITOR.SHRINK_TEXT);
            			var root = range.getCommonAncestor();
            			return root.getAscendant(type, true );
            		} catch( e ) { 
            			return null; 
            		}
            	};
    	
    	        var elem = getSelectedElement(editor, 'a');
	
	            if(elem && elem.is('a')) {
					return { menuitem : CKEDITOR.TRISTATE_OFF };
	            } else {				    
				    return null;
	            }
	            
			});
		}
	});
})();