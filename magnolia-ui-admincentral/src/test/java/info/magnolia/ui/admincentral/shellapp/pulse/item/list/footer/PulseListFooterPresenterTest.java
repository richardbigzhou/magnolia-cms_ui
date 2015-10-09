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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ExternalResource;

/**
 * Test class for {@link PulseListFooterPresenter}.
 */
public class PulseListFooterPresenterTest {

    private PulseListFooterPresenter pulseListFooterPresenter;
    private PulseListFooterView view;

    @Before
    public void setUp() throws Exception {
        view = mock(PulseListFooterView.class);
        pulseListFooterPresenter = new PulseListFooterPresenter(view);
    }

    @Test
    public void verifyThatBulkActionCreatedEnough() {
        // GIVEN
        List<ActionDefinition> bulkActions = new ArrayList<>();
        bulkActions.add(new ConfiguredActionDefinition());
        bulkActions.add(new ConfiguredActionDefinition());

        // WHEN
        pulseListFooterPresenter.start(bulkActions, 10);

        // THEN
        verify(view, times(2)).addActionItem(anyString(), anyString(), any(ExternalResource.class));
    }
}
