/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionAreaView;
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionAreaViewImpl;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.renderer.DefaultEditorActionRenderer;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogViewImpl;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.workbench.tree.MoveLocation;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MoveDialogPresenterImpl}.
 */
public class MoveDialogPresenterImplTest {

    private ComponentProvider componentProvider = mock(ComponentProvider.class);

    private ActionRenderer actionRenderer = new DefaultEditorActionRenderer();

    private ChooseDialogViewImpl view = new ChooseDialogViewImpl();

    private MoveDialogActionAreaView actionAreaView = new MoveDialogActionAreaViewImpl();

    private MoveDialogPresenterImpl presenter = new MoveDialogPresenterImpl(componentProvider, view, null, null, null, null, null, null);

    @Before
    public void setUp() throws Exception {
        View moveInsideActionView = actionRenderer.start(new MoveNodeActionDefinition(MoveLocation.INSIDE), presenter);
        actionAreaView.addPrimaryAction(moveInsideActionView, MoveLocation.INSIDE.name());
        view.setActionAreaView(actionAreaView);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsCommitAndMoveInsideActionIsEnabled() throws Exception {
        // GIVEN
        actionAreaView.setActionEnabled(MoveLocation.INSIDE.name(), true);

        // WHEN
        String actionName = presenter.getCorrectActionName(BaseDialog.COMMIT_ACTION_NAME);

        // THEN
        assertEquals(MoveLocation.INSIDE.name(), actionName);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsCommitAndMoveInsideActionIsDisabled() throws Exception {
        // GIVEN
        actionAreaView.setActionEnabled(MoveLocation.INSIDE.name(), false);

        // WHEN
        String actionName = presenter.getCorrectActionName(BaseDialog.COMMIT_ACTION_NAME);

        // THEN
        assertNull(actionName);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsMoveInside() throws Exception {
        // WHEN
        String actionName = presenter.getCorrectActionName(MoveLocation.INSIDE.name());

        // THEN
        assertEquals(MoveLocation.INSIDE.name(), actionName);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsMoveBefore() throws Exception {
        // WHEN
        String actionName = presenter.getCorrectActionName(MoveLocation.BEFORE.name());

        // THEN
        assertEquals(MoveLocation.BEFORE.name(), actionName);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsMoveAfter() throws Exception {
        // WHEN
        String actionName = presenter.getCorrectActionName(MoveLocation.AFTER.name());

        // THEN
        assertEquals(MoveLocation.AFTER.name(), actionName);
    }

    @Test
    public void testGetCorrectActionNameWhenActionNameEqualsCancel() throws Exception {
        // WHEN
        String actionName = presenter.getCorrectActionName(BaseDialog.CANCEL_ACTION_NAME);

        // THEN
        assertEquals(BaseDialog.CANCEL_ACTION_NAME, actionName);
    }

}