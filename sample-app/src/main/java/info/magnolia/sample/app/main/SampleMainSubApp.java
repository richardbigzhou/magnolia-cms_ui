/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.sample.app.main;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.AppEventBus;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * SubApp for the main tab in sample app.
 */
public class SampleMainSubApp extends BaseSubApp<SampleMainView> implements SampleMainView.Listener {

    private SampleMainView sampleMainView;
    private NavigationPresenter navigationPresenter;
    private ContentDisplayPresenter contentDisplayPresenter;

    @Inject
    public SampleMainSubApp(final SubAppContext subAppContext, @Named(AppEventBus.NAME) EventBus appEventBus, SampleMainView sampleMainView, NavigationPresenter navigationPresenter, final ContentDisplayPresenter contentDisplayPresenter) {
        super(subAppContext, sampleMainView);
        this.sampleMainView = sampleMainView;
        this.contentDisplayPresenter = contentDisplayPresenter;
        this.navigationPresenter = navigationPresenter;

        appEventBus.addHandler(ContentItemSelectedEvent.class, new ContentItemSelectedEvent.Handler() {

            @Override
            public void onContentItemSelected(String name) {
                contentDisplayPresenter.setResourceToDisplay(name);
            }
        });
    }

    @Override
    public SampleMainView start(Location location) {

        ContentDisplayView contentDisplayView = contentDisplayPresenter.start();

        NavigationView navigationView = navigationPresenter.start();

        sampleMainView.setListener(this);
        sampleMainView.setLeftView(navigationView);
        sampleMainView.setRightView(contentDisplayView);
        return sampleMainView;
    }
}
