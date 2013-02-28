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
import info.magnolia.ui.contentapp.ContentPresenter;
import info.magnolia.ui.contentapp.workbench.ContentWorkbenchView;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.ChooseDialogEventBusConfigurer;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.ContentViewBuilder;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import com.rits.cloning.Cloner;

/**
 * ChooseDialog ContentPresenter.
 * Used to inject a specific EventBus, and to handle specific ChooseDialog logic.
 */
public class ChooseDialogContentPresenter extends ContentPresenter {

    @Inject
    public ChooseDialogContentPresenter(ContentViewBuilder contentViewBuilder, AppContext context, @Named(ChooseDialogEventBusConfigurer.EVENT_BUS_NAME) EventBus subAppEventBus, Shell shell) {
        super(context, contentViewBuilder, subAppEventBus, shell);
        workbenchDefinition = new Cloner().deepClone(workbenchDefinition);
        ((ConfiguredWorkbenchDefinition) workbenchDefinition).setDialogWorkbench(true);
    }

    /**
     * Return the Root path.
     */
    public String getRootPath() {
        return StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/");
    }

    @Override
    public void initContentView(ContentWorkbenchView parentView) {
        super.initContentView(parentView);
        parentView.setViewType(ContentView.ViewType.TREE);
    }
}