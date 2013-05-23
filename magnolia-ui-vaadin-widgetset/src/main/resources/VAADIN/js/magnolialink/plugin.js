/**
 * This file Copyright (c) 2010-2012 Magnolia International Ltd.
 * (http://www.magnolia-cms.com). All rights reserved.
 * 
 * 
 * This file is dual-licensed under both the Magnolia Network Agreement and the
 * GNU General Public License. You may elect to use one or the other of these
 * licenses.
 * 
 * This file is distributed in the hope that it will be useful, but AS-IS and
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT. Redistribution,
 * except as permitted by whichever of the GPL or MNA you select, is prohibited.
 * 
 * 1. For the GPL license (GPL), you can redistribute and/or modify this file
 * under the terms of the GNU General Public License, Version 3, as published by
 * the Free Software Foundation. You should have received a copy of the GNU
 * General Public License, Version 3 along with this program; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * 2. For the Magnolia Network Agreement (MNA), this file and the accompanying
 * materials are made available under the terms of the MNA which accompanies
 * this distribution, and is available at http://www.magnolia-cms.com/mna.html
 * 
 * Any modifications to this file must keep this entire header intact.
 * 
 */

(function() {
    var EVENT_SEND_MAGNOLIA_LINK = "mgnlLinkSelected";
    var EVENT_CANCEL_LINK = "mgnlLinkCancel";
    var EVENT_GET_MAGNOLIA_LINK = "mgnlGetLink";
    
    CKEDITOR.on('dialogDefinition', function(event) {
        if (event.data.name == 'image') {
            event.data.definition.removeContents('advanced');
            event.data.definition.addButton({
                type: 'button',
                id: 'newstuff',
                disabled: false, 
                label: 'extra btn',
                onClick: function() {
                    alert('clickki');
                }
                   
            });
        }
    });

    
    CKEDITOR.plugins.add('magnolialink', {
        init: function(editor) {
            editor.ui.addButton('InternalLink', {
                label: 'Link to Magnolia page',
                command: 'magnolialink',
                icon: "../../../themes/admincentraltheme/img/mlink.png"
            });

            editor.addMenuGroup('mlinkgroup');

            editor.addMenuItem('magnolialink', { 
                label: 'Edit Magnolia Link',
                command: 'magnolialink',
                group: 'mlinkgroup',
                icon: "../../../themes/admincentraltheme/img/mlink.png"
            });
            
            editor.ui.addRichCombo('combo', {
                icon: "http://localhost:8080/VAADIN/themes/admincentraltheme/img/mlink.png",
                label: "kek",
                title: 'cmb',
                panel: {
                    css : [ editor.config.contentsCss, CKEDITOR.getUrl(CKEDITOR.skinName.split(",")[1]||"skins/"+CKEDITOR.skinName.split(",")[0]+"/") + "editor.css" ]
                },
                init: function() {
                    //this.startGroup('combo');
                    this.add('a1', '<img src="http://localhost:8080/VAADIN/themes/admincentraltheme/img/mlink.png"></img>', 'c1');
                    this.add('a2', 'b2', 'c2');
                }
            });

            /*
             * Firefox will lose focus when popping up a dialog. So a variable
             * is needed to restore selection after dialog has closed.
             */
            var selectionRangeHack = null;

            // Request Pages app dialog
            editor.addCommand('magnolialink', {
                exec: function(editor) {
                    selectionRangeHack = editor.getSelection().getRanges(true);
                    var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                    
                    if (isInternalLink(selectedElement)) {
                        var href = selectedElement.getAttribute('href');                        
                        var path = href.match(/path\:\{([^\}]*)\}/);

                        if(!path) {
                            path = href.match(/handle\:\{([^\}]*)\}/);
                        }

                        editor.fire(EVENT_GET_MAGNOLIA_LINK, path[1]);                      
                    } else {
                        editor.fire(EVENT_GET_MAGNOLIA_LINK);
                    }

                    setReadOnly(editor, true);
                }
            });

            // Respond from Pages app
            editor.on(EVENT_SEND_MAGNOLIA_LINK, function(e) {
                setReadOnly(editor, false);
                editor.getSelection().selectRanges(selectionRangeHack);             
                var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                var link = eval('('+e.data+')');
                var href = '${link:{uuid:{'+link.identifier+
                    '},repository:{'+link.repository+
                    '},path:{'+link.path+
                    '},nodeData:{},extension:{html}}}';

                if (isLink(selectedElement)) {
                    selectedElement.setAttribute('href', href);
                } else {
                    var selectedText = editor.getSelection();
                    var elem = editor.document.createElement('img');
                    elem.setAttribute('src', link.path);

                    if (selectedText && selectedText.getSelectedText() != '') {
                        elem.setHtml(selectedText.getSelectedText());
                    } else {
                        elem.setHtml(link.caption);
                    }

                    editor.insertElement(elem);
                }
            });

            editor.on(EVENT_CANCEL_LINK, function(e) {
                if (e.data) {
                    alert(e.data);
                }
                
                setReadOnly(editor, false);
            });

            // Double click
            editor.on('doubleclick', function(ev) {             
                var selected = CKEDITOR.plugins.link.getSelectedLink(editor);
                
                if (isInternalLink(selected)) {
                    ev.data.dialog = null; // Eat the CK link dialog
                    editor.getCommand('magnolialink').exec(editor);
                }
            });

            // Selection change
            editor.on( 'selectionChange', function( evt ) {
                if ( editor.readOnly ) {
                    return;
                }

                var element = evt.data.path.lastElement && evt.data.path.lastElement.getAscendant( 'a', true );
                var internalLinkState = CKEDITOR.TRISTATE_OFF;
                var externalLinkState = CKEDITOR.TRISTATE_OFF;

                if (isLink(element) && !isInternalLink(element)) {
                    internalLinkState = CKEDITOR.TRISTATE_DISABLED;                 
                    }

                if (isInternalLink(element)) {
                    externalLinkState = CKEDITOR.TRISTATE_DISABLED;
                    }

                editor.getCommand('magnolialink').setState(internalLinkState);
                editor.getCommand('link').setState(externalLinkState);              
                });

            // Context menu
            editor.contextMenu.addListener(function(element, selection) {       
                if (!isInternalLink(element)) {
                    return null;
                } 

                return {
                    magnolialink: CKEDITOR.TRISTATE_OFF
                };
            });
        }
    });

    function setReadOnly(editor, isReadOnly) {
        if (isReadOnly) {
            editor.element.setStyle('opacity', '0.2');
        } else {
            editor.element.removeStyle('opacity');
        }
    }

    function isInternalLink(element) {
        if (isLink(element) && element.getAttribute('href').substring(0,1) == '$') {
            return true;
        }

        return false;
    };

    function isLink(element) {
        if (element && element.getName().toLowerCase() == 'a' && element.getAttribute('href') && element.getChildCount()) {
            return true;
        } else {
            return false;
        }
    }
})();