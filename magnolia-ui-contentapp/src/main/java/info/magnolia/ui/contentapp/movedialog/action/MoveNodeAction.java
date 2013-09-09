/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog.action;

import com.vaadin.data.Item;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.framework.action.AbstractMultiItemAction;
import info.magnolia.ui.framework.action.MoveLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.jcr.Session;
import java.util.List;

/**
 * Action that moves a node.
 *
 * @see MoveNodeActionDefinition
 */
public class MoveNodeAction extends AbstractMultiItemAction<MoveNodeActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The item where the items should be moved relative to. */
    private final Item targetItem;

    private final AppController appController;

    private final UiContext uiContext;

    private final SubAppContext subAppContext;

    protected final EventBus admincentralEventBus;

    private MoveActionCallback callback;

    private MoveLocation moveLocation = MoveLocation.BEFORE;

    public MoveNodeAction(
            MoveNodeActionDefinition definition,
            List<JcrItemAdapter> items,
            Item targetItem,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
            AppController appController,
            UiContext uiContext,
            SubAppContext subAppContext,
            MoveActionCallback callback) {
        super(definition, items, uiContext);
        this.uiContext = uiContext;
        this.subAppContext = subAppContext;
        this.appController = appController;
        this.targetItem = targetItem;
        this.admincentralEventBus = admincentralEventBus;
        this.callback = callback;
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor();
        WorkbenchDefinition workbench = subAppDescriptor.getWorkbench();
        HierarchicalJcrContainer container = new HierarchicalJcrContainer(workbench);
        moveLocation = getDefinition().getMoveLocation();
        try {
            javax.jcr.Item source = item.getJcrItem();
            javax.jcr.Item target = null;

            String itemIdOfChangedItem = JcrItemUtil.getItemId(source.getParent());

            if (!(targetItem instanceof JcrItemAdapter)) {
                return;
            }
            target = ((JcrItemAdapter) targetItem).getJcrItem();
            if (!target.isNode()) {
                return;
            }

            // TODO: Should validate that it can actually move the item inside.
            if (moveLocation == MoveLocation.INSIDE) {
                container.moveItem(source, target);
            } else if (moveLocation == MoveLocation.BEFORE) {
                container.moveItemBefore(source, target);
            } else if (moveLocation == MoveLocation.AFTER) {
                container.moveItemAfter(source, target);
            }

            Session session = source.getSession();
            admincentralEventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), itemIdOfChangedItem));
            callback.onMovePerformed(targetItem, moveLocation);

        } catch (Exception e) {
            log.error("Problem occurred during moving items.", e);
        }

    }

    @Override
    protected String getSuccessMessage() {
        return "Item(s) moved.";
    }

    @Override
    protected String getFailureMessage() {
        return "Move failed.";
    }

}
