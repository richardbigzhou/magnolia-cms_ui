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
import info.magnolia.objectfactory.Components;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatus;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatusView;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatusViewImpl;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.view.View;
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
    private ActivationStatusViewImpl view;

    @Before
    public void setUp() throws Exception {
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        this.serverConfiguration = mock(ServerConfiguration.class);
        this.contentConnector = mock(ContentConnector.class);
        this.admincentralEventBus = mock(EventBus.class);
        this.subAppEventBus = mock(EventBus.class);

        this.session = new MockSession(RepositoryConstants.WEBSITE);

        MockContext systemContext = new MockContext();
        systemContext.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(systemContext);

        this.view = mock(ActivationStatusViewImpl.class);
        ComponentsTestUtil.setInstance(ActivationStatusView.class, view);

        this.activationStatus = new ActivationStatus(i18n, serverConfiguration, contentConnector, admincentralEventBus, subAppEventBus, Components.getComponentProvider());
    }

    @Test
    public void testHandlerRegistrationOnAuthorInstance() throws Exception {
        // GIVEN
        DetailLocation location = mock(DetailLocation.class);

        when(serverConfiguration.isAdmin()).thenReturn(true);
        when(location.getNodePath()).thenReturn("/path/to/node");

        // WHEN
        View view = activationStatus.start(location);

        // THEN
        verify(subAppEventBus, times(1)).addHandler(eq(NodeSelectedEvent.class), any(NodeSelectedEvent.Handler.class));
        verify(subAppEventBus, times(1)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));
        verify(admincentralEventBus, times(1)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));

        assertNotNull("The view should have been inititalized when on author instance.", view);
    }

    @Test
    public void testHandlerRegistrationOnPublicInstance() throws Exception {
        // GIVEN
        when(serverConfiguration.isAdmin()).thenReturn(false);

        // WHEN
        View view = activationStatus.start(mock(DetailLocation.class));

        // THEN
        verify(subAppEventBus, times(0)).addHandler(eq(NodeSelectedEvent.class), any(NodeSelectedEvent.Handler.class));
        verify(subAppEventBus, times(0)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));
        verify(admincentralEventBus, times(0)).addHandler(eq(ContentChangedEvent.class), any(ContentChangedEvent.Handler.class));

        assertNull("The view should not be initialized on Public instance.", view);
    }

    @Test
    public void test() throws Exception {
        // GIVEN
        DetailLocation location = mock(DetailLocation.class);

        when(serverConfiguration.isAdmin()).thenReturn(true);
        when(location.getNodePath()).thenReturn("/path/to/node");
        NodeUtil.createPath(session.getRootNode(), "/path/to/node", NodeTypes.Page.NAME);

        // WHEN
        activationStatus.start(location);

        // THEN
        verify(view, times(1)).setIconStyle(anyString());
        verify(view, times(1)).setActivationStatus(anyString());
    }
}
