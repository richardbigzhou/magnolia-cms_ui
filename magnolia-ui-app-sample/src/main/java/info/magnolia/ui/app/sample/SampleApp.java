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
package info.magnolia.ui.app.sample;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.app.sample.editor.SampleEditorSubApp;
import info.magnolia.ui.app.sample.main.SampleMainSubApp;
import info.magnolia.ui.framework.app.AbstractApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

/**
 * Sample app.
 */
public class SampleApp extends AbstractApp {

    private AppContext context;
    private ComponentProvider componentProvider;
    private SampleMainSubApp mainSubApp;

    @Inject
    public SampleApp(AppContext context, ComponentProvider componentProvider, SampleMainSubApp mainSubApp) {
        this.context = context;
        this.componentProvider = componentProvider;
        this.mainSubApp = mainSubApp;
    }

    @Override
    public SubApp start(Location location) {
        return mainSubApp;
    }

    @Override
    public void stop() {
    }

    @Override
    public void locationChanged(Location location) {

        DefaultLocation l = (DefaultLocation) location;

        String token = l.getToken();
        if (StringUtils.isNotBlank(token)) {
            openNewEditor(token);
        }
    }

    private void openNewEditor(String name) {
        SampleEditorSubApp editorSubApp = componentProvider.getComponent(SampleEditorSubApp.class);
        editorSubApp.setName(name);
        context.openSubApp(editorSubApp);
        context.setAppLocation(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "sample", name));
    }
}
