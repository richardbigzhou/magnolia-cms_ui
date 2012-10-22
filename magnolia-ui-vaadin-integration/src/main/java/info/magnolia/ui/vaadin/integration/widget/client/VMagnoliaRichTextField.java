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
package info.magnolia.ui.vaadin.integration.widget.client;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Magnolia rich text field adds an ability to custom plugins to communicate
 * with the server. This was not possible with the add-on out of the box.
 */
public class VMagnoliaRichTextField extends VCKEditorTextField {

    private static final String EVENT_DEMO = "demoevent";
    public static final String VAR_EXTERNAL_LINK = "externalLink";

    public VMagnoliaRichTextField() {
        super();
        loadCKEditor();
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);      
    }

    /**
     * Initializes CKEditor if not done already and add event listeners.
     */
    private void loadCKEditor() {
        if(!CKEditorService.libraryReady()) {
            CKEditorService.loadLibrary(new ScheduledCommand() {
                @Override
                public void execute() {
                    registerEventHandlers();
                }
            });
        } else {
            registerEventHandlers();
        }
    }

    private void onEvent(String eventName) {
        if(eventName.equals(EVENT_DEMO)) {
            onSave();
            clientToServer.updateVariable(paintableId, VAR_EXTERNAL_LINK, "", true);
        }
    }
    
    private String getPaintableId() {
        return paintableId;
    }
    
    private void registerEventHandlers() {
        register(EVENT_DEMO, this);
    }

    /*
     * This method hides a hack. Base class owns privately an instance of 
     * CKEditor editor field. Editor object needs to be accessed or else it
     * would not be possible to handle events from custom made CKEditor
     * plugins. As a workaround this method adds a listener for creation of
     * editor objects and if an editor is created inside DIV element with id
     * matching paintableId member of this class, then this editor is the one 
     * from the base class => we got access to the private member we need.
     */
    private static native void register(final String eventName, final VMagnoliaRichTextField listener)
    /*-{
        var eventHandler = function(ev) {
            ev.listenerData.@info.magnolia.ui.vaadin.integration.widget.client.VMagnoliaRichTextField::onEvent(Ljava/lang/String;)(eventName);
        };
        
        var createdEvent = function(e) {
            var listenerInstanceId = listener.@info.magnolia.ui.vaadin.integration.widget.client.VMagnoliaRichTextField::getPaintableId()();
            var editorInstanceId = e.editor.element.getId();
            if(listenerInstanceId == editorInstanceId) {
                e.editor.on(eventName, eventHandler, null, listener);
            }
        };

        $wnd.CKEDITOR.on('instanceCreated', createdEvent);
     }-*/;
}
