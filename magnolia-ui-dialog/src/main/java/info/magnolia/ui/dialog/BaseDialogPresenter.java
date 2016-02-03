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
package info.magnolia.ui.dialog;

import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import javax.inject.Inject;

import com.vaadin.ui.Panel;

/**
 * Base implementation of {@link DialogPresenter}.
 */
public class BaseDialogPresenter implements DialogPresenter {

    private final DialogView view;

    @Inject
    public BaseDialogPresenter(DialogView view) {
        this.view = view;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public void showCloseButton() {
        getBaseDialog().showCloseButton();
    }

    @Override
    public void addDialogCloseHandler(DialogCloseEvent.Handler handler) {
        getBaseDialog().addDialogCloseHandler(handler);
    }


    @Override
    public void addAction(String actionName, String actionLabel, DialogActionListener callback) {
        getBaseDialog().addAction(actionName, actionLabel, callback);

    }

    @Override
    public void addActionCallback(String actionName, DialogActionListener callback) {
        getBaseDialog().addActionCallback(actionName, callback);
    }

    @Override
    public void closeDialog() {
        getBaseDialog().closeSelf();
    }

    /**
     * A convenience method to get hold of the BaseDialog.
     */
    protected BaseDialog getBaseDialog() {
        return (BaseDialog) ((Panel) view.asVaadinComponent()).getContent();
    }

}
