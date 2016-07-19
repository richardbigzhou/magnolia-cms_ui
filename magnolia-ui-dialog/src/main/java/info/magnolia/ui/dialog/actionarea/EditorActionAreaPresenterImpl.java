/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.definition.ActionRendererDefinition;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * Implementation of {@link EditorActionAreaPresenter}.
 */
public class EditorActionAreaPresenterImpl implements EditorActionAreaPresenter {

    private EditorActionAreaView view;

    private ComponentProvider componentProvider;

    @Inject
    public EditorActionAreaPresenterImpl(EditorActionAreaView view, ComponentProvider componentProvider) {
        this.view = view;
        this.componentProvider = componentProvider;
    }

    @Override
    public EditorActionAreaView start(Iterable<ActionDefinition> actions, EditorActionAreaDefinition definition, final ActionListener listener, UiContext uiContext) {
        final Map<String, View> secondaryActions = new HashMap<>();
        for (final ActionDefinition action : actions) {
            ActionRendererDefinition actionRendererDef = definition.getActionRenderers().get(action.getName());
            ActionRenderer actionRenderer = actionRendererDef == null ?
                    componentProvider.getComponent(ActionRenderer.class):
                    componentProvider.newInstance(actionRendererDef.getRendererClass(), action, actionRendererDef, uiContext);
            final View actionView = actionRenderer.start(action, listener);
            // We simply compare secondary actions by name, as comparing the underlying object might not work due to a
            // wrapped/proxied definition
            if (FluentIterable.from(definition.getSecondaryActions()).anyMatch(new Predicate<SecondaryActionDefinition>() {
                @Override
                public boolean apply(SecondaryActionDefinition secondaryActionDefinition) {
                    return action.getName().equals(secondaryActionDefinition.getName());
                }
            })) {
                // Store rendered secondary action
                secondaryActions.put(action.getName(), actionView);
            } else {
                view.addPrimaryAction(actionView, action.getName());
            }
        }

        // Add secondary action views according to their order in definition
        for (final SecondaryActionDefinition secondaryActionDefinition : definition.getSecondaryActions()) {
            final String actionName = secondaryActionDefinition.getName();
            final View actionView = secondaryActions.get(actionName);
            if (actionView != null) {
                view.addSecondaryAction(actionView, actionName);
            }
        }

        return view;
    }

    protected EditorActionAreaView getView() {
        return view;
    }
}
