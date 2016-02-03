/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.mediaeditor;

import info.magnolia.ui.mediaeditor.field.MediaField;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.dialog.BaseDialog;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Skeleton implementation of the media editor UI. Contains an actionbar and a dialog laid out horizontally.
 */
public class MediaEditorViewImpl extends CustomComponent implements MediaEditorView {
    
    private BaseDialog dialog;
    
    private ActionbarView actionbar;
    
    private HorizontalLayout root = new HorizontalLayout();
    
    public MediaEditorViewImpl() {
        addStyleName("v-media-editor");
        setCompositionRoot(root);
        setDialog(new BaseDialog());
        setSizeFull();
        root.setSizeFull();
        dialog.setSizeFull();
        root.setSpacing(true);
    }
    
    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setDialog(BaseDialog dialog) {
        this.dialog = dialog;
        root.addComponentAsFirst(dialog);
        root.setExpandRatio(dialog, 1f);
    }

    @Override
    public void setActionBar(ActionbarView actionbar) {
        this.actionbar = actionbar;
        root.addComponent(actionbar.asVaadinComponent());
    }

    @Override
    public BaseDialog getDialog() {
        return dialog;
    }

    @Override
    public ActionbarView getActionbar() {
        return actionbar;
    }

    @Override
    public void clearActions() {
        dialog.removeAllActions();
    }

    @Override
    public void setMediaContent(MediaField mediaField) {
        getDialog().setContent(mediaField);
    }

    @Override
    public void setToolbar(Component controls) {
        dialog.setFooterToolbar(controls);
    }

}
