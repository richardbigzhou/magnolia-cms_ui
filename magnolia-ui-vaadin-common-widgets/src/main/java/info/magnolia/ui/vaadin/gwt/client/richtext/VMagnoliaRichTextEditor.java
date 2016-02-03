/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.richtext;

import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditor;

/**
 * Add support for handling custom plugins to CKEditor instance.
 */
public class VMagnoliaRichTextEditor extends CKEditor {
    protected VMagnoliaRichTextEditor() {
    }

    public final native void addListener(final Listener listener, final String eventName) /*-{
                                                                                          this.on( eventName, function( ev ) {
                                                                                          ev.listenerData.@info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextEditor.Listener::onPluginEvent(Ljava/lang/String;Ljava/lang/String;)(eventName, ev.data);
                                                                                          }, null, listener);
                                                                                          }-*/;

    public final native void fire(final String eventName, final String value) /*-{
                                                                              this.fire(eventName, value);
                                                                              }-*/;

    /**
     * Listener interface for plugin events.
     */
    public interface Listener {
        void onPluginEvent(String eventName, String data);
    }
}
