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
package info.magnolia.m5admincentral.shellapp.pulse;

import info.magnolia.m5admincentral.framework.PlaceStateHandler;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;

import javax.inject.Inject;

/**
 * Activity for pulse.
 *
 * @version $Id$
 */
public class PulseActivity extends AbstractActivity implements PulseView.Presenter {
    
    private PulseView pulseView;

    private Shell shell;
   
    @PlaceStateHandler
    public void updateTab(final PulsePlace place) {
        final String displayedTabId = pulseView.setCurrentPulseTab(place.getCurrentPulseTab());
        place.setCurrentPulseTab(displayedTabId);
    }
    
    @Inject
    public PulseActivity(PulseView pulseView, final Shell shell) {
        this.pulseView = pulseView;
        this.shell = shell;
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        pulseView.setPresenter(this);
        shell.showNotification("Something weird goes on....But you can skip it for now");
        viewPort.setView(pulseView);
    }

    @Override
    public void onPulseTabChanged(String tabId) {
        final String currentFragment = shell.getFragment();
        int index = currentFragment.lastIndexOf(":");
        final String newFragment = currentFragment.substring(0, index + 1) + tabId;
        shell.setFragment(newFragment);
    }
}
