/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.app.pages;

import com.vaadin.terminal.ExternalResource;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.editor.PageEditor;
import info.magnolia.ui.widget.editor.PageEditorView;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * PageEditorPresenter.
 */
public class PageEditorPresenter implements PageEditorView.Presenter {

    private PageEditorView view;
    private Node pageNode;
    private String nodeName;
    private DialogPresenterFactory dialogPresenterFactory;

    public PageEditorPresenter(final Node pageNode) throws RepositoryException {
        this.pageNode = pageNode;
        this.view = new PageEditor(new ExternalResource(MgnlContext.getContextPath() + pageNode.getPath()));
        this.nodeName = pageNode.getName();
    }

    @Override
    public void editComponent(String workSpace, String path, String dialog) {
        DialogView.Presenter dialogPresenter = dialogPresenterFactory.createDialog(dialog);
        Session session = null;
        try {
            session = MgnlContext.getJCRSession(workSpace);

        if (path == null || !session.itemExists(path)) {
            path = "/";
        }
        final Node node = session.getNode(path);
            JcrNodeAdapter item = new JcrNodeAdapter(node);
            dialogPresenter.editItem(item);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public IsVaadinComponent getView() {
        return view;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }
}
