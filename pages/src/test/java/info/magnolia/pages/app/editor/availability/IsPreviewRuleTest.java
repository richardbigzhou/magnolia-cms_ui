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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.pages.app.editor.PagesEditorSubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsPreviewRule}.
 */
public class IsPreviewRuleTest {

    private PagesEditorSubApp subApp;
    private PageEditorParameters parameters;
    private IsPreviewRuleDefinition definition;
    private SubAppContext subAppContext;
    private IsPreviewRule rule;

    @Before
    public void setUp() throws Exception {
        this.subApp = mock(PagesEditorSubApp.class);
        this.parameters = mock(PageEditorParameters.class);
        this.definition = mock(IsPreviewRuleDefinition.class);
        this.subAppContext = mock(SubAppContext.class);

        when(subApp.getParameters()).thenReturn(parameters);
        when(subAppContext.getSubApp()).thenReturn(subApp);
        this.rule = new IsPreviewRule(definition, subAppContext);

    }

    @Test
    public void testExpectingToBeInPreviewAndIsPreview() throws Exception {
        // GIVEN
        when(parameters.isPreview()).thenReturn(true);
        when(definition.isPreview()).thenReturn(true);

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        assertTrue(result);

    }

    @Test
    public void testExpectingToBeInPreviewAndIsNotPreview() throws Exception {
        // GIVEN
        when(parameters.isPreview()).thenReturn(false);
        when(definition.isPreview()).thenReturn(true);

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        assertFalse(result);

    }
}
