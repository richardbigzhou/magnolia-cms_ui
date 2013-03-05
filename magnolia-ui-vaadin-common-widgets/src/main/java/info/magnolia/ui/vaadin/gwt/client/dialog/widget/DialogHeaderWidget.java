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
package info.magnolia.ui.vaadin.gwt.client.dialog.widget;

import info.magnolia.ui.vaadin.gwt.client.editorlike.widget.EditorLikeHeaderWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * DialogHeaderWidget.
 */
public class DialogHeaderWidget extends EditorLikeHeaderWidget {

    private static final String CLASSNAME_CLOSEBUTTON = "btn-dialog-close";

    /**
     * Callback interface for the EditorLike header.
     */
    public interface VDialogHeaderCallback extends EditorLikeHeaderWidget.VEditorLikeHeaderCallback {

        void onCloseFired();
    }

    protected Button closeButton;

    public DialogHeaderWidget(VDialogHeaderCallback callback) {
        super(callback);
    }

    @Override
    public void construct() {

        closeButton = new Button("", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ((VDialogHeaderCallback) callback).onCloseFired();
            }
        });

        closeButton.setStyleName(CLASSNAME_CLOSEBUTTON);
        closeButton.addStyleName("green");
        add(closeButton, headerPanel);

        super.construct();

    }
}
