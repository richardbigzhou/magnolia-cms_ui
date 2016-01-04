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
package info.magnolia.jcrbrowser.app.contenttools;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.jcrbrowser.app.SystemPropertiesVisibilityToggledEvent;
import info.magnolia.jcrbrowser.app.contentconnector.JcrBrowserContentConnector;
import info.magnolia.jcrbrowser.app.contentconnector.JcrBrowserContentConnector.JcrBrowserContentConnectorDefinition;
import info.magnolia.jcrbrowser.app.workbench.JcrBrowserWorkbenchPresenter;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Property;

public class JcrBrowserContextToolTest {

    private JcrBrowserContextTool tool;

    private JcrBrowserContextToolView view;

    private Property<String> workspaceSwitcherProperty;

    private Property<Boolean> systemPropertiesInclusionProperty;

    private EventBus eventBus;

    private RepositoryManager repositoryManager;

    private JcrBrowserContentConnector contentConnector;

    private JcrBrowserContentConnectorDefinition jcrBrowserContentConnectorDefinition;

    private JcrBrowserWorkbenchPresenter workbenchPresenter;

    @Before
    public void setUp() throws Exception {
        this.view = mock(JcrBrowserContextToolView.class);

        final JcrContentConnectorDefinition jcrContentConnectorDefinition = mock(JcrContentConnectorDefinition.class);
        doReturn("foo").when(jcrContentConnectorDefinition).getWorkspace();

        this.jcrBrowserContentConnectorDefinition = new JcrBrowserContentConnectorDefinition(jcrContentConnectorDefinition);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                systemPropertiesInclusionProperty = (Property<Boolean>) inv.getArguments()[0];
                return null;
            }
        }).when(view).setSystemPropertiesInclusionProperty(any(Property.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                workspaceSwitcherProperty = (Property<String>) inv.getArguments()[0];
                return null;
            }
        }).when(view).setWorkspaceNameProperty(any(Property.class));

        eventBus = mock(EventBus.class);
        repositoryManager = mock(RepositoryManager.class);

        doReturn(Lists.newArrayList("foo", "bar", "baz", "qux")).when(repositoryManager).getWorkspaceNames();

        contentConnector = mock(JcrBrowserContentConnector.class);
        doReturn(jcrBrowserContentConnectorDefinition).when(contentConnector).getContentConnectorDefinition();

        workbenchPresenter = mock(JcrBrowserWorkbenchPresenter.class);

        tool = new JcrBrowserContextTool(view, eventBus, repositoryManager, contentConnector, workbenchPresenter);
    }

    @Test
    public void toolSuppliesViewWithWorkspaceNamesSortedAlphabetically() throws Exception {
        // WHEN
        tool.start();

        // THEN
        verify(view).setWorkspaceOptions(argThat(isContainerWithIds("bar", "baz", "foo", "qux")));
    }

    @Test
    public void toolInitializesViewWithCorrectWorkspace() throws Exception {
        // WHEN
        tool.start();

        // THEN
        assertThat(workspaceSwitcherProperty.getValue(), equalTo("foo"));
    }

    @Test
    public void toolUpdatesContentConnectorDefinitionsWorkspaceAndRefreshesWorkbench() throws Exception {
        // WHEN
        tool.start();
        workspaceSwitcherProperty.setValue("bar");

        // THEN
        assertThat(jcrBrowserContentConnectorDefinition.getWorkspace(), equalTo("bar"));
        verify(workbenchPresenter).refresh();
    }

    @Test
    public void toolEmitsEventOnSystemPropertiesInlusionStateChange() throws Exception {
        // WHEN
        tool.start();
        systemPropertiesInclusionProperty.setValue(true);

        // THEN
        verify(eventBus, only()).fireEvent(any(SystemPropertiesVisibilityToggledEvent.class));
    }

    private Matcher<Container> isContainerWithIds(Object... ids) {
        return new ContainerIdsMatcher<>(ids);
    }

    private static class ContainerIdsMatcher<T extends Container> extends TypeSafeMatcher<T> {

        private final Object[] ids;

        public ContainerIdsMatcher(Object... ids) {
            this.ids = ids;
        }

        @Override
        protected boolean matchesSafely(T item) {
            return contains(ids).matches(item.getItemIds());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a Vaadin Container with item ids: ").appendText(Joiner.on(",").join(ids));
        }
    }

}