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
package info.magnolia.ui.widget.editor.gwt.client;



import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.web.bindery.event.shared.EventBus;
import info.magnolia.ui.widget.editor.gwt.client.event.EditComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.NewComponentEvent;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 *
 */
public class VPageEditorViewImpl extends FlowPanel implements VPageEditorView, VPageEditorView.Presenter {


    private Presenter presenter;
    private EventBus eventBus;


    public VPageEditorViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.setPresenter(this);

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void onNewComponent(String workspace, String path, String nodeType) {
        eventBus.fireEvent(new NewComponentEvent(workspace, path, nodeType));
    }

    @Override
    public void onEditComponent(String dialog, String workspace, String path) {
        eventBus.fireEvent(new EditComponentEvent(workspace, path, dialog));

    }

    @Override
    public void onDeleteComponent(String path) {
        //eventBus.fireEvent(deleteEvent);
        Window.alert("Hi, one fine day I will be able to delete component at path [" + path +"]");
    }

    @Override
    public void onMoveComponent() {
        // TODO decide how to handle move action.
        Window.alert("Move me here, move me there!");
    }

}