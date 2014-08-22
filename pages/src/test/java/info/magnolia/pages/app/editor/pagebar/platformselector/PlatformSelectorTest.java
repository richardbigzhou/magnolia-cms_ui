/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.pagebar.platformselector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.parameters.DefaultPageEditorStatus;
import info.magnolia.pages.app.editor.parameters.PageEditorStatus;
import info.magnolia.test.mock.MockAggregationState;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PlatformSelector}.
 */
public class PlatformSelectorTest {

    private PlatformSelector selector;
    private PlatformSelectorView view;
    private PageEditorPresenter pageEditorPresenter;
    private PageEditorStatus pageEditorStatus;

    @Before
    public void setUp() throws Exception {

        MockWebContext ctx = new MockWebContext();
        ctx.setAggregationState(new MockAggregationState());

        MgnlContext.setInstance(ctx);

        this.view = mock(PlatformSelectorView.class);
        this.pageEditorStatus = new DefaultPageEditorStatus(mock(I18NAuthoringSupport.class));
        this.pageEditorPresenter = mock(PageEditorPresenter.class);
        when(pageEditorPresenter.getStatus()).thenReturn(pageEditorStatus);
        this.selector = new PlatformSelector(view, pageEditorPresenter);
    }

    @Test
    public void testLocationUpdateInEdit() throws Exception {

        // WHEN
        selector.onLocationUpdate(new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/bla", null));

        // THEN
        verify(view, times(1)).setPlatFormType(PlatformType.DESKTOP);
        verify(view, times(1)).setVisible(false);
    }

    @Test
    public void testLocationUpdateInView() throws Exception {

        // WHEN
        selector.onLocationUpdate(new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, "/bla", null));

        // THEN
        verify(view, times(0)).setPlatFormType(any(PlatformType.class));
        verify(view, times(1)).setVisible(true);
    }

    @Test
    public void testSettingPlatform() throws Exception {

        // WHEN
        selector.platformSelected(PlatformType.MOBILE);

        // THEN
        assertThat(pageEditorStatus.getPlatformType(), is(PlatformType.MOBILE));
        verify(pageEditorPresenter, times(1)).loadPageEditor();
    }

    @Test
    public void testIgnoreSettingUnchangedPlatform() throws Exception {

        // WHEN
        selector.platformSelected(PlatformType.DESKTOP);

        // THEN
        assertThat(pageEditorStatus.getPlatformType(), is(PlatformType.DESKTOP));
        verify(pageEditorPresenter, times(0)).loadPageEditor();
    }
}
