/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.jcrbrowser.app.contentviews;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcrbrowser.app.SystemPropertiesVisibilityToggledEvent;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ActionEvent;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.TreeView;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Container;

public class JcrBrowserTreePresenterTest {

    private ExposingContainerJcrBrowserTreePresenter jcrBrowserTreePresenter;

    private EventBus eventBus;
    private SubAppContext subAppContext;
    private ComponentProvider componentProvider;
    private JcrContentConnector jcrContentConnector;
    private WorkbenchDefinition workbenchDefinition;

    @Before
    public void setUp() throws Exception {
        this.subAppContext = mock(SubAppContext.class);
        this.eventBus = new SimpleEventBus();
        this.jcrContentConnector = mock(JcrContentConnector.class);
        this.workbenchDefinition = mock(WorkbenchDefinition.class);

        TreePresenterDefinition treePresenterDefinition = new TreePresenterDefinition();
        treePresenterDefinition.setViewType("foo");

        doReturn(Collections.singletonList(treePresenterDefinition)).when(workbenchDefinition).getContentViews();
        doReturn(new ConfiguredJcrContentConnectorDefinition()).when(jcrContentConnector).getContentConnectorDefinition();

        componentProvider = mock(ComponentProvider.class);
        this.jcrBrowserTreePresenter = new ExposingContainerJcrBrowserTreePresenter(mock(TreeView.class), componentProvider, subAppContext, mock(SimpleTranslator.class));
    }

    @Test
    public void containerTogglesSystemPropertiesDisplayUponEvent() throws Exception {
        // WHEN
        jcrBrowserTreePresenter.start(workbenchDefinition, eventBus, "foo", jcrContentConnector);

        // THEN
        assertThat(jcrBrowserTreePresenter.container.isIncludingSystemProperties(), is(false));

        // WHEN
        eventBus.fireEvent(new SystemPropertiesVisibilityToggledEvent(true));

        // THEN
        assertThat(jcrBrowserTreePresenter.container.isIncludingSystemProperties(), is(true));

        // WHEN
        eventBus.fireEvent(new SystemPropertiesVisibilityToggledEvent(false));

        // THEN
        assertThat(jcrBrowserTreePresenter.container.isIncludingSystemProperties(), is(false));
    }

    @Test
    public void bailsOnAttemptToPersistSystemPropertyModification() throws Exception {
        // WHEN
        jcrBrowserTreePresenter.start(workbenchDefinition, eventBus, "foo", jcrContentConnector);

        final ActionEvent.Handler actionEventHandler = mock(ActionEvent.Handler.class);
        eventBus.addHandler(ActionEvent.class, actionEventHandler);

        // WHEN
        final JcrPropertyItemId nonSystemPropertyId = new JcrPropertyItemId("fooUUID", "fooWorkspace", "foo");
        jcrBrowserTreePresenter.onItemEdited(nonSystemPropertyId, "name", null);

        // THEN
        verify(actionEventHandler, only()).onAction(any(ActionEvent.class));

        // WHEN
        final JcrPropertyItemId mgnlPropertyId = new JcrPropertyItemId("fooUUID", "fooWorkspace", "mgnl:foo");
        final JcrPropertyItemId jcrPropertyId = new JcrPropertyItemId("fooUUID", "fooWorkspace", "jcr:foo");

        jcrBrowserTreePresenter.onItemEdited(mgnlPropertyId, "name", null);
        jcrBrowserTreePresenter.onItemEdited(jcrPropertyId, "name", null);

        // THEN
        verify(subAppContext, times(2)).openNotification(eq(MessageStyleTypeEnum.WARNING), eq(true), anyString());
        verifyNoMoreInteractions(actionEventHandler);
    }

    private static class ExposingContainerJcrBrowserTreePresenter extends JcrBrowserTreePresenter {

        private HierarchicalJcrContainer container;

        public ExposingContainerJcrBrowserTreePresenter(TreeView view, ComponentProvider componentProvider, SubAppContext ctx, SimpleTranslator i18n) {
            super(view, componentProvider, ctx, i18n);
        }

        @Override
        protected Container.Hierarchical createContainer() {
            container = (HierarchicalJcrContainer) super.createContainer();
            return container;
        }
    }
}