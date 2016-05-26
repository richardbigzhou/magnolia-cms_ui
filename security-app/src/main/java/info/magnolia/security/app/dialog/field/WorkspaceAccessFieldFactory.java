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
package info.magnolia.security.app.dialog.field;

import info.magnolia.cms.security.Permission;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogComponentProviderUtil;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
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
 * final format by {@link info.magnolia.security.app.dialog.action.SaveRoleDialogAction}.
 *
 * @param <D> definition type
 * @see WorkspaceAccessFieldDefinition
 * @see info.magnolia.security.app.dialog.action.SaveRoleDialogAction
 */
public class WorkspaceAccessFieldFactory<D extends WorkspaceAccessFieldDefinition> extends AbstractAccessFieldFactory<D> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessFieldFactory.class);

    public static final String INTERMEDIARY_FORMAT_PROPERTY_NAME = "__intermediary_format";
    public static final String ACCESS_TYPE_PROPERTY_NAME = "accessType";

    private final UiContext uiContext;
    private final SimpleTranslator i18n;
    private final ComponentProvider componentProvider;
    private ChooseDialogPresenter workbenchChooseDialogPresenter;

    @Inject
    public WorkspaceAccessFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.uiContext = uiContext;
        this.workbenchChooseDialogPresenter = workbenchChooseDialogPresenter;
        this.i18n = i18n;
        this.componentProvider = componentProvider;
    }

    /**
     * @deprecated since 5.4.7 - use {@link #WorkspaceAccessFieldFactory(WorkspaceAccessFieldDefinition, Item, UiContext, I18NAuthoringSupport, ChooseDialogPresenter, SimpleTranslator, ComponentProvider)} instead.
     */
    @Deprecated
    public WorkspaceAccessFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n, ComponentProvider componentProvider) {
        this(definition, relatedFieldItem, uiContext, componentProvider.getComponent(I18NAuthoringSupport.class), workbenchChooseDialogPresenter, i18n, componentProvider);
    }

    /**
     * @deprecated since 5.3.1. {@link ComponentProvider} has to be injected in order to create the choose-dialog specific component provider, with proper bindings for e.g. {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector} or {@link info.magnolia.ui.imageprovider.ImageProvider}.
     */
    @Deprecated
    public WorkspaceAccessFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n) {
        this(definition, relatedFieldItem, uiContext, Components.getComponent(I18NAuthoringSupport.class), workbenchChooseDialogPresenter, i18n, Components.getComponentProvider());
    }

    @Override
    protected Field<Object> createFieldComponent() {

        final String aclName = "acl_" + getFieldDefinition().getWorkspace();

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        try {

            final JcrNodeAdapter roleItem = (JcrNodeAdapter) item;

            final VerticalLayout aclLayout = new VerticalLayout();

            final Label emptyLabel = new Label(i18n.translate("security.workspace.field.noAccess"));

            // Since JcrNewNodeAdapter.getJcrItem() returns the parent node we need to skip this step because we don't want to inspect the parent node
            if (!(roleItem instanceof JcrNewNodeAdapter)) {
                Node roleNode = roleItem.getJcrItem();
                if (roleNode.hasNode(aclName)) {

                    final Node aclNode = roleNode.getNode(aclName);

                    AccessControlList acl = new AccessControlList();
                    acl.readEntries(aclNode);

                    AbstractJcrNodeAdapter aclItem = new JcrNodeAdapter(aclNode);
                    roleItem.addChild(aclItem);

                    aclItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));

                    final Set<AccessControlList.Entry> uniqueEntries = new HashSet<AccessControlList.Entry>();
                    for (final Node aclEntryNode : NodeUtil.getNodes(aclNode)) {
                        AccessControlList.Entry entry = acl.getEntryByNode(aclEntryNode);
                        if (uniqueEntries.contains(entry)) {
                            continue;
                        }

                        uniqueEntries.add(entry);
                        long permissions = entry.getPermissions();
                        long accessType = entry.getAccessType();
                        String path = entry.getPath();

                        JcrNodeAdapter entryItem = new JcrNodeAdapter(aclEntryNode);
                        entryItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
                        final Property<Long> permissionsProperty = DefaultPropertyUtil.newDefaultProperty(Long.class, null);
                        entryItem.addItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME, permissionsProperty);
                        final Property<Long> accessProperty = DefaultPropertyUtil.newDefaultProperty(Long.class, null);
                        entryItem.addItemProperty(ACCESS_TYPE_PROPERTY_NAME, accessProperty);
                        final Property<String> pathProperty = DefaultPropertyUtil.newDefaultProperty(String.class, null);
                        entryItem.addItemProperty(AccessControlList.PATH_PROPERTY_NAME, pathProperty);

                        permissionsProperty.setValue(permissions);
                        accessProperty.setValue(accessType);
                        pathProperty.setValue(path);

                        aclItem.addChild(entryItem);

                        Component ruleRow = createRuleRow(aclLayout, entryItem, emptyLabel);
                        aclLayout.addComponent(ruleRow);
                    }
                }
            }

            if (aclLayout.getComponentCount() == 0) {
                aclLayout.addComponent(emptyLabel);
            }

            final HorizontalLayout buttons = new HorizontalLayout();
            final Button addButton = new Button(i18n.translate("security.workspace.field.addButton"));
            addButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {

                        AbstractJcrNodeAdapter aclItem = getOrAddAclItem(roleItem, aclName);
                        if (aclItem.getItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME) == null) {
                            aclItem.addItemProperty(INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
                        }

                        JcrNodeAdapter entryItem = addAclEntryItem(aclItem);
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
        ruleLayout.setWidth("100%");

        NativeSelect accessRights = new NativeSelect();
        accessRights.setNullSelectionAllowed(false);
        accessRights.setImmediate(true);
        accessRights.setInvalidAllowed(false);
        accessRights.setNewItemsAllowed(false);
        accessRights.addItem(Permission.ALL);
        accessRights.setItemCaption(Permission.ALL, i18n.translate("security.workspace.field.readWrite"));
        accessRights.addItem(Permission.READ);
        accessRights.setItemCaption(Permission.READ, i18n.translate("security.workspace.field.readOnly"));
        accessRights.addItem(Permission.NONE);
        accessRights.setItemCaption(Permission.NONE, i18n.translate("security.workspace.field.denyAccess"));
        accessRights.setPropertyDataSource(ruleItem.getItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME));
        ruleLayout.addComponent(accessRights);

        NativeSelect accessType = new NativeSelect();
        accessType.setNullSelectionAllowed(false);
        accessType.setImmediate(true);
        accessType.setInvalidAllowed(false);
        accessType.setNewItemsAllowed(false);
        accessType.setWidth("150px");
        accessType.addItem(AccessControlList.ACCESS_TYPE_NODE);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_NODE, i18n.translate("security.workspace.field.selected"));
        accessType.addItem(AccessControlList.ACCESS_TYPE_CHILDREN);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_CHILDREN, i18n.translate("security.workspace.field.subnodes"));
        accessType.addItem(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);
        accessType.setItemCaption(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, i18n.translate("security.workspace.field.selectedSubnodes"));
        Property accessTypeProperty = ruleItem.getItemProperty(ACCESS_TYPE_PROPERTY_NAME);
        accessType.setPropertyDataSource(accessTypeProperty);
        ruleLayout.addComponent(accessType);

        final TextField path = new TextField();
        path.setWidth("100%");
        path.setPropertyDataSource(ruleItem.getItemProperty(AccessControlList.PATH_PROPERTY_NAME));
        ruleLayout.addComponent(path);
        ruleLayout.setExpandRatio(path, 1.0f);

        Button chooseButton = new Button(i18n.translate("security.workspace.field.choose"));
        chooseButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                openChooseDialog(path);
            }
        });
        ruleLayout.addComponent(chooseButton);

        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(i18n.translate("security.workspace.field.delete"));
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
        final ConfiguredChooseDialogDefinition def = new ConfiguredChooseDialogDefinition();
        final ConfiguredJcrContentConnectorDefinition contentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        contentConnectorDefinition.setWorkspace(getFieldDefinition().getWorkspace());
        contentConnectorDefinition.setRootPath("/");
        contentConnectorDefinition.setDefaultOrder(ModelConstants.JCR_NAME);
        // node types
        contentConnectorDefinition.setNodeTypes(resolveNodeTypes());
        def.setContentConnector(contentConnectorDefinition);

        final WorkbenchDefinition wbDef = resolveWorkbenchDefinition();
        final WorkbenchFieldDefinition fieldDef = new WorkbenchFieldDefinition();
        fieldDef.setWorkbench(wbDef);
        def.setField(fieldDef);

        // create chooseDialogComponentProvider and get new instance of presenter from there
        ComponentProvider chooseDialogComponentProvider = ChooseDialogComponentProviderUtil.createChooseDialogComponentProvider(def, componentProvider);
        workbenchChooseDialogPresenter = chooseDialogComponentProvider.newInstance(def.getPresenterClass(), chooseDialogComponentProvider);

        // Define selected ItemId

        ChooseDialogView chooseDialogView = workbenchChooseDialogPresenter.start(new ChooseDialogCallback() {
            @Override
            public void onItemChosen(String actionName, Object value) {
                try {
                    if (value instanceof JcrItemId) {
                        JcrItemId jcrItemId = (JcrItemId) value;
                        textField.setValue(JcrItemUtil.getJcrItem(jcrItemId).getPath());
                    } else {
                        textField.setValue("/");
                    }
                } catch (RepositoryException e) {
                    log.error("Failed to read chosen node", e);
                }
            }

            @Override
            public void onCancel() {
            }
        }, def, uiContext, textField.getValue());
        chooseDialogView.setCaption(StringUtils.capitalize(getFieldDefinition().getWorkspace()));
    }

    protected WorkbenchDefinition resolveWorkbenchDefinition() {

        if (getFieldDefinition().getWorkbench() != null) {
            return getFieldDefinition().getWorkbench();
        }

        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setDialogWorkbench(true);
        workbenchDefinition.setEditable(false);


        // content views
        ArrayList<ContentPresenterDefinition> contentViews = new ArrayList<ContentPresenterDefinition>();
        TreePresenterDefinition treeView = new TreePresenterDefinition();
        ArrayList<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
        PropertyColumnDefinition column = new PropertyColumnDefinition();
        column.setEditable(false);
        column.setDisplayInChooseDialog(true);
        column.setLabel(i18n.translate("security.workspace.field.nodeName"));
        column.setPropertyName(ModelConstants.JCR_NAME);
        column.setName(ModelConstants.JCR_NAME);
        columns.add(column);
        treeView.setColumns(columns);
        contentViews.add(treeView);
        workbenchDefinition.setContentViews(contentViews);

        return workbenchDefinition;
    }

    private List<NodeTypeDefinition> resolveNodeTypes() {

        if (getFieldDefinition().getNodeTypes() != null) {
            return getFieldDefinition().getNodeTypes();
        }

        ArrayList<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();
        ConfiguredNodeTypeDefinition nodeType = new ConfiguredNodeTypeDefinition();
        nodeType.setName(JcrConstants.NT_BASE);
        nodeType.setIcon("icon-folder");
        nodeTypes.add(nodeType);

        return nodeTypes;
    }

}
