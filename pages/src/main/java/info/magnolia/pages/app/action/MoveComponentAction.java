/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.pages.app.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.pages.app.editor.event.ComponentMoveEvent;
import info.magnolia.ui.api.app.SubAppEventBus;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Action used for starting and stopping move events components.
 * Usages:
 * <pre>
 *     <ul>
 *         <li>{@link info.magnolia.pages.app.editor.PagesEditorSubApp#onActionbarItemClicked(String)} when action triggered from actionbar.</li>
 *         <li>{@link info.magnolia.pages.app.editor.PagesEditorSubApp#onEscape()} when action by escape key.</li>
 *         <li>{@link info.magnolia.ui.vaadin.editor.PageEditor} when move action coming from client side.</li>
 *     </ul>
 * </pre>
 * @see MoveComponentActionDefinition
 */
public class MoveComponentAction extends AbstractAction<MoveComponentActionDefinition> {

    private final EventBus eventBus;

    @Inject
    public MoveComponentAction(MoveComponentActionDefinition definition, @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {
        boolean start = getDefinition().isStart();
        boolean serverSide = getDefinition().isServerSide();
        eventBus.fireEvent(new ComponentMoveEvent(start, serverSide));
    }
}
