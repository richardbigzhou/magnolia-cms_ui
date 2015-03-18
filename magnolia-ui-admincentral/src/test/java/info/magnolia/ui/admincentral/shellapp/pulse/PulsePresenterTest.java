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
package info.magnolia.ui.admincentral.shellapp.pulse;

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ConfiguredPulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.PulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListView;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.message.MessagesManager;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PulsePresenter}.
 */
public class PulsePresenterTest extends MgnlTestCase {

    private PulseView view;
    private EventBus eventBus;
    private ConfiguredPulseDefinition pulseDefinition;
    private PulsePresenter pulsePresenter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockContext context = new MockContext();
        User user = mock(User.class);
        when(user.getName()).thenReturn("testuser");
        context.setUser(user);
        this.view = mock(PulseView.class);
        this.eventBus = mock(EventBus.class);
        pulseDefinition = new ConfiguredPulseDefinition();

        // init components.
        initComponents();
        pulseDefinition.addPresenters(getTaskListDefinition());
        pulseDefinition.addPresenters(getMessagesListDefinition());

        this.pulsePresenter = Components.newInstance(PulsePresenter.class);

    }

    @Test
    public void testStartPulsePresenter() {

        // WHEN
        pulsePresenter.start();

        // THEN
        verify(view, times(1)).setPulseSubView(any(PulseListView.class));
    }

    @Test
    public void testRetrievingBadgeCountFromListPresenters() throws Exception {
        // GIVEN

        // WHEN
        pulsePresenter.start();

        // THEN
        view.updateCategoryBadgeCount(any(PulseItemCategory.class), eq(8));

    }

    @Override
    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
    }

    private ConfiguredPulseListDefinition getTaskListDefinition() {
        ConfiguredPulseListDefinition taskListDefinition = new ConfiguredPulseListDefinition();
        taskListDefinition.setName("tasks");
        taskListDefinition.setPresenterClass(TestListPresenter.class);

        return taskListDefinition;
    }

    private ConfiguredPulseListDefinition getMessagesListDefinition() {
        ConfiguredPulseListDefinition messagesListDefinition = new ConfiguredPulseListDefinition();
        messagesListDefinition.setName("messages");
        messagesListDefinition.setPresenterClass(TestListPresenter.class);

        return messagesListDefinition;
    }

    /**
     * Initialize components used by {@link PulsePresenter}.
     */
    private void initComponents() {
        ComponentsTestUtil.setInstance(PulseDefinition.class, pulseDefinition);
        ComponentsTestUtil.setInstance(PulseListDefinition.class, mock(PulseListDefinition.class));
        ComponentsTestUtil.setInstance(EventBus.class, eventBus);
        ComponentsTestUtil.setInstance(PulseView.class, view);
        ComponentsTestUtil.setInstance(Shell.class, mock(Shell.class));
        ComponentsTestUtil.setInstance(AppController.class, mock(AppController.class));
        ComponentsTestUtil.setInstance(MessagesManager.class, mock(MessagesManager.class));
        ComponentsTestUtil.setInstance(ComponentProvider.class, Components.getComponentProvider());
        ComponentsTestUtil.setInstance(PulseListView.class, mock(PulseListView.class));
    }

    private class TestListPresenter implements PulseListPresenter {

        private PulseListDefinition definition;
        private PulseListView view;

        @Inject
        public TestListPresenter(PulseListDefinition definition, PulseListView view) {
            this.definition = definition;
            this.view = view;
        }

        @Override
        public View start() {
            return view;
        }

        @Override
        public View openItem(String itemId) throws RegistrationException {
            return null;
        }

        @Override
        public void setListener(Listener listener) {

        }

        @Override
        public int getPendingItemCount() {
            if ("tasks".equals(definition.getName())) {
                return 3;
            }
            else if ("messages".equals(definition.getName())) {
                return 5;
            }
            return 0;
        }

        @Override
        public PulseItemCategory getCategory() {
            return PulseItemCategory.MESSAGES;
        }
    }

}
