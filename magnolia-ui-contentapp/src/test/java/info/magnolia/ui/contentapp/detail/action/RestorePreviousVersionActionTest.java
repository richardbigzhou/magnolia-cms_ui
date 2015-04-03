/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.contentapp.detail.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RestorePreviousVersionAction}.
 */
public class RestorePreviousVersionActionTest extends RepositoryTestCase {

    private Node node;

    private RestorePreviousVersionActionDefinition definition;

    private SimpleEventBus locationEventBus;
    private LocationController locationController;
    private SubAppContext subAppContext;
    private EventBus eventBus;
    private SimpleTranslator i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        definition = new RestorePreviousVersionActionDefinition();

        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        node = webSiteSession.getRootNode().addNode("node", NodeTypes.Page.NAME);
        NodeTypes.Created.set(node);
        node.setProperty("property", "property");
        node.addNode("subNode", NodeTypes.Page.NAME);
        node.getNode("subNode").setProperty("property_subNode", "property_subNode");
        webSiteSession.save();

        locationEventBus = new SimpleEventBus();
        locationController = new LocationController(locationEventBus, mock(Shell.class));

        subAppContext = mock(SubAppContext.class);
        eventBus = mock(EventBus.class);
        i18n = mock(SimpleTranslator.class);
    }

    @Test
    public void testExecute() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.addVersion(node);
        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        RestorePreviousVersionAction action = new RestorePreviousVersionAction(definition, item, versionMan, subAppContext, eventBus, i18n);

        // WHEN
        action.execute();

        // THEN
        assertEquals(versionMan.getBaseVersion(node).getName(), "1.0");
    }

    @Test
    public void testExecuteNoVersion() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        RestorePreviousVersionAction action = new RestorePreviousVersionAction(definition, item, versionMan, subAppContext, eventBus, i18n);

        // WHEN
        action.execute();

        // THEN
        try {
            versionMan.getBaseVersion(node);
            fail("Should have failed");
        } catch (RepositoryException re) {
            assertTrue(re.getMessage().contains("Node /node was never versioned"));
        }
    }

    @Test
    public void testExecuteThreeVersion() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.addVersion(node);
        versionMan.addVersion(node);
        versionMan.addVersion(node);

        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        RestorePreviousVersionAction action = new RestorePreviousVersionAction(definition, item, versionMan, subAppContext, eventBus, i18n);

        // WHEN
        action.execute();

        // THEN
        assertEquals(versionMan.getBaseVersion(node).getName(), "1.2");
    }

}
