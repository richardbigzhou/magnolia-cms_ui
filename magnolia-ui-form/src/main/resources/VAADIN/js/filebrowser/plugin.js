(function() {
    // Adds (additional) arguments to given url.
    //
    // @param {String}
    //            url The url.
    // @param {Object}
    //            params Additional parameters.
    function addQueryString( url, params ) {
        var queryString = [];

        if ( !params )
            return url;
        else {
            for ( var i in params )
                queryString.push( i + "=" + encodeURIComponent( params[ i ] ) );
        }

        return url + ( ( url.indexOf( "?" ) != -1 ) ? "&" : "?" ) + queryString.join( "&" );
    }

    // Make a string's first character uppercase.
    //
    // @param {String}
    //            str String.
    function ucFirst( str ) {
        str += '';
        var f = str.charAt( 0 ).toUpperCase();
        return f + str.substr( 1 );
    }

    // The onlick function assigned to the 'Browse Server' button. Opens the
    // file browser and updates target field when file is selected.
    //
    // @param {CKEDITOR.event}
    //            evt The event object.
    function browseServer( evt ) {
        var dialog = this.getDialog();
        var editor = dialog.getParentEditor();

        editor._.filebrowserSe = this;

        var params = this.filebrowser.params || {};
        params.CKEditor = editor.name;
        params.CKEditorFuncNum = editor._.filebrowserFn;
        if ( !params.langCode )
            params.langCode = editor.langCode;

        /**
         * In order to be able to open file browser within Magnolia Admin Central
         * we have to forbid the native window opening and rather notify server so that it
         * handles file/asset selection itself.
         */
        editor.fire("chooseAsset");
    }

    // The onlick function assigned to the 'Upload' button. Makes the final
    // decision whether form is really submitted and updates target field when
    // file is uploaded.
    //
    // @param {CKEDITOR.event}
    //            evt The event object.
    function uploadFile( evt ) {
        var dialog = this.getDialog();
        var editor = dialog.getParentEditor();

        editor._.filebrowserSe = this;

        // If user didn't select the file, stop the upload.
        if ( !dialog.getContentElement( this[ 'for' ][ 0 ], this[ 'for' ][ 1 ] ).getInputElement().$.value )
            return false;

        if ( !dialog.getContentElement( this[ 'for' ][ 0 ], this[ 'for' ][ 1 ] ).getAction() )
            return false;

        return true;
    }

    // Setups the file element.
    //
    // @param {CKEDITOR.ui.dialog.file}
    //            fileInput The file element used during file upload.
    // @param {Object}
    //            filebrowser Object containing filebrowser settings assigned to
    //            the fileButton associated with this file element.
    function setupFileElement( editor, fileInput, filebrowser ) {
        var params = filebrowser.params || {};
        params.CKEditor = editor.name;
        params.CKEditorFuncNum = editor._.filebrowserFn;
        if ( !params.langCode )
            params.langCode = editor.langCode;

        fileInput.action = addQueryString( filebrowser.url, params );
        fileInput.filebrowser = filebrowser;
    }

    // Traverse through the content definition and attach filebrowser to
    // elements with 'filebrowser' attribute.
    //
    // @param String
    //            dialogName Dialog name.
    // @param {CKEDITOR.dialog.definitionObject}
    //            definition Dialog definition.
    // @param {Array}
    //            elements Array of {@link CKEDITOR.dialog.definition.content}
    //            objects.
    function attachFileBrowser( editor, dialogName, definition, elements ) {
        if ( !elements || !elements.length )
            return;

        var element, fileInput;

        for ( var i = elements.length; i--; ) {
            element = elements[ i ];

            if ( element.type == 'hbox' || element.type == 'vbox' || element.type == 'fieldset' )
                attachFileBrowser( editor, dialogName, definition, element.children );

            if ( !element.filebrowser )
                continue;

            if ( typeof element.filebrowser == 'string' ) {
                var fb = {
                    action: ( element.type == 'fileButton' ) ? 'QuickUpload' : 'Browse',
                    target: element.filebrowser
                };
                element.filebrowser = fb;
            }

            if ( element.filebrowser.action == 'Browse' ) {
                var url = element.filebrowser.url;
                if ( url === undefined ) {
                    url = editor.config[ 'filebrowser' + ucFirst( dialogName ) + 'BrowseUrl' ];
                    if ( url === undefined )
                        url = editor.config.filebrowserBrowseUrl;
                }

                if ( url ) {
                    element.onClick = browseServer;
                    element.filebrowser.url = url;
                    element.hidden = false;
                }
            } else if ( element.filebrowser.action == 'QuickUpload' && element[ 'for' ] ) {
                url = element.filebrowser.url;
                if ( url === undefined ) {
                    url = editor.config[ 'filebrowser' + ucFirst( dialogName ) + 'UploadUrl' ];
                    if ( url === undefined )
                        url = editor.config.filebrowserUploadUrl;
                }

                if ( url ) {
                    var onClick = element.onClick;
                    element.onClick = function( evt ) {
                        // "element" here means the definition object, so we need to find the correct
                        // button to scope the event call
                        var sender = evt.sender;
                        if ( onClick && onClick.call( sender, evt ) === false )
                            return false;

                        return uploadFile.call( sender, evt );
                    };

                    element.filebrowser.url = url;
                    element.hidden = false;
                    setupFileElement( editor, definition.getContents( element[ 'for' ][ 0 ] ).get( element[ 'for' ][ 1 ] ), element.filebrowser );
                }
            }
        }
    }

    // Updates the target element with the url of uploaded/selected file.
    //
    // @param {String}
    //            url The url of a file.
    function updateTargetElement( url, sourceElement ) {
        var dialog = sourceElement.getDialog();
        var targetElement = sourceElement.filebrowser.target || null;

        // If there is a reference to targetElement, update it.
        if ( targetElement ) {
            var target = targetElement.split( ':' );
            var element = dialog.getContentElement( target[ 0 ], target[ 1 ] );
            if ( element ) {
                element.setValue( url );
                dialog.selectPage( target[ 0 ] );
            }
        }
    }

    // Returns true if filebrowser is configured in one of the elements.
    //
    // @param {CKEDITOR.dialog.definitionObject}
    //            definition Dialog definition.
    // @param String
    //            tabId The tab id where element(s) can be found.
    // @param String
    //            elementId The element id (or ids, separated with a semicolon) to check.
    function isConfigured( definition, tabId, elementId ) {
        if ( elementId.indexOf( ";" ) !== -1 ) {
            var ids = elementId.split( ";" );
            for ( var i = 0; i < ids.length; i++ ) {
                if ( isConfigured( definition, tabId, ids[ i ] ) )
                    return true;
            }
            return false;
        }

        var elementFileBrowser = definition.getContents( tabId ).get( elementId ).filebrowser;
        return ( elementFileBrowser && elementFileBrowser.url );
    }

    function setUrl( fileUrl, data, fileBrowserSe) {
        var dialog = fileBrowserSe.getDialog(),
            targetInput = fileBrowserSe[ 'for' ],
            onSelect = fileBrowserSe.onSelect;


        if ( targetInput )
            dialog.getContentElement( targetInput[ 0 ], targetInput[ 1 ] ).reset();

        if ( typeof data == 'function' && data.call(fileBrowserSe) === false )
            return;

        if ( onSelect && onSelect.call(fileBrowserSe, fileUrl.data, data ) === false )
            return;

        // The "data" argument may be used to pass the error message to the editor.
        if ( typeof data == 'string' && data )
            alert( data );

        if ( fileUrl )
            updateTargetElement( fileUrl, fileBrowserSe);
    }

    CKEDITOR.plugins.add( 'magnoliaFileBrowser', {
        requires: 'popup',
        init: function( editor, pluginPath ) {
            editor._.filebrowserFn = CKEDITOR.tools.addFunction( setUrl, editor );
            editor.on( 'destroy', function() {
                CKEDITOR.tools.removeFunction( this._.filebrowserFn );
            });

            editor.on( 'assetChosen', function(event) {
                var data = eval('('+event.data+')');
                setUrl(data.url, data.errorMessage, this._.filebrowserSe);
            });
        }
    });

    CKEDITOR.on( 'dialogDefinition', function( evt ) {
        var definition = evt.data.definition,
            element;
        // Associate filebrowser to elements with 'filebrowser' attribute.
        for ( var i = 0; i < definition.contents.length; ++i ) {
            if ( ( element = definition.contents[ i ] ) ) {
                attachFileBrowser( evt.editor, evt.data.name, definition, element.elements );
                if ( element.hidden && element.filebrowser ) {
                    element.hidden = !isConfigured( definition, element[ 'id' ], element.filebrowser );
                }
            }
        }
    });

})();


