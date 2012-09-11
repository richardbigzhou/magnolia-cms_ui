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
package info.magnolia.ui.widget.editor.gwt.client.jsni;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Frame;
import info.magnolia.ui.widget.editor.gwt.client.VPageEditorView;

/**
 * NativeDomHandler.
 */
abstract public class NativeDomHandler {

    /**
     * Takes care of the mouse up and touchend events for selecting elements inside the page editor.
     * Unfortunately the GWT handlers do not work, so using jsni.
     * @param element
     * @param listener
     */
    public native void initNativeSelectionListener(Element element, VPageEditorView.Listener listener) /*-{
        if (element != 'undefined') {
            var ref = this;
            var that = listener;
            element.contentDocument.onmouseup = function(event) {
                that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditor::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);

            }
            element.contentDocument.ontouchend = function(event) {
                var isTouchScrolling = ref.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorView::isTouchScrolling()();
                if (!isTouchScrolling) {
                    that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditor::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);
                    ref.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorView::resetScrollTop()();
                }
            }
        }
    }-*/;

    public native void reloadIFrame(Element iframeElement) /*-{
        iframeElement.contentWindow.location.reload(true);
    }-*/;

    public abstract void registerLoadHandler(Frame frame, VPageEditorView.Listener handler);

    public abstract void notifyUrlChange();

    private native void onPageEditorReady() /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
            for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply();
            }
        }
    }-*/;
}
