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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.security.app.dialog.field.AccessControlListField.NewEntryHandler;
import info.magnolia.security.app.dialog.field.validator.WorkspaceAccessControlValidator;
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
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

/**
 * Field factory for workspace ACL fields; unlike other field factories, it does not read ACLs straight from the JCR adapter.
 *
 * <p>First, reading and saving entries from/to the role node is delegated to a {@link WorkspaceAccessControlList}.
 * This typed ACL is then carried over as a property of the dialog item to the save action,
 * where it gets removed from the item, not to interfere with the JCR adapter.
 *
 * @see WorkspaceAccessFieldDefinition
 * @see info.magnolia.security.app.dialog.action.SaveRoleDialogAction
 */
public class WorkspaceAccessFieldFactory extends AbstractAccessFieldFactory<WorkspaceAccessFieldDefinition, AccessControlList> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessFieldFactory.class);

    /**
     * @deprecated since 5.4.8, not used anymore now that fields operate over ACEs directly.
     */
    @Deprecated
    public static final String INTERMEDIARY_FORMAT_PROPERTY_NAME = "__intermediary_format";

    /**
     * @deprecated since 5.4.8, constant has been relocated to {@link WorkspaceAccessControlList#ACCESS_TYPE_PROPERTY_NAME}
     */
    @Deprecated
    public static final String ACCESS_TYPE_PROPERTY_NAME = WorkspaceAccessControlList.ACCESS_TYPE_PROPERTY_NAME;

    private final UiContext uiContext;
    private final SimpleTranslator i18n;
    private final ComponentProvider componentProvider;

    private final String workspace;
    private final String aclName;

    @Inject
    public WorkspaceAccessFieldFactory(WorkspaceAccessFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.uiContext = uiContext;
        this.i18n = i18n;
        this.componentProvider = componentProvider;

        workspace = definition.getWorkspace();
        aclName = "acl_" + workspace;
    }

    /**
     * @deprecated since 5.4.7 - use {@link #WorkspaceAccessFieldFactory(WorkspaceAccessFieldDefinition, Item, UiContext, I18NAuthoringSupport, ChooseDialogPresenter, SimpleTranslator, ComponentProvider)} instead.
     */
    @Deprecated
    public WorkspaceAccessFieldFactory(WorkspaceAccessFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n, ComponentProvider componentProvider) {
        this(definition, relatedFieldItem, uiContext, componentProvider.getComponent(I18NAuthoringSupport.class), workbenchChooseDialogPresenter, i18n, componentProvider);
    }

    /**
     * @deprecated since 5.3.1. {@link ComponentProvider} has to be injected in order to create the choose-dialog specific component provider, with proper bindings for e.g. {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector} or {@link info.magnolia.ui.imageprovider.ImageProvider}.
     */
    @Deprecated
    public WorkspaceAccessFieldFactory(WorkspaceAccessFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, ChooseDialogPresenter workbenchChooseDialogPresenter, SimpleTranslator i18n) {
        this(definition, relatedFieldItem, uiContext, Components.getComponent(I18NAuthoringSupport.class), workbenchChooseDialogPresenter, i18n, Components.getComponentProvider());
    }

    @Override
    protected Field<AccessControlList> createFieldComponent() {
        final Map<Long, String> permissionItems = ImmutableMap.of(
                Permission.ALL, i18n.translate("security.workspace.field.readWrite"),
                Permission.READ, i18n.translate("security.workspace.field.readOnly"),
                Permission.NONE, i18n.translate("security.workspace.field.denyAccess"));

        final Map<Long, String> accessTypeItems = ImmutableMap.of(
                WorkspaceAccessControlList.ACCESS_TYPE_NODE, i18n.translate("security.workspace.field.selected"),
                WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, i18n.translate("security.workspace.field.subnodes"),
                WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, i18n.translate("security.workspace.field.selectedSubnodes"));

        final String chooseCaption = i18n.translate("security.workspace.field.choose");

        AccessControlListField aclField = new AccessControlListField(permissionItems, new NewEntryHandler() {
            @Override
            public AccessControlList.Entry createEntry() {
                return new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "");
            }
        });

        final AccessControlField.PathChooserHandler pathChooserHandler = new AccessControlField.PathChooserHandler() {
            @Override
            public void openChooser(final Property<String> pathProperty) {
                openChooseDialog(pathProperty.getValue(), new ChooseDialogCallback() {
                    @Override
                    public void onItemChosen(String actionName, Object value) {
                        try {
                            String newPath = value instanceof JcrItemId ? JcrItemUtil.getJcrItem((JcrItemId) value).getPath() : "/";
                            pathProperty.setValue(newPath);
                        } catch (RepositoryException e) {
                            log.error("Failed to read chosen node", e);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        };

        aclField.setEntryFieldFactory(new AccessControlListField.EntryFieldFactory() {
            @Override
            public Field<AccessControlList.Entry> createField(AccessControlList.Entry entry) {
                AccessControlField entryField = new AccessControlField(permissionItems, accessTypeItems);
                entryField.setPropertyDataSource(new ObjectProperty<>(entry));
                entryField.setPathChooserHandler(pathChooserHandler);
                entryField.setChooseButtonCaption(chooseCaption);
                entryField.addValidator(new WorkspaceAccessControlValidator(definition.getWorkspace(), i18n.translate("security-app.role.acls.errorMessage")));
                return entryField;
            }
        });
        aclField.setAddButtonCaption(i18n.translate("security.workspace.field.addButton"));
        aclField.setRemoveButtonCaption(i18n.translate("security.workspace.field.delete"));
        aclField.setEmptyPlaceholderCaption(i18n.translate("security.workspace.field.noAccess"));

        return aclField;
    }

    @Override
    protected Property<AccessControlList> initializeProperty() {
        // prepare backing WorkspaceAccessControlList bean
        JcrNodeAdapter roleItem = (JcrNodeAdapter) item;
        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        roleItem.addItemProperty(aclName, new ObjectProperty<>(acl));

        if (!(roleItem.isNew())) {
            try {
                Node roleNode = roleItem.getJcrItem();
                if (roleNode.hasNode(aclName)) {
                    final Node aclNode = roleNode.getNode(aclName);
                    acl.readEntries(aclNode);

                }
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }

        return new ObjectProperty<AccessControlList>(acl);
    }

    /**
     * @deprecated since 5.4.8 - won't use anymore.
     */
    @Deprecated
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
        accessType.addItem(WorkspaceAccessControlList.ACCESS_TYPE_NODE);
        accessType.setItemCaption(WorkspaceAccessControlList.ACCESS_TYPE_NODE, i18n.translate("security.workspace.field.selected"));
        accessType.addItem(WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN);
        accessType.setItemCaption(WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, i18n.translate("security.workspace.field.subnodes"));
        accessType.addItem(WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);
        accessType.setItemCaption(WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, i18n.translate("security.workspace.field.selectedSubnodes"));
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

    /**
     * @deprecated since 5.4.8 - won't use anymore.
     */
    @Deprecated
    protected void openChooseDialog(final TextField textField) {
        openChooseDialog(textField.getValue(), new ChooseDialogCallback() {
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
        });
    }

    protected void openChooseDialog(String initialItemId, ChooseDialogCallback callback) {
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
        ChooseDialogPresenter workbenchChooseDialogPresenter = chooseDialogComponentProvider.newInstance(def.getPresenterClass(), chooseDialogComponentProvider);

        ChooseDialogView chooseDialogView = workbenchChooseDialogPresenter.start(callback, def, uiContext, initialItemId);
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
        ArrayList<ContentPresenterDefinition> contentViews = new ArrayList<>();
        TreePresenterDefinition treeView = new TreePresenterDefinition();
        ArrayList<ColumnDefinition> columns = new ArrayList<>();
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

        ArrayList<NodeTypeDefinition> nodeTypes = new ArrayList<>();
        ConfiguredNodeTypeDefinition nodeType = new ConfiguredNodeTypeDefinition();
        nodeType.setName(JcrConstants.NT_BASE);
        nodeType.setIcon("icon-folder");
        nodeTypes.add(nodeType);

        return nodeTypes;
    }
}
