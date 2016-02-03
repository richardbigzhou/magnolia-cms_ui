/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.workbench.column.definition;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.exchange.ActivationManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class OnAuthorOrWhenThereIsSubscribersRuleTest {

    private ColumnAvailabilityRule rule;
    private ServerConfiguration serverConfiguration;
    private ActivationManager activationManager;

    @Before
    public void setUp() throws Exception {
        serverConfiguration = mock(ServerConfiguration.class);
        activationManager = mock(ActivationManager.class);
        rule = new OnAuthorOrWhenThereIsSubscribersRule(serverConfiguration, activationManager);
    }

    @Test
    public void testIsAvailableReturnsTrueIfOnAuthor() {
        // GIVEN
        when(serverConfiguration.isAdmin()).thenReturn(true);

        // WHEN
        boolean result = rule.isAvailable();

        // THEN
        assertTrue(result);
    }

    @Test
    public void testIsAvailableReturnsTrueOnNonAuthorWithActiveSubscribers() {
        // GIVEN
        when(serverConfiguration.isAdmin()).thenReturn(false);
        when(activationManager.hasAnyActiveSubscriber()).thenReturn(true);

        // WHEN
        boolean result = rule.isAvailable();

        // THEN
        assertTrue(result);
    }

    @Test
    public void testIsAvailableReturnsFalseOnNonAuthorAndNoSubscribers() {
        // GIVEN
        when(serverConfiguration.isAdmin()).thenReturn(false);
        when(activationManager.hasAnyActiveSubscriber()).thenReturn(false);

        // WHEN
        boolean result = rule.isAvailable();

        // THEN
        assertFalse(result);
    }
}
