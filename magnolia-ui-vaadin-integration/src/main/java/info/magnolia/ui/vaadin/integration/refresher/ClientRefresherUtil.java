/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.vaadin.integration.refresher;

import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ProgressIndicator;

/**
 * A util class which enables the server to update the client.
 */
public class ClientRefresherUtil {

    /**
     * Effectively, the server will push any updates to its state to the client after the delayMsec elapses.
     * 
     * @param delayMsec When the server should update the client.
     * @param refreshee A component to host the refreshing logic.
     */
    public static ProgressIndicator addClientRefresher(int delayMsec, AbstractComponentContainer refreshee) {

        /*
         * Progressbar hack to cause client to update to server state.
         * Progressbar helps simply because it uses polling to initiate a client/server transaction,
         * this transaction enables the client to refresh itself to the changes we perform in the timer.
         * 
         * When Vaadin 7.1 with built-in push is out this code can be refactored to use it.
         * See ticket MGNLUI-1112
         */
        ProgressIndicator clientRefresher = new ProgressIndicator();
        clientRefresher.setPollingInterval(delayMsec);
        clientRefresher.setIndeterminate(true);
        clientRefresher.setStyleName("progressbar-based-client-refresher");

        refreshee.addComponent(clientRefresher);

        return clientRefresher;
    }


}
