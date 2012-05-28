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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceTokenizer;
import info.magnolia.ui.framework.place.Prefix;

/**
 * Place for pulse.
 *
 * @version $Id$
 */
@Prefix("pulse")
public class PulsePlace extends Place {

    /**
     * Tokenizer for PulsePlace.
     *
     * @version $Id$
     */
    public static class Tokenizer implements PlaceTokenizer<PulsePlace> {

        @Override
        public PulsePlace getPlace(String token) {
            final String[] subTokens = token.split("/");
            return new PulsePlace(subTokens.length > 0 ? subTokens[0] : "");
        }

        @Override
        public String getToken(PulsePlace place) {
            return place.getCurrentPulseTab();
        }
    }

    private String currentPulseTab;

    public PulsePlace(String currentTab) {
        this.currentPulseTab = currentTab;
    }

    public String getCurrentPulseTab() {
        return currentPulseTab;
    }
    
    public void setCurrentPulseTab(String currentPulseTab) {
        this.currentPulseTab = currentPulseTab;
    }
}
