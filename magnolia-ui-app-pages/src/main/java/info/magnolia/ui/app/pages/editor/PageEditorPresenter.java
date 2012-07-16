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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.editor.PageEditorView;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.NotImplementedException;


/**
 * PageEditorPresenter.
 */
public class PageEditorPresenter implements PageEditorView.Listener {

    private final PageEditorView view;

    private final DialogPresenterFactory dialogPresenterFactory;

    private PageEditorParameters parameters;

    @Inject
    public PageEditorPresenter(PageEditorView view, DialogPresenterFactory dialogPresenterFactory) {
        this.view = view;
        this.dialogPresenterFactory = dialogPresenterFactory;
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
            e.printStackTrace();
        }
    }

    @Override
    public void deleteComponent(String workSpace, String path) {
        /*    Session session = null;
            try {
                session = MgnlContext.getJCRSession(workSpace);

                Node page = session.getNode(handle);
                session.removeItem(path);
                MetaDataUtil.updateMetaData(page);
                session.save();
            } catch (RepositoryException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }*/
    }

    @Override
    public void newComponent(String workSpace, String nodeType, String path) {
        throw new NotImplementedException();
    }

    @Override
    public void sortComponent() {
        // sort paragraphs
        /*        try {
                    String pathSelected = request.getParameter(PARAM_PATH_SELECTED);
                    String pathTarget = request.getParameter(PARAM_PATH_TARGET);
                    String pathParent = StringUtils.substringBeforeLast(pathSelected, "/");
                    String srcName = StringUtils.substringAfterLast(pathSelected, "/");
                    String destName = StringUtils.substringAfterLast(pathTarget, "/");
                    String order = StringUtils.defaultIfEmpty(request.getParameter("order"), "before");
                    if (StringUtils.equalsIgnoreCase(destName, "mgnlNew")) {
                        destName = null;
                    }
                    Node parent = session.getNode(pathParent+srcName);

                    if("before".equals(order)) {
                        NodeUtil.orderBefore(parent, destName);
                    } else {
                        NodeUtil.orderAfter(parent, destName);
                    }

                    Node page = session.getNode(handle);
                    MetaDataUtil.updateMetaData(page);
                    session.save();
                } catch (RepositoryException e) {
                    log.error("Exception caught: {}", e.getMessage(), e);
                }*/
    }

    @Override
    public void selectComponent(String path) {
        String selectedComponentPath = path;
    }

    public PageEditorView start() {
        view.setListener(this);
        view.init(parameters.getContextPath(), parameters.getNodePath());
        return view;
    }

    public void setParameters(PageEditorParameters parameters) {
        this.parameters = parameters;
    }
}
