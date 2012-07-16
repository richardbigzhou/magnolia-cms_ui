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
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.editor.PageEditorView;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * PageEditorPresenter.
 */
public class PageEditorPresenter implements PageEditorView.Listener {

    private final PageEditorView view;

    private EventBus eventBus;
    private final DialogPresenterFactory dialogPresenterFactory;

    private PageEditorParameters parameters;
    private String path;

    @Inject
    public PageEditorPresenter(PageEditorView view, EventBus eventBus, DialogPresenterFactory dialogPresenterFactory) {
        this.view = view;
        this.eventBus = eventBus;
        this.dialogPresenterFactory = dialogPresenterFactory;

        registerHandlers();
    }

    private void registerHandlers() {
        eventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {
            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getPath().equals(getPath())) {
                    view.refresh();
                    setPath(null);
                }
            }
        });
    }

    @Override
    public void editComponent(String workSpace, String path, String dialog) {
        DialogView.Presenter dialogPresenter = dialogPresenterFactory.createDialog(dialog);

        try {
            Session session = MgnlContext.getJCRSession(workSpace);

            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);
            JcrNodeAdapter item = new JcrNodeAdapter(node);
            dialogPresenter.editItem(item);
            setPath(path);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteComponent(String workSpace, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);

        try {
            Session session = MgnlContext.getJCRSession(workSpace);

            Node parentNode = session.getNode(parent);
            session.removeItem(path);
            MetaDataUtil.updateMetaData(parentNode);
            session.save();
            view.refresh();

        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newComponent(String workSpace, String nodeType, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);
        String relPath = path.substring(index+1);

        Session session = null;
        try {
            session = MgnlContext.getJCRSession(workSpace);

            Node parentNode = session.getNode(parent);

            Node newNode = NodeUtil.createPath(parentNode, relPath, nodeType);
            MetaDataUtil.updateMetaData(newNode);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sortComponent(String workSpace, String parentPath, String source, String target, String order) {
        try {

            if (StringUtils.isBlank(order)) {
                order = "before";
            }

            if (StringUtils.equalsIgnoreCase(target, "mgnlNew")) {
                target = null;
            }

            Session session = MgnlContext.getJCRSession(workSpace);

            Node parent  = session.getNode(parentPath);
            Node component = parent.getNode(source);

            if("before".equals(order)) {
                NodeUtil.orderBefore(component, target);
            } else {
                NodeUtil.orderAfter(component, target);
            }

            MetaDataUtil.updateMetaData(parent);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            //log.error("Exception caught: {}", e.getMessage(), e);
        }
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
