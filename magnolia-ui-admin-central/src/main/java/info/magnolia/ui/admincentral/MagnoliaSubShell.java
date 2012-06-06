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
package info.magnolia.ui.admincentral;

import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.widget.magnoliashell.BaseMagnoliaShell;


/**
 *
 * MagnoliaSubShell.
 * @version $Id$
 *
 */
@SuppressWarnings("serial")
public class MagnoliaSubShell extends BaseMagnoliaShell implements Shell {

    private MagnoliaShell parent;
    private String id;

    public MagnoliaSubShell(String id, MagnoliaShell parent) {
        this.id = id;
        this.parent = parent;
    }

    public void askForConfirmation(String message, ConfirmationHandler listener) {
        parent.askForConfirmation(message, listener);
    }

    public void showNotification(String message) {
        parent.showNotification(message);
    }

    public void showError(String message, Exception e) {
        parent.showError(message, e);
    }

    public String getFragment() {
        return parent.getFragment();
    }
    public void openWindow(String uri, String windowName) {
        parent.openWindow(uri, windowName);
    }

    @Override
    public void setFragment(String fragment) {
        parent.setFragment(fragment);

    }
    @Override
    public HandlerRegistration addFragmentChangedHandler(FragmentChangedHandler handler) {
        return parent.addFragmentChangedHandler(handler);
    }

    @Override
    public Shell createSubShell(String id) {
       throw new UnsupportedOperationException("A MagnoliaSubShell cannot create a sub shell.");
    }
}