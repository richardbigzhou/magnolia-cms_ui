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
package info.magnolia.ui.app.pages.preview;

import javax.inject.Inject;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.view.View;

/**
 * SubApp that displays the page preview.
 */
public class PagePreviewSubApp implements SubApp, PagePreviewView.Listener {

    private PagePreviewView view;
    
    private ActionbarPresenter actionBarPresenter;
    
    LocationController locationController;
    
    @Inject
    public PagePreviewSubApp(final PagePreviewView view, ActionbarPresenter actionbarPresenter, LocationController locationController) {
        this.view = view;
        this.locationController = locationController;
        this.actionBarPresenter = actionbarPresenter;
    }
    
    @Override
    public String getCaption() {
        return null;
    }

    @Override
    public View start() {
        view.setListener(this);   
        return view;
    }

    public void setUrl(String url) {
        view.setUrl(url);
    }

    @Override
    public void closePreview() {
        locationController.goTo(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", ""));
    }

}
