/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.dialog.actionarea;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.config.MutableWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EditorActionAreaPresenterImplTest {

    private final ActionListener actionListener = mock(ActionListener.class);
    private final UiContext uiContext = mock(UiContext.class);
    private final View actionView = mock(View.class);

    private EditorActionAreaView view;
    private EditorActionAreaPresenterImpl editorActionAreaPresenter;

    @Before
    public void setUp() {
        final ActionRenderer actionRenderer = mock(ActionRenderer.class);
        when(actionRenderer.start(any(ActionDefinition.class), eq(actionListener))).thenReturn(actionView);

        ComponentsTestUtil.setInstance(ActionRenderer.class, actionRenderer);

        view = mock(EditorActionAreaView.class);

        editorActionAreaPresenter = new EditorActionAreaPresenterImpl(view, Components.getComponentProvider());
    }

    @Test
    public void makeSureSecondaryActionsAreAddedToCorrectArea() {
        // GIVEN
        final String secondaryActionName = "secondary";

        final ConfiguredActionDefinition primaryAction = new ConfiguredActionDefinition();
        primaryAction.setName("primary");
        final ConfiguredActionDefinition secondaryAction = new ConfiguredActionDefinition();
        secondaryAction.setName(secondaryActionName);

        final List<ActionDefinition> actions = Lists.newArrayList();
        actions.add(primaryAction);
        actions.add(secondaryAction);
        final List<SecondaryActionDefinition> secondaryActions = Lists.newArrayList();
        secondaryActions.add(new SecondaryActionDefinition(secondaryActionName));

        final EditorActionAreaDefinition editorActionAreaDefinition = mock(EditorActionAreaDefinition.class);
        when(editorActionAreaDefinition.getSecondaryActions()).thenReturn(secondaryActions);

        // The problem occurs when the underlying definition is wrapped
        final EditorActionAreaDefinition wrappedEditorActionAreaDefinition = MutableWrapper.wrap(editorActionAreaDefinition);

        // WHEN
        editorActionAreaPresenter.start(actions, wrappedEditorActionAreaDefinition, actionListener, uiContext);

        // THEN
        // We want to verify that the secondary action was indeed added
        verify(view, times(1)).addSecondaryAction(eq(actionView), eq(secondaryActionName));
    }

}