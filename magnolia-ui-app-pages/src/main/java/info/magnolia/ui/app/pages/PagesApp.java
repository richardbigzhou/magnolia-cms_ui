/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.pages;


import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.workbench.ContentWorkbench;
import info.magnolia.ui.framework.app.AbstractApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pages app.
 *
 * @version $Id$
 */
public class PagesApp extends AbstractApp implements PagesView.Presenter {


    private enum PagesTab {


        WORKBENCH, PAGEEDITOR;

        private static final String PAGEEDITOR_TOKEN = "pageeditor";

        public static PagesTab getDefault() {
            return WORKBENCH;
        }

        public static PagesTab resolveTab(String tabName) {
            if (tabName.equals(PAGEEDITOR_TOKEN)) {
                return PAGEEDITOR;
            }
            return getDefault();
        }
    }
    private AppContext context;
    private ComponentProvider componentProvider;
    private PagesView view;
    private Location currentLocation;

    @Inject
    public PagesApp(PagesView view, AppContext context, ComponentProvider componentProvider, ContentWorkbench workbench) {
        this.view = view;
        this.context = context;
        this.componentProvider = componentProvider;
        workbench.initWorkbench("website");
        view.initView(workbench.asVaadinComponent());
    }


    @Override
    public AppView start(Location location) {
        view.setPresenter(this);
        return view;
    }

    @Override
    public void locationChanged(Location location) {
        DefaultLocation pagesLocation = (DefaultLocation) location;

       List<String> pathParams = parsePathParamsFromToken(pagesLocation.getToken());
       PagesAppPresenter tabPresenter = this;

        if (pathParams.size() > 0) {
            final String tabName = pathParams.remove(0);
            PagesTab pagesTab = PagesTab.resolveTab(tabName);
            switch (pagesTab) {
                case PAGEEDITOR:
                    Object[] parameters = new Object[]{pathParams};
                    tabPresenter = componentProvider.newInstance(PageEditorPresenter.class, parameters);
                    context.openAppView(tabPresenter.getView());
                    context.setAppLocation(location);
                    break;
                case WORKBENCH: default:
                    break;

            }
        }
        currentLocation = location;

//        pulsePlace.setCurrentPulseTab(displayedTabId);
    }

    private List<String> parsePathParamsFromToken(String token) {
        final List<String> result = new ArrayList<String>(Arrays.asList(token.split(":")));
        return result;
    }

    @Override
    public AppView getView() {
        return view;
    }
}
