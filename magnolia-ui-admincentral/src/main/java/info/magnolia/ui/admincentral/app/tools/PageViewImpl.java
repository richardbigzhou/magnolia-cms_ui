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
package info.magnolia.ui.admincentral.app.tools;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.shell.Shell;

import javax.inject.Inject;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * View implementation for a page app.
 */
public class PageViewImpl implements PageView {

    private final CssLayout layout = new CssLayout();

    @Inject
    public PageViewImpl(final Shell shell, final AppContext appContext) {

        layout.setSizeFull();

        final String sourceURL = ((PageSubAppDescriptor) appContext.getAppDescriptor().getSubApps().get("main")).getUrl();
        final BrowserFrame page = new BrowserFrame("", new ExternalResource(MgnlContext.getContextPath() + "/.magnolia/pages/" + sourceURL));
        page.setSizeFull();

        layout.addComponent(page);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

}
