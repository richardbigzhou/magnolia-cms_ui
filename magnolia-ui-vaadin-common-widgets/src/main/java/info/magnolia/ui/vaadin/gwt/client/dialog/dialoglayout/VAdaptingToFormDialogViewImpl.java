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
package info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout;

import info.magnolia.ui.vaadin.gwt.client.form.VForm;
import info.magnolia.ui.vaadin.gwt.client.form.VFormView;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implements {@link VBaseDialogView} by delegating to embedded form.
 */
public class VAdaptingToFormDialogViewImpl extends SimplePanel implements VBaseDialogView {

    private VForm form;
    
    private Presenter presenter;
    
    public VAdaptingToFormDialogViewImpl() {
        setStyleName("dialog-panel");
    }
    
    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void addAction(String name, String label) {
        form.getView().addAction(name, label);
    }

    @Override
    public void setDescription(String description) {
        form.getView().setDescription(description);
    }

    @Override
    public void setCaption(String caption) {
        form.getView().setCaption(caption);
    }

    @Override
    public int getContentWidth() {
        return form.getView().getFormWidth();
    }

    @Override
    public int getContentHeight() {
        return form.getView().getFormHeight();
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (contentWidget instanceof VForm) {
            this.form = ((VForm)contentWidget);
            this.form.getView().setPresenter(new VFormView.Presenter() {
                @Override
                public void fireAction(String action) {
                    presenter.fireAction(action);
                }
                
                @Override
                public void runLayout() {
                    
                }
            });
            setWidget(contentWidget);
        }
    }

    @Override
    public Widget getContent() {
        return getWidget();
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
        if (form != null) {
            form.getView().setPresenter(new VFormView.Presenter() {
                @Override
                public void fireAction(String action) {
                    presenter.fireAction(action);
                }
                
                @Override
                public void runLayout() {
                    form.runLayout();
                }
            });            
        }
    }

    @Override
    public void setActionLabel(String actionName, String label) {
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

}
