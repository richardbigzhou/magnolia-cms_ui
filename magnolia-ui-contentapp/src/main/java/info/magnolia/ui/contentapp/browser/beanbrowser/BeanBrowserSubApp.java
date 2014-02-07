/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.contentapp.browser.beanbrowser;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.contentapp.browser.BrowserSubAppBase;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 04/02/14
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class BeanBrowserSubApp extends BrowserSubAppBase {

    @Inject
    public BeanBrowserSubApp(ActionExecutor actionExecutor, SubAppContext subAppContext, ContentSubAppView view, BeanBrowserPresenter browser, @Named(SubAppEventBus.NAME) EventBus subAppEventBus, ComponentProvider componentProvider) {
        super(actionExecutor, subAppContext, view, browser, subAppEventBus, componentProvider);
    }

    @Override
    protected Object ensureSelection(String urlFragmentPath, WorkbenchDefinition workbench) {
        return getBrowser().getWorkbenchPresenter().getItemFor(new File("/Users/sasha/test"));
    }

    @Override
    protected void applySelectionToLocation(BrowserLocation location, Object selectedId) {

    }

}
