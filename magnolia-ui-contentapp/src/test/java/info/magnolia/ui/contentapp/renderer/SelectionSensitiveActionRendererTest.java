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
package info.magnolia.ui.contentapp.renderer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SelectionSensitiveActionRenderer}.
 */
public class SelectionSensitiveActionRendererTest extends MgnlTestCase {

    private ActionListener actionListener;
    private ActionDefinition actionDefinition;
    private SelectionSensitiveActionRenderer renderer;
    private EventBus chooseDialogEventBus;
    private Set<Object> itemId;
    private SelectionChangedEvent event;
    private AvailabilityChecker availabilityChecker;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init
        this.chooseDialogEventBus = new SimpleEventBus();

        this.availabilityChecker = mock(AvailabilityChecker.class);
        this.actionDefinition = mock(ActionDefinition.class);
        this.actionListener = mock(ActionListener.class);
        ConfiguredAvailabilityDefinition availability = mock(ConfiguredAvailabilityDefinition.class);

        this.renderer = new SelectionSensitiveActionRenderer(chooseDialogEventBus, availabilityChecker);
        this.itemId = new HashSet<Object>();
        itemId.add("one");
        this.event = new SelectionChangedEvent(itemId);

        when(actionDefinition.getAvailability()).thenReturn(availability);
        when(availabilityChecker.isAvailable(any(ConfiguredAvailabilityDefinition.class), anyList())).thenReturn(true);
    }

    @Test
    public void isEnabledForValidSelection() {
        // GIVEN
        when(availabilityChecker.isAvailable(any(ConfiguredAvailabilityDefinition.class), anyList())).thenReturn(true);
        View view = renderer.start(actionDefinition, actionListener);

        // WHEN
        // applicable item change event is sent, should pass availability check
        chooseDialogEventBus.fireEvent(event);

        // THEN
        assertTrue(view.asVaadinComponent().isEnabled());
    }

    @Test
    public void isDisabledForInvalidSelection() {
        // GIVEN
        when(availabilityChecker.isAvailable(any(ConfiguredAvailabilityDefinition.class), anyList())).thenReturn(false);
        View view = renderer.start(actionDefinition, actionListener);

        // WHEN
        // applicable item change event is sent, shouldn't pass availability check
        chooseDialogEventBus.fireEvent(event);

        // THEN
        assertFalse(view.asVaadinComponent().isEnabled());
    }
}
