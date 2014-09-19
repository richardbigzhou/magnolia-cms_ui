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
package info.magnolia.pages.app.editor;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.pages.app.editor.parameters.PageEditorStatus;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.vaadin.editor.PageEditorView;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PageEditorPresenter}.
 */
public class PageEditorPresenterTest {

    private PageEditorPresenter pageEditorPresenter;
    private PageEditorPresenter.Listener listener;

    @Before
    public void setUp() throws Exception {
        this.pageEditorPresenter = new PageEditorPresenter(mock(ActionExecutor.class), mock(PageEditorView.class),
                mock(EventBus.class),mock(SubAppContext.class), mock(SimpleTranslator.class), mock(PageEditorStatus.class));

        listener = mock(PageEditorPresenter.Listener.class);
        pageEditorPresenter.setListener(listener);

    }

    @Test
    public void testSelectingExternalPage() throws Exception {
        // GIVEN

        // WHEN
        pageEditorPresenter.onExternalPageSelect();

        //THEN
        assertThat(pageEditorPresenter.getSelectedElement(), is(nullValue()));
        verify(listener, times(1)).deactivateComponents();
        verify(listener, times(1)).updateCaptionForExternalPage(anyString());

    }
}
