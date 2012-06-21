/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.cms.core.Path;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrTransientNodeAdapter;
import info.magnolia.ui.widget.dialog.DialogView.Presenter;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Opens a dialog for creating a new node in a tree.
 * @version $Id$
 */
public class OpenCreateDialogAction extends ActionBase<OpenCreateDialogActionDefinition> {

    private DialogPresenterFactory dialogPresenterFactory;

    private Node parent;

    public OpenCreateDialogAction(OpenCreateDialogActionDefinition definition, Node parent, DialogPresenterFactory dialogPresenterFactory) {
        super(definition);
        this.parent = parent;
        this.dialogPresenterFactory = dialogPresenterFactory;
    }

    @Override
    public void execute() throws ActionExecutionException {

        Presenter dialogPresenter = dialogPresenterFactory.createDialog(getDefinition().getDialogName());
        String name;
        Node transientNode;
        try {
            name = getUniqueNewItemName(parent);
            transientNode = parent.addNode(name, getDefinition().getNodeType());
            dialogPresenter.editItem(new JcrTransientNodeAdapter(transientNode));
        } catch (AccessDeniedException e) {
            throw new ActionExecutionException(e);
        } catch (ItemNotFoundException e) {
            throw new ActionExecutionException(e);
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }

    }

    protected String getUniqueNewItemName(final Item item) throws RepositoryException, ItemNotFoundException, AccessDeniedException {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        return Path.getUniqueLabel(item.getSession(), item.getPath(), "untitled");
    }
}
