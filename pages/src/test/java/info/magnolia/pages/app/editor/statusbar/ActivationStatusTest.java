/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.statusbar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatus;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatusView;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatusViewImpl;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatus}.
 */
public class ActivationStatusTest {
    private ActivationStatus activationStatus;
    private ServerConfiguration serverConfiguration;
    private ContentConnector contentConnector;
    private EventBus admincentralEventBus;
    private EventBus subAppEventBus;
    private MockSession session;
    private ActivationStatusView view;
    private SimpleTranslator i18n;

    @Before
    public void setUp() throws Exception {
        this.i18n = mock(SimpleTranslator.class);
        this.serverConfiguration = mock(ServerConfiguration.class);
        this.contentConnector = mock(ContentConnector.class);
        this.admincentralEventBus = mock(EventBus.class);
        this.subAppEventBus = mock(EventBus.class);

        this.session = new MockSession(RepositoryConstants.WEBSITE);

        MockContext systemContext = new MockContext();
        systemContext.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(systemContext);

        this.view = new ActivationStatusViewImpl();

        this.activationStatus = new ActivationStatus(view, i18n, serverConfiguration, contentConnector, admincentralEventBus, subAppEventBus);
    }

    @Test
    public void testHandlerRegistrationOnAuthorInstance() throws Exception {
        // GIVEN
        DetailLocation location = mock(DetailLocation.class);

        when(serverConfiguration.isAdmin()).thenReturn(true);
        when(location.getNodePath()).thenReturn("/path/to/node");

        // WHEN
        activationStatus.start();

        // THEN
        verify(subAppEventBus, times(1)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));
        verify(admincentralEventBus, times(1)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));

        assertTrue("The view should be visible on author instance.", view.isVisible());
    }

    @Test
    public void testHandlerRegistrationOnPublicInstance() throws Exception {
        // GIVEN
        when(serverConfiguration.isAdmin()).thenReturn(false);

        // WHEN
        activationStatus.start();

        // THEN
        verify(subAppEventBus, times(0)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));
        verify(admincentralEventBus, times(0)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));

        assertFalse("The view should not be visible on Public instance.", view.isVisible());
    }

    @Test
    public void testUpdateActivationStatusOnLocationChange() throws Exception {
        // GIVEN
        DetailLocation location = mock(DetailLocation.class);
        this.view = mock(ActivationStatusView.class);
        this.activationStatus = new ActivationStatus(view, i18n, serverConfiguration, contentConnector, admincentralEventBus, subAppEventBus);

        when(view.isVisible()).thenReturn(true);
        when(serverConfiguration.isAdmin()).thenReturn(true);
        when(location.getNodePath()).thenReturn("/path/to/node");
        NodeUtil.createPath(session.getRootNode(), "/path/to/node", NodeTypes.Page.NAME);

        // WHEN
        activationStatus.start();
        activationStatus.onLocationUpdate(location);

        // THEN
        verify(view, times(1)).setIconStyle(anyString());
        verify(view, times(1)).setActivationStatus(anyString());
    }
}
