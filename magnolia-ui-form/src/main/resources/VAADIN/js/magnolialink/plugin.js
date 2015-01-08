(function() {
    var EVENT_SEND_MAGNOLIA_LINK = "mgnlLinkSelected";
    var EVENT_CANCEL_LINK = "mgnlLinkCancel";
    var EVENT_GET_MAGNOLIA_LINK = "mgnlGetLink";
    var WORKSPACES = ['website', 'dam'];

    CKEDITOR.plugins.add('magnolialink', {
        init: function(editor) {
            editor.ui.addButton('InternalLink', {
                label: 'Link to Magnolia page',
                command: 'website',
                icon: "../../../themes/admincentraltheme/img/s02-internal-link.png"
            });

            editor.ui.addButton('DamLink', {
                label: 'Link to DAM document',
                command: 'dam',
                icon: "../../../themes/admincentraltheme/img/damdoc.png"
            });

            editor.addMenuGroup('mlinkgroup');

            editor.addMenuItem('website', {
                label: 'Edit Magnolia Link',
                command: 'website',
                group: 'mlinkgroup',
                icon: "../../../themes/admincentraltheme/img/s02-internal-link.png"
            });

            editor.addMenuItem('dam', {
                label: 'Edit DAM Link',
                command: 'dam',
                group: 'mlinkgroup',
                icon: "../../../themes/admincentraltheme/img/damdoc.png"
            });

            /*
             * Firefox will lose focus when popping up a dialog. So a variable
             * is needed to restore selection after dialog has closed.
             */
            var selectionRangeHack = null;

            function reqDialog(workspace) {
                return function(editor) {
                    selectionRangeHack = editor.getSelection().getRanges(true);
                    var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);

                    if (isInternalLink(selectedElement)) {
                        var href = selectedElement.getAttribute('href');
                        var path = href.match(/path\:\{([^\}]*)\}/);
                        var repository = getWorkspace(selectedElement);

                        if(!path) {
                            path = href.match(/handle\:\{([^\}]*)\}/);
                        }

                        editor.fire(EVENT_GET_MAGNOLIA_LINK, '{\'workspace\' :\''+ repository+'\', \'path\': \''+path[1]+'\'}');
                    } else {
                        editor.fire(EVENT_GET_MAGNOLIA_LINK, '{\'workspace\' :\''+ workspace+'\'}');
                    }

                    setReadOnly(editor, true);
                };
            }

            // Setup commands requesting App dialogs
            for (var index = 0; index < WORKSPACES.length; index++) {
                editor.addCommand(WORKSPACES[index], {
                    exec: reqDialog(WORKSPACES[index])
                });
            }

            // Respond from Pages app
            editor.on(EVENT_SEND_MAGNOLIA_LINK, function(e) {
                setReadOnly(editor, false);
                editor.getSelection().selectRanges(selectionRangeHack);
                var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                var link = eval('('+e.data+')');
                var href = '${link:{uuid:{'+link.identifier+
                    '},repository:{'+link.repository+
                    '},path:{'+link.path+
                    '}}}';

                if (isLink(selectedElement)) {
                    selectedElement.setAttribute('href', href);
                } else {
                    var selectedText = editor.getSelection();
                    var elem = editor.document.createElement('a');
                    elem.setAttribute('href', href);

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
                    editor.getCommand(getWorkspace(selected)).exec(editor);
                }
            });

            // Selection change
            editor.on( 'selectionChange', function( evt ) {
                if ( editor.readOnly ) {
                    return;
                }

                var element = evt.data.path.lastElement && evt.data.path.lastElement.getAscendant( 'a', true );

                if (isLink(element)) {
                    for (var index = 0; index < WORKSPACES.length; index++) {
                        editor.getCommand(WORKSPACES[index]).setState(CKEDITOR.TRISTATE_DISABLED);
                    }
                    editor.getCommand('unlink').setState(CKEDITOR.TRISTATE_OFF);

                    var workspace = getWorkspace(element);
                    if (workspace) {
                        editor.getCommand(workspace).setState(CKEDITOR.TRISTATE_OFF);
                    }
                } else {
                    for (var index = 0; index < WORKSPACES.length; index++) {
                        editor.getCommand(WORKSPACES[index]).setState(CKEDITOR.TRISTATE_OFF);
                    }

                    editor.getCommand('link').setState(CKEDITOR.TRISTATE_OFF);
                    editor.getCommand('unlink').setState(CKEDITOR.TRISTATE_DISABLED);
                }
            });

            // Context menu
            editor.contextMenu.addListener(function(element, selection) {
                if (!isInternalLink(element)) {
                    return null;
                }

                var retval = {};
                retval[getWorkspace(element)] = CKEDITOR.TRISTATE_OFF;
                return retval;
            });
        }
    });

    function getWorkspace(element) {
        var href = element.getAttribute('href');
        var repository = href.match(/repository\:\{([^\}]*)\}/);

        if (repository) {
            return repository[1];
        } else {
            return false;
        }
    }

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