/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.pages.app.editor.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsAreaOptionalRule}.
 */
public class IsAreaOptionalRuleTest {
    private PageEditorPresenter presenter;
    private AreaElement element;
    private IsAreaOptionalRule rule;

    @Before
    public void setUp() throws Exception {
        this.presenter = mock(PageEditorPresenter.class);
        this.element = mock(AreaElement.class);
        when(presenter.getSelectedElement()).thenReturn(element);

        this.rule = new IsAreaOptionalRule(presenter);
    }

    @Test
    public void testAreaIsOptional() throws Exception {
        // GIVEN
        when(element.isOptional()).thenReturn(true);

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        // THEN
        assertTrue(result);
    }

    @Test
    public void testAreaIsNotOptional() throws Exception {
        // GIVEN
        when(element.isOptional()).thenReturn(false);

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        // THEN
        assertFalse(result);
    }

    @Test
    public void testExpectingAreaButIsComponent() throws Exception {
        // GIVEN
        when(presenter.getSelectedElement()).thenReturn(mock(ComponentElement.class));
        this.rule = new IsAreaOptionalRule(presenter);

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        // THEN
        assertFalse(result);
    }
}
