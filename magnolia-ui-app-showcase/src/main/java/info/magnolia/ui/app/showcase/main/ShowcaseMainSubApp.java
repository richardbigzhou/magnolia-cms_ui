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
package info.magnolia.ui.app.showcase.main;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

/**
 * SubApp for the main tab in showcase app.
 */
@Singleton
public class ShowcaseMainSubApp extends AbstractSubApp implements
        ShowcaseMainView.Listener {

    private ShowcaseMainView view;
    private FormsPresenter formPresenter;
    private VaadinPresenter vaadinPresenter;
    private UnsupportedPresenter unsupportedPresenter;

    @Inject
    public ShowcaseMainSubApp(final AppContext appContext, @Named("app") EventBus subAppEventBus, ShowcaseMainView view,
            FormsPresenter formsPresenter, VaadinPresenter vaadinPresenter,
            UnsupportedPresenter unsupportedPresenter) {
        super(appContext, view);
        this.view = view;
        this.formPresenter = formsPresenter;
        this.vaadinPresenter = vaadinPresenter;
        this.unsupportedPresenter = unsupportedPresenter;
    }

    @Override
    public View start(Location location) {
        view.setListener(this);
        view.setFormFieldView(formPresenter.start());
        view.setVaadinView(vaadinPresenter.start());
        view.setUnsupportedVaadinView(unsupportedPresenter.start());
        return view;
    }

    @Override
    public void locationChanged(Location location) {

    }

    @Override
    public String getCaption() {
        return "showcase";
    }
}
