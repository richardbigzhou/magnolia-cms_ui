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

import info.magnolia.cms.security.Permission;
import info.magnolia.jcr.RuntimeRepositoryException;
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
 * Field builder for the workspace ACL field.  Adds data to the item in an intermediary format that is transformed to the
 * final format by {@link info.magnolia.ui.app.security.dialog.action.SaveRoleDialogAction}.
 *
 * @param <D> definition type
 * @see WorkspaceAccessFieldDefinition
 * @see info.magnolia.ui.app.security.dialog.action.SaveRoleDialogAction
 */
public class WorkspaceAccessFieldBuilder<D extends WorkspaceAccessFieldDefinition> extends AbstractAccessFieldBuilder<D> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessFieldBuilder.class);

    public static final String INTERMEDIARY_FORMAT_PROPERTY_NAME = "__intermediary_format";
    public static final String ACCESS_TYPE_PROPERTY_NAME = "accessType";

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

                AccessControlList acl = new AccessControlList();
                acl.readEntries(aclNode);

                AbstractJcrNodeAdapter aclItem = new JcrNodeAdapter(aclNode);
                roleItem.addChild(aclItem);

                aclItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));

                for (AccessControlList.Entry entry : acl.getEntries()) {

                    long permissions = entry.getPermissions();
                    long accessType = entry.getAccessType();
                    String path = entry.getPath();

                    JcrNewNodeAdapter entryItem = addAclEntryItem(aclItem);
                    entryItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
                    entryItem.addItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME, new DefaultProperty<Long>(Long.class, permissions));
                    entryItem.addItemProperty(ACCESS_TYPE_PROPERTY_NAME, new DefaultProperty<Long>(Long.class, accessType));
                    entryItem.addItemProperty(AccessControlList.PATH_PROPERTY_NAME, new DefaultProperty<String>(String.class, path));

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

                        AbstractJcrNodeAdapter aclItem = getOrAddAclItem(roleItem, aclName);
                        if (aclItem.getItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME) == null) {
                            aclItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
                        }

                        JcrNewNodeAdapter entryItem = addAclEntryItem(aclItem);
                        entryItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
                        entryItem.addItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME, new DefaultProperty<Long>(Long.class, Permission.ALL));
                        entryItem.addItemProperty(ACCESS_TYPE_PROPERTY_NAME, new DefaultProperty<Long>(Long.class, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN));
                        entryItem.addItemProperty(AccessControlList.PATH_PROPERTY_NAME, new DefaultProperty<String>(String.class, ""));

                        Component ruleRow = createRuleRow(aclLayout, entryItem, emptyLabel);
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

        NativeSelect accessRights = new NativeSelect();
        accessRights.setNullSelectionAllowed(false);
        accessRights.setImmediate(true);
        accessRights.setInvalidAllowed(false);
        accessRights.setNewItemsAllowed(false);
        accessRights.addItem(Permission.ALL);
        accessRights.setItemCaption(Permission.ALL, "Read/Write");
        accessRights.addItem(Permission.READ);
        accessRights.setItemCaption(Permission.READ, "Read-only");
        accessRights.addItem(Permission.NONE);
        accessRights.setItemCaption(Permission.NONE, "Deny access");
        accessRights.setPropertyDataSource(ruleItem.getItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME));
        ruleLayout.addComponent(accessRights);

        NativeSelect accessType = new NativeSelect();
        accessType.setNullSelectionAllowed(false);
        accessType.setImmediate(true);
        accessType.setInvalidAllowed(false);
        accessType.setNewItemsAllowed(false);
        accessType.setWidth("150px");
        accessType.addItem(AccessControlList.ACCESS_TYPE_NODE);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_NODE, "Selected");
        accessType.addItem(AccessControlList.ACCESS_TYPE_CHILDREN);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_CHILDREN, "Sub nodes");
        accessType.addItem(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "Selected and sub nodes");
        Property accessTypeProperty = ruleItem.getItemProperty(ACCESS_TYPE_PROPERTY_NAME);
        accessType.setPropertyDataSource(accessTypeProperty);
        ruleLayout.addComponent(accessType);

        final TextField textField = new TextField();
        textField.setWidth("125px");
        textField.setPropertyDataSource(ruleItem.getItemProperty(AccessControlList.PATH_PROPERTY_NAME));
        ruleLayout.addComponent(textField);

        Button chooseButton = new Button("Choose...");
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
                    textField.setValue(((AbstractJcrNodeAdapter) item).getJcrItem().getPath());
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
        nodeTypes.add(nodeType);

        return nodeTypes;
    }
}
