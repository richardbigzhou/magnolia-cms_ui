/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.mediaeditor.action.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link IsNotUserAgentRule}.
 */
public class IsNotUserAgentRuleTest {

    private IsNotUserAgentRuleDefinition isNotUserAgentRuleDefinition;
    private IsNotUserAgentRule isNotUserAgentRule;

    private WebContext webContext;
    private HttpServletRequest request;

    private Map<String, String> disabledUserAgents = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        isNotUserAgentRuleDefinition = new IsNotUserAgentRuleDefinition();
        isNotUserAgentRule = new IsNotUserAgentRule(isNotUserAgentRuleDefinition);
        isNotUserAgentRuleDefinition.setUserAgents(disabledUserAgents);

        webContext = mock(WebContext.class);
        request = mock(HttpServletRequest.class);
        when(webContext.getRequest()).thenReturn(request);
        MgnlContext.setInstance(webContext);
    }

    @Test
    public void testDisableActionIfUserAgentContains() {
        // GIVEN
        disabledUserAgents.put("Safari", "Safari");

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/600.1.17 (KHTML, like Gecko) Version/7.1 Safari/537.85.10");

        // WHEN
        boolean isAvailable = isNotUserAgentRule.isAvailable();

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testDisableActionByRegex() {
        // GIVEN
        disabledUserAgents.put("IE11", ".*Trident/.*rv:([0-9]{1,}[\\.0-9]{0,}).*");

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko");

        // WHEN
        boolean isAvailable = isNotUserAgentRule.isAvailable();

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testDoNotDisableActionWhenUserAgentIsNotForbidden() {
        // GIVEN
        disabledUserAgents.put("IE11", ".*Trident/.*rv:([0-9]{1,}[\\.0-9]{0,}).*");

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:40.0) Gecko/20100101 Firefox/40.0");

        // WHEN
        boolean isAvailable = isNotUserAgentRule.isAvailable();

        // THEN
        assertTrue(isAvailable);
    }
}
