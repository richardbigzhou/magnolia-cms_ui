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
package info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ConfiguredPulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesContainer;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesListView;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.framework.availability.AvailabilityCheckerImpl;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PulseListFooterPresenterTest {

    private PulseListFooterView view;
    private ConfiguredPulseListDefinition definition;
    private PulseListFooterPresenter pulseListFooterPresenter;
    private MessagesListPresenter messagesListPresenter;
    private Set<String> selectedItems;

    @Before
    public void setUp() throws Exception {
        view = mock(PulseListFooterView.class);
        pulseListFooterPresenter = new PulseListFooterPresenter(view);

        ComponentProvider componentProvider = new MockComponentProvider();

        definition = new ConfiguredPulseListDefinition();
        I18nizer i18nizer = mock(I18nizer.class);
        when(i18nizer.decorate(definition)).thenReturn(definition);

        AvailabilityChecker availabilityChecker = new AvailabilityCheckerImpl(componentProvider, mock(ContentConnector.class));

        MockContext context = new MockContext();
        MgnlContext.setInstance(context);
        User user = mock(User.class);
        when(user.getName()).thenReturn("username");
        when(user.getAllRoles()).thenReturn(Arrays.asList("canexecute"));
        context.setUser(user);

        selectedItems = new HashSet<>();
        selectedItems.add("item 1");

        MessagesListView messageView = mock(MessagesListView.class);
        when(messageView.getSelectedItemIds()).thenReturn(Arrays.asList(selectedItems.toArray()));

        messagesListPresenter = new MessagesListPresenter(mock(MessagesContainer.class), mock(EventBus.class),
                messageView, null, componentProvider, context, definition, availabilityChecker,
                new PulseListActionExecutor(componentProvider), pulseListFooterPresenter, i18nizer);
    }

    @Test
    public void verifyThatBulkActionCreatedEnough() {
        // GIVEN
        List<ActionDefinition> bulkActions = new ArrayList<>();
        bulkActions.add(new ConfiguredActionDefinition());
        bulkActions.add(new ConfiguredActionDefinition());

        // WHEN
        pulseListFooterPresenter.start(bulkActions, 0);

        // THEN
        verify(view, times(2)).addActionItem(anyString(), anyString(), anyString());
    }

    @Test
    public void verifyThatBulkActionEnableWithoutConfiguredRole() {
        // GIVEN
        List<ActionDefinition> bulkActions = new ArrayList<>();
        bulkActions.add(new ConfiguredActionDefinition());

        definition.setBulkActions(bulkActions);

        // WHEN
        messagesListPresenter.onSelectionChanged(selectedItems);

        // THEN
        verify(view, times(1)).setActionEnabled(anyString(), eq(true));
    }

    @Test
    public void verifyThatBulkActionEnableWithMatchedConfiguredRole() {
        // GIVEN
        List<ActionDefinition> bulkActions = new ArrayList<>();

        ConfiguredAccessDefinition accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.setRoles(Arrays.asList("canexecute"));

        ConfiguredAvailabilityDefinition availability = new ConfiguredAvailabilityDefinition();
        availability.setAccess(accessDefinition);

        ConfiguredActionDefinition deleteAction = new ConfiguredActionDefinition();
        deleteAction.setAvailability(availability);
        bulkActions.add(deleteAction);

        definition.setBulkActions(bulkActions);

        // WHEN
        messagesListPresenter.onSelectionChanged(selectedItems);

        // THEN
        verify(view, times(1)).setActionEnabled(anyString(), eq(true));
    }

    @Test
    public void verifyThatBulkActionDisableWithUnmatchedConfiguredRole() {
        // GIVEN
        List<ActionDefinition> bulkActions = new ArrayList<>();

        ConfiguredAccessDefinition accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.setRoles(Arrays.asList("cannotexecute"));

        ConfiguredAvailabilityDefinition availability = new ConfiguredAvailabilityDefinition();
        availability.setAccess(accessDefinition);

        ConfiguredActionDefinition deleteAction = new ConfiguredActionDefinition();
        deleteAction.setAvailability(availability);
        bulkActions.add(deleteAction);

        definition.setBulkActions(bulkActions);

        // WHEN
        messagesListPresenter.onSelectionChanged(selectedItems);

        // THEN
        verify(view, times(1)).setActionEnabled(anyString(), eq(false));
    }
}
