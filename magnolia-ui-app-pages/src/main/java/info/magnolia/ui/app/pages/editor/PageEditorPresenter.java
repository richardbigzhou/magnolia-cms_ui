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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.app.pages.field.ComponentSelectorDefinition;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;
import info.magnolia.ui.model.tab.definition.TabDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.MagnoloaDialogPresenter;
import info.magnolia.ui.widget.editor.PageEditorView;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PageEditorPresenter.
 */
public class PageEditorPresenter implements PageEditorView.Listener {

    private static final String NEW_COMPONENT_DIALOG = "ui-pages-app:newComponent";

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);

    private final PageEditorView view;

    private final EventBus admincentralEventBus;

    private final EventBus appEventBus;

    private final DialogPresenterFactory dialogPresenterFactory;

    private final TemplateDefinitionRegistry templateDefinitionRegistry;

    private PageEditorParameters parameters;

    private String path;

    private String dialog;

    private final ConfiguredDialogDefinition dialogDefinition;

    @Inject
    public PageEditorPresenter(PageEditorView view, @Named("admincentral") EventBus admincentralEventBus, @Named("app") EventBus appEventBus, DialogPresenterFactory dialogPresenterFactory, TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.view = view;
        this.admincentralEventBus = admincentralEventBus;
        this.appEventBus = appEventBus;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.templateDefinitionRegistry = templateDefinitionRegistry;

        this.dialogDefinition = (ConfiguredDialogDefinition) dialogPresenterFactory.getDialogDefinition(NEW_COMPONENT_DIALOG);

        registerHandlers();
    }

    private void registerHandlers() {
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getPath().equals(getPath())) {
                    view.refresh();
                    setPath(null);
                    setDialog(null);
                }
            }
        });
    }

    @Override
    public void editComponent(String workspace, String path, String dialog) {
        final MagnoloaDialogPresenter.Presenter dialogPresenter = dialogPresenterFactory.createDialog(dialog);

        try {
            Session session = MgnlContext.getJCRSession(workspace);

            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);
            final JcrNodeAdapter item = new JcrNodeAdapter(node);
            setPath(path);
            setDialog(dialog);
            createDialogAction(item, dialogPresenter);
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void newComponent(String workspace, String path, String availableComponents) {

        updateDialogDefinition(availableComponents);

        MagnoloaDialogPresenter.Presenter dialogPresenter = dialogPresenterFactory.getDialogPresenter(dialogDefinition);

        try {
            Session session = MgnlContext.getJCRSession(workspace);

            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(path);

            JcrNodeAdapter item = new JcrNewNodeAdapter(parentNode, MgnlNodeType.NT_COMPONENT);
            DefaultProperty property = new DefaultProperty(item.JCR_NAME, "0");
            item.addItemProperty(item.JCR_NAME, property);
            setPath(path);
            setDialog(NEW_COMPONENT_DIALOG);

            createDialogAction(item, dialogPresenter);
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }

    }

    /**
     * Create a Dialog and define the call back actions.
     */
    private void createDialogAction(final JcrNodeAdapter item, final MagnoloaDialogPresenter.Presenter dialogPresenter) {
        final EventBus eventBus = dialogPresenter.getEventBus();
        dialogPresenter.start(item, new MagnoloaDialogPresenter.Presenter.Callback() {

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                dialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                dialogPresenter.closeDialog();
            }
        });
    }

    private void updateDialogDefinition(String availableComponents) {

        ConfiguredTabDefinition tabDefinition = new ConfiguredTabDefinition();
        tabDefinition.setLabel("Components");

        ComponentSelectorDefinition selector = new ComponentSelectorDefinition();
        selector.setName("MetaData/mgnl:template");
        selector.setLabel("Component");
        String[] tokens = availableComponents.split(",");

        for (int i = 0; i < tokens.length; i++) {
            try {
                TemplateDefinition paragraphInfo = templateDefinitionRegistry.getTemplateDefinition(tokens[i]);
                SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                option.setValue(paragraphInfo.getId());
                option.setName(paragraphInfo.getTitle());
                selector.addOption(option);

            } catch (RegistrationException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }

        }
        tabDefinition.addField(selector);
        List<TabDefinition> tabs = new LinkedList<TabDefinition>();
        tabs.add(tabDefinition);
        dialogDefinition.setTabs(tabs);
    }

    @Override
    public void deleteComponent(String workspace, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);

        try {
            Session session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(parent);
            session.removeItem(path);
            MetaDataUtil.updateMetaData(parentNode);
            session.save();
            view.refresh();

        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void newArea(String workspace, String nodeType, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);
        String relPath = path.substring(index + 1);

        Session session = null;
        try {
            session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(parent);

            Node newNode = NodeUtil.createPath(parentNode, relPath, nodeType);
            MetaDataUtil.updateMetaData(newNode);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sortComponent(String workspace, String parentPath, String source, String target, String order) {
        try {

            if (StringUtils.isBlank(order)) {
                order = "before";
            }

            if (StringUtils.equalsIgnoreCase(target, "mgnlNew")) {
                target = null;
            }

            Session session = MgnlContext.getJCRSession(workspace);

            Node parent = session.getNode(parentPath);
            Node component = parent.getNode(source);

            if ("before".equals(order)) {
                NodeUtil.orderBefore(component, target);
            } else {
                NodeUtil.orderAfter(component, target);
            }

            MetaDataUtil.updateMetaData(parent);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void selectNode(String workspace, String path, String dialog) {
        setPath(path);
        setDialog(dialog);
        appEventBus.fireEvent(new NodeSelectedEvent(path, workspace));
    }

    public PageEditorView start() {
        view.setListener(this);
        view.init(parameters.getContextPath(), parameters.getNodePath(), parameters.isPreview());
        setPath(parameters.getNodePath());
        // TODO 20120823 mgeljic get page dialog from page editor view
        setDialog("ui-pages-app:pages");
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

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

}
