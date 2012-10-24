/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;

import com.google.gwt.user.client.ui.Widget;


/**
 * Viewports might have different ways of displaying the content. This interface helps to define
 * them from outside.
 */
interface TransitionDelegate {

    Callbacks setActive(VShellViewport viewport, boolean active);

    Callbacks setVisibleApp(VShellViewport viewport, Widget app);

    boolean bypass();

    final static TransitionDelegate SHELL_APPS_TRANSITION_DELEGATE = new ShellAppsTransitionDelegate();

    final static TransitionDelegate APPS_TRANSITION_DELEGATE = new AppsTransitionDelegate();

    /**
     * The Class BaseTransitionDelegate with the bypass mechanism.
     */
    static abstract class BaseTransitionDelegate implements TransitionDelegate {

        protected boolean bypass;

        @Override
        public Callbacks setActive(VShellViewport viewport, boolean active) {
            bypass = true;
            viewport.setActive(active);
            return null;
        }

        @Override
        public Callbacks setVisibleApp(VShellViewport viewport, Widget app) {
            bypass = true;
            viewport.setVisibleApp(app);
            return null;
        }

        @Override
        public boolean bypass() {
            return bypass && !(bypass = false);
        }
    };

}
