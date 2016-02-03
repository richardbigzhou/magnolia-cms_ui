/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.app.stub;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.ui.framework.app.DefaultAppView;

import javax.inject.Inject;

/**
 * Stub {@link info.magnolia.ui.framework.app.BaseApp} extension showed in case an exception
 * was thrown during the original app start-up phase. Displays a minimal view and sends the
 * exception details to the Pulse in a form of a message.
 */
public class FailedAppStub extends BaseApp {

    public static final String MSG_KEY = "ui-framework.app.start.failed";

    private Throwable relatedException;

    private SimpleTranslator i18n;

    @Inject
    public FailedAppStub(final AppContext appContext, Throwable relatedException, SimpleTranslator i18n) {
        super(appContext, new DefaultAppView());
        this.relatedException = relatedException;
        this.i18n = i18n;
    }

    @Override
    public void locationChanged(Location location) {
        // Do nothing
    }

    @Override
    public void start(Location location) {
        final String appName = appContext.getName();
        final String message = i18n.translate(MSG_KEY, appName);

        Message error = new ExceptionMessage(relatedException, message, i18n);
        ((DefaultAppView)getView()).asVaadinComponent().addComponent(new StubView("icon-warning").asVaadinComponent());

        appContext.sendLocalMessage(error);
    }

}

