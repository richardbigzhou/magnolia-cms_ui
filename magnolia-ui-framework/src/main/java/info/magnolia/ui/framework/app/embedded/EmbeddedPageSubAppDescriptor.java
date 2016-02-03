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

import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;

/**
 * Allows to specify the url to an html page to be embedded in an iframe.
 * <p>
 * If the supplied url doesn't start with <code>http</code> it will be assumed to be an internal one and thus prepended with the current webapp's context path. E.g. given the url <code>/some-static-pages/some-page</code> an url will be built in the following form <code>&lt;magnolia-webapp-context&gt;/some-static-pages/some-page</code>
 */
public class EmbeddedPageSubAppDescriptor extends ConfiguredSubAppDescriptor {

    private String url;

    public String getUrl() {
        return url;
    }

    /**
     * @param url if not starting with <code>http</code> the url will be assumed to be an internal one and thus prepended with the current webapp's context path.
     * E.g. given the url <code>some-static-pages/some-page</code> an url will be built in the following form <code>&lt;magnolia-webapp-context&gt;/some-static-pages/some-page</code>
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
