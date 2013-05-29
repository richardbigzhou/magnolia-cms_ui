/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.app.security.dialog.field;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogView;
import info.magnolia.ui.contentapp.choosedialog.WorkbenchChooseDialogPresenter;
import info.magnolia.ui.contentapp.choosedialog.WorkbenchChooseDialogView;
import info.magnolia.ui.framework.app.ItemChosenListener;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Field builder for the workspace ACL field.
 *
 * @see WorkspaceAccessFieldDefinition
 */
public class WorkspaceAccessFieldBuilder<D extends WorkspaceAccessFieldDefinition> extends AbstractAccessFieldBuilder<D> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessFieldBuilder.class);

    private static final String PERMISSIONS_PROPERTY_NAME = "permissions";
    private static final String PATH_PROPERTY_NAME = "path";

    private final ComponentProvider componentProvider;
    private final UiContext uiContext;

    public WorkspaceAccessFieldBuilder(D definition, Item relatedFieldItem, ComponentProvider componentProvider, UiContext uiContext) {
        super(definition, relatedFieldItem);
        this.componentProvider = componentProvider;
        this.uiContext = uiContext;
    }

    @Override
    protected Field<Object> buildField() {

        final String aclName = "acl_" + getFieldDefinition().getWorkspace();

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        try {

            final JcrNodeAdapter roleItem = (JcrNodeAdapter) item;
            Node roleNode = roleItem.getJcrItem();

            final VerticalLayout aclLayout = new VerticalLayout();

            final Label emptyLabel = new Label("No access.");

            if (roleNode.hasNode(aclName)) {

                final Node aclNode = roleNode.getNode(aclName);
                AbstractJcrNodeAdapter aclItem = new JcrNodeAdapter(aclNode);
                roleItem.addChild(aclItem);

                for (Node entryNode : NodeUtil.getNodes(aclNode)) {

                    AbstractJcrNodeAdapter entryItem = new JcrNodeAdapter(entryNode);
                    aclItem.addChild(entryItem);

                    Component ruleRow = createRuleRow(aclLayout, entryItem, emptyLabel);
                    aclLayout.addComponent(ruleRow);
                }
            }

            if (aclLayout.getComponentCount() == 0) {
                aclLayout.addComponent(emptyLabel);
            }

            final HorizontalLayout buttons = new HorizontalLayout();
            final Button addButton = new Button("Add new");
            addButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        JcrNewNodeAdapter newItem = addAclEntryItem(roleItem, aclName);
                        Component ruleRow = createRuleRow(aclLayout, newItem, emptyLabel);
                        aclLayout.removeComponent(emptyLabel);
                        aclLayout.addComponent(ruleRow, aclLayout.getComponentCount() - 1);
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                }
            });
            buttons.addComponent(addButton);
            aclLayout.addComponent(buttons);

            layout.addComponent(aclLayout);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        return new CustomField<Object>() {

            @Override
            protected Component initContent() {
                return layout;
            }

            @Override
            public Class<?> getType() {
                return Object.class;
            }
        };
    }

    protected Component createRuleRow(final AbstractOrderedLayout parentContainer, final AbstractJcrNodeAdapter ruleItem, final Label emptyLabel) {

        final HorizontalLayout ruleLayout = new HorizontalLayout();
        ruleLayout.setSpacing(true);

        NativeSelect select = new NativeSelect();
        select.addItem(63L);
        select.setItemCaption(63L, "Read/Write");
        select.addItem(8L);
        select.setItemCaption(8L, "Read-only");
        select.addItem(0L);
        select.setItemCaption(0L, "Deny access");
        select.setNullSelectionAllowed(false);
        select.setImmediate(true);
        select.setInvalidAllowed(false);
        select.setNewItemsAllowed(false);
        Property permissionsProperty = ruleItem.getItemProperty(PERMISSIONS_PROPERTY_NAME);
        if (permissionsProperty == null) {
            permissionsProperty = new DefaultProperty<Long>(Long.class, 63L);
            ruleItem.addItemProperty(PERMISSIONS_PROPERTY_NAME, permissionsProperty);
        }
        select.setPropertyDataSource(permissionsProperty);
        ruleLayout.addComponent(select);

        NativeSelect select2 = new NativeSelect();
        ruleLayout.addComponent(select2);

        final TextField textField = new TextField();
        textField.setWidth("150px");
        Property pathProperty = ruleItem.getItemProperty(PATH_PROPERTY_NAME);
        if (pathProperty == null) {
            pathProperty = new DefaultProperty<String>(String.class, "/*");
            ruleItem.addItemProperty(PATH_PROPERTY_NAME, pathProperty);
        }
        textField.setPropertyDataSource(pathProperty);
        ruleLayout.addComponent(textField);

        Button chooseButton = new Button();
        chooseButton.addStyleName("inline");
        chooseButton.setCaption("Choose...");
        chooseButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                openChooseDialog(textField);
            }
        });
        ruleLayout.addComponent(chooseButton);

        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription("Delete");
        deleteButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parentContainer.removeComponent(ruleLayout);
                ruleItem.getParent().removeChild(ruleItem);
                if (parentContainer.getComponentCount() == 1) {
                    parentContainer.addComponent(emptyLabel, 0);
                }
            }
        });
        ruleLayout.addComponent(deleteButton);


        return ruleLayout;
    }

    protected void openChooseDialog(final TextField textField) {

        final ItemChosenListener listener = new ItemChosenListener() {

            @Override
            public void onItemChosen(Item item) {
                try {
                    textField.setValue(((AbstractJcrNodeAdapter)item).getJcrItem().getPath());
                } catch (RepositoryException e) {
                    log.error("Failed to read chosen node", e);
                }
            }

            @Override
            public void onChooseCanceled() {
            }
        };

        final WorkbenchChooseDialogPresenter workbenchChooseDialogPresenter = componentProvider.newInstance(WorkbenchChooseDialogPresenter.class);
        workbenchChooseDialogPresenter.setWorkbenchDefinition(resolveWorkbenchDefinition());

        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.COMMIT_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onItemChosen(workbenchChooseDialogPresenter.getValue());
            }
        });

        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onChooseCanceled();
            }
        });

        ChooseDialogView chooseDialogView = workbenchChooseDialogPresenter.start();
        chooseDialogView.setCaption(StringUtils.capitalize(getFieldDefinition().getWorkspace()));

        final OverlayCloser overlayCloser = uiContext.openOverlay(chooseDialogView);

        workbenchChooseDialogPresenter.setListener(new ChooseDialogPresenter.Listener() {

            @Override
            public void onClose() {
                overlayCloser.close();
            }
        });
    }

    protected WorkbenchDefinition resolveWorkbenchDefinition() {

        if (getFieldDefinition().getWorkbench() != null) {
            return getFieldDefinition().getWorkbench();
        }

        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(getFieldDefinition().getWorkspace());
        workbenchDefinition.setPath("/");
        workbenchDefinition.setDialogWorkbench(true);
        workbenchDefinition.setEditable(false);
        workbenchDefinition.setDefaultOrder(ModelConstants.JCR_NAME);

        // columns
        ArrayList<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
        PropertyColumnDefinition column = new PropertyColumnDefinition();
        column.setEditable(false);
        column.setDisplayInChooseDialog(true);
        column.setLabel("Node name");
        column.setPropertyName(ModelConstants.JCR_NAME);
        column.setName(ModelConstants.JCR_NAME);
        columns.add(column);
        workbenchDefinition.setColumns(columns);

        // node types
        workbenchDefinition.setNodeTypes(resolveNodeTypes());

        // content views
        ArrayList<ContentPresenterDefinition> contentViews = new ArrayList<ContentPresenterDefinition>();
        contentViews.add(new TreePresenterDefinition());
        workbenchDefinition.setContentViews(contentViews);
        return workbenchDefinition;
    }

    private List<NodeTypeDefinition> resolveNodeTypes() {

        if (getFieldDefinition().getNodeTypes() != null) {
            return getFieldDefinition().getNodeTypes();
        }

        ArrayList<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();
        ConfiguredNodeTypeDefinition nodeType = new ConfiguredNodeTypeDefinition();
        nodeType.setName("nt:base");
        nodeType.setIcon("icon-folder");
        nodeType.setStrict(false);
        nodeTypes.add(nodeType);

        return nodeTypes;
    }
}
