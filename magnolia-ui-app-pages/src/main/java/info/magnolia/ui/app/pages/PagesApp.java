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


import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.app.pages.editor.PageEditorParameters;
import info.magnolia.ui.app.pages.editor.PagesEditorSubApp;
import info.magnolia.ui.app.pages.main.PagesMainSubApp;
import info.magnolia.ui.framework.app.AbstractApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pages app.
 */
public class PagesApp extends AbstractApp {


    private static final String PAGEEDITOR_TOKEN = "pageeditor";

    private AppContext context;
    private ComponentProvider componentProvider;
    private PagesMainSubApp mainSubApp;

    @Inject
    public PagesApp(AppContext context, ComponentProvider componentProvider, PagesMainSubApp mainSubApp) {
        this.context = context;
        this.componentProvider = componentProvider;
        this.mainSubApp = mainSubApp;
    }

    @Override
    public void locationChanged(Location location) {

        DefaultLocation l = (DefaultLocation) location;

        List<String> pathParams = parsePathParamsFromToken(l.getToken());

        final String subAppName = pathParams.remove(0);

        if (subAppName.equals(PAGEEDITOR_TOKEN)) {
            String contextPath = MgnlContext.getContextPath();

            PagesEditorSubApp editorSubApp = componentProvider.newInstance(PagesEditorSubApp.class);
            PageEditorParameters parameters = new PageEditorParameters(contextPath, pathParams.get(0));
            editorSubApp.setParameters(parameters);
            context.openSubApp(editorSubApp);
            context.setAppLocation(location);
        }

    }

    private List<String> parsePathParamsFromToken(String token) {
        final List<String> result = new ArrayList<String>(Arrays.asList(token.split(":")));
        return result;
    }

    @Override
    public SubApp start(Location location) {
        return mainSubApp;
    }

    @Override
    public void stop() {
    }

}
