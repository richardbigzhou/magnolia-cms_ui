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
package info.magnolia.ui.mediaeditor;

import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.mediaeditor.action.availability.MediaEditorAvailabilityChecker;
import info.magnolia.ui.mediaeditor.definition.ConfiguredMediaEditorDefinition;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.vaadin.ui.Component;

/**
 * Test class for {@link MediaEditorPresenterImpl}.
 */
public class MediaEditorPresenterImplTest {

    @Test
    public void testUpdateActionbar() {
        // GIVEN
        ConfiguredMediaEditorDefinition mediaEditorDefinition = new ConfiguredMediaEditorDefinition();

        Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

        ConfiguredActionDefinition actionEnabledDefinition = new ConfiguredActionDefinition();
        AvailabilityDefinition availabilityEnabledDefinition = new ConfiguredAvailabilityDefinition();
        actionEnabledDefinition.setAvailability(availabilityEnabledDefinition);
        actions.put("actionEnabled", actionEnabledDefinition);

        ConfiguredActionDefinition actionDisabledDefinition = new ConfiguredActionDefinition();
        AvailabilityDefinition availabilityDisabledDefinition = new ConfiguredAvailabilityDefinition();
        actionDisabledDefinition.setAvailability(availabilityDisabledDefinition);
        actions.put("actionDisabled", actionDisabledDefinition);

        mediaEditorDefinition.setActions(actions);

        ActionbarPresenter actionbarPresenter = mock(ActionbarPresenter.class);
        MediaEditorAvailabilityChecker mediaEditorAvailabilityChecker = mock(MediaEditorAvailabilityChecker.class);
        MediaEditorView mediaEditorView = mock(MediaEditorView.class);
        when(mediaEditorView.asVaadinComponent()).thenReturn(mock(Component.class));
        MediaEditorPresenterImpl mediaEditorPresenter = new MediaEditorPresenterImpl(mediaEditorDefinition, mock(EventBus.class), mediaEditorView, actionbarPresenter, null, null, null, mediaEditorAvailabilityChecker);

        when(mediaEditorAvailabilityChecker.isAvailable(availabilityEnabledDefinition)).thenReturn(true);
        when(mediaEditorAvailabilityChecker.isAvailable(availabilityDisabledDefinition)).thenReturn(false);

        // WHEN
        mediaEditorPresenter.updateActionbar();

        // THEN
        verify(actionbarPresenter).enable("actionEnabled");
        verify(actionbarPresenter).disable("actionDisabled");
    }
}
