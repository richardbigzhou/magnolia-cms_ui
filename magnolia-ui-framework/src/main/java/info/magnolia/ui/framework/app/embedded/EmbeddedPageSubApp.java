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
package info.magnolia.ui.framework.app.embedded;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sub app for the main tab in an embedded page app.
 */
public class EmbeddedPageSubApp extends BaseSubApp<EmbeddedPageView> {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedPageSubApp.class);
    private Location lastLocation;

    @Inject
    public EmbeddedPageSubApp(SubAppContext subAppContext, EmbeddedPageView pageView) {
        super(subAppContext, pageView);
    }

    @Override
    public EmbeddedPageView start(Location location) {
        this.lastLocation = location;
        String url = location.getParameter();
        if (StringUtils.isEmpty(url)) {
            EmbeddedPageSubAppDescriptor subAppDescriptor = ((EmbeddedPageSubAppDescriptor) getSubAppContext().getSubAppDescriptor());
            url = subAppDescriptor.getUrl();
        }

        url = updateUrl(url);
        getView().setUrl(url);
        return super.start(location);
    }

    /**
     * Check whether the url has changed, if so update the view to display the new location.
     */
    @Override
    public void locationChanged(Location location) {
        String url = location.getParameter();
        if (StringUtils.isNotBlank(url) && !url.equals(lastLocation.getParameter())) {
            getView().setUrl(updateUrl(url));
        }
        this.lastLocation = location;
        super.locationChanged(location);
    }

    /**
     * Add the context path to internal urls.
     */
    protected String updateUrl(String url) {
        boolean isInternalPage = !url.startsWith("http");

        if(isInternalPage) {
            url = url.startsWith("/") ? MgnlContext.getContextPath() + url : MgnlContext.getContextPath() + "/" + url;
        }
        return url;
    }
}
