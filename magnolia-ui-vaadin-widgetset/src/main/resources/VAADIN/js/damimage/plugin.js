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
    var EVENT_SEND_MAGNOLIA_LINK = "mgnDamImageSelected";
    var EVENT_CANCEL_LINK = "mgnlLinkCancel";
    var EVENT_GET_MAGNOLIA_LINK = "mgnlGetDamImage";
    
    CKEDITOR.plugins.add('damimage', {
        init: function(editor) {
            editor.ui.addButton('DamImage', {
                label: 'Image from DAM',
                command: 'damimage',
                icon: "../../../themes/admincentraltheme/img/damimage.png"
            });

            editor.addMenuGroup('damgroup');

            editor.addMenuItem('damimage', { 
                label: 'Edit DAM image link',
                command: 'damimage',
                group: 'damgroup',
                icon: "../../../themes/admincentraltheme/img/damimage.png"
            });

            /*
             * Firefox will lose focus when popping up a dialog. So a variable
             * is needed to restore selection after dialog has closed.
             */
            var selectionRangeHack = null;

            // Request DAM App dialog
            editor.addCommand('damimage', {
                exec: function(editor) {
                    selectionRangeHack = editor.getSelection().getRanges(true);
                    var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                    
                    if (isImage(selectedElement)) {
                        var href = selectedElement.getAttribute('src');
                        editor.fire(EVENT_GET_MAGNOLIA_LINK, href);                      
                    } else {
                        editor.fire(EVENT_GET_MAGNOLIA_LINK);
                    }

                    setReadOnly(editor, true);
                }
            });

            // Respond from DAM App
            editor.on(EVENT_SEND_MAGNOLIA_LINK, function(e) {
                setReadOnly(editor, false);
                editor.getSelection().selectRanges(selectionRangeHack);             
                var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                var link = eval('('+e.data+')');

                if (isImage(selectedElement)) {
                    selectedElement.setAttribute('src', link.path);
                } else {
                    var selectedText = editor.getSelection();
                    var elem = editor.document.createElement('img');
                    elem.setAttribute('src', link.path);

                    if (selectedText && selectedText.getSelectedText() != '') {
                        elem.setAttribute('alt', selectedText.getSelectedText());                        
                    } else {                        
                        elem.setAttribute('alt', link.caption);
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

            // Context menu
            editor.contextMenu.addListener(function(element, selection) {       
                if (!isImage(element)) {
                    return null;
                } 

                return {
                    damimage: CKEDITOR.TRISTATE_OFF
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
   
    function isImage(element) {
        if (element && element.getName().toLowerCase() == 'img') {
            return true;
        } else {
            return false;
        }
    }
})();