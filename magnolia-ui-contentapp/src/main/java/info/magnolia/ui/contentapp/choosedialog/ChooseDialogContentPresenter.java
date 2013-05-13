/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.event.EventBus;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.event.ChooseDialogEventBus;
import info.magnolia.ui.workbench.AbstractContentPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import javax.inject.Inject;
import javax.inject.Named;

import com.rits.cloning.Cloner;
import com.vaadin.data.Container;

/**
 * ChooseDialog ContentPresenter.
 * Used to inject a specific EventBus, and to handle specific ChooseDialog logic.
 */
public class ChooseDialogContentPresenter extends AbstractContentPresenter {

    private AppContext appContext;

    private EventBus chooseDialogEventBus;

    @Inject
    public ChooseDialogContentPresenter(@Named(ChooseDialogEventBus.NAME) EventBus chooseDialogEventBus, AppContext appContext) {
        this.appContext = appContext;
        this.chooseDialogEventBus = chooseDialogEventBus;
    }

    public void startChooseDialog(WorkbenchView workbenchView) {
        SubAppDescriptor subAppContext = appContext.getDefaultSubAppDescriptor();
        if (subAppContext instanceof BrowserSubAppDescriptor) {
            BrowserSubAppDescriptor bsd = (BrowserSubAppDescriptor) subAppContext;
            WorkbenchDefinition clone = new Cloner().deepClone(bsd.getWorkbench());
            super.start(clone, chooseDialogEventBus);
        }
    }

    public String getRootPath() {
        return "/";
        // return StringUtils.defaultIfEmpty(getWorkbenchDefinition().getPath(), "/");
    }

    protected void initContentView(WorkbenchView view) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public Container getContainer() {
        return null;
    }
}