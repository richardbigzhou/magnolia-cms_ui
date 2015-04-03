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

import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionAreaViewImpl;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.definition.ConfiguredEditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.renderer.DefaultEditorActionRenderer;
import info.magnolia.ui.workbench.tree.MoveLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MoveDialogActionAreaPresenter}.
 */
public class MoveDialogActionAreaPresenterImplTest {

    private ComponentProvider provider = mock(ComponentProvider.class);

    private MoveDialogActionAreaPresenter presenter;

    private MoveDialogActionAreaViewImpl view;

    private UiContext uiContext = mock(UiContext.class);

    @Before
    public void setUp() throws Exception {
        this.view = new MoveDialogActionAreaViewImpl();
        this.presenter = new MoveDialogActionAreaPresenterImpl(view, provider);

        when(provider.getComponent(ActionRenderer.class)).thenReturn(new DefaultEditorActionRenderer());
        List<ActionDefinition> actions = new ArrayList<ActionDefinition>();
        for (MoveLocation location : MoveLocation.values()) {
            ConfiguredActionDefinition definition = new MoveNodeActionDefinition(location);
            definition.setName(location.name());
            definition.setLabel("move " + location.name());
            actions.add(definition);
        }

        presenter.start(actions, new ConfiguredEditorActionAreaDefinition(), new ActionListener() {
            @Override
            public void onActionFired(String actionName, Object... actionContextParams) {
            }
        }, uiContext);
    }

    @Test
    public void testSetPossibleMoveLocations() throws Exception {
        //GIVEN
        Set<MoveLocation> locations = new HashSet<MoveLocation>();
        locations.add(MoveLocation.AFTER);

        //WHEN
        presenter.setPossibleMoveLocations(locations);

        //THEN
        assert(view.getViewForAction(MoveLocation.AFTER.name()).asVaadinComponent().isEnabled());
        assert(!view.getViewForAction(MoveLocation.BEFORE.name()).asVaadinComponent().isEnabled());
        assert(!view.getViewForAction(MoveLocation.INSIDE.name()).asVaadinComponent().isEnabled());
    }
}
