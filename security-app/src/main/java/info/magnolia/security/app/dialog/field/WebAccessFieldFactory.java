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
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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
 * Field builder for the web access field.
 *
 * @param <D> definition type
 * @see WebAccessFieldDefinition
 */
public class WebAccessFieldFactory<D extends WebAccessFieldDefinition> extends AbstractAccessFieldFactory<D> {

    private static final String ACL_NODE_NAME = "acl_uri";
    private static final String PERMISSIONS_PROPERTY_NAME = "permissions";
    private static final String PATH_PROPERTY_NAME = "path";

    private final SimpleTranslator i18n;

    public WebAccessFieldFactory(D definition, Item relatedFieldItem, SimpleTranslator i18n) {
        super(definition, relatedFieldItem);
        this.i18n = i18n;
    }

    @Override
    protected Field<Object> createFieldComponent() {

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        try {

            final JcrNodeAdapter roleItem = (JcrNodeAdapter) item;
            Node roleNode = roleItem.getJcrItem();

            final VerticalLayout aclLayout = new VerticalLayout();

            final Label emptyLabel = new Label(i18n.translate("security.web.field.noAccess"));

            if (roleNode.hasNode(ACL_NODE_NAME)) {

                final Node aclNode = roleNode.getNode(ACL_NODE_NAME);
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
            final Button addButton = new Button(i18n.translate("security.web.field.addNew"));
            addButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    try {
                        AbstractJcrNodeAdapter aclItem = getOrAddAclItem(roleItem, ACL_NODE_NAME);
                        JcrNewNodeAdapter newItem = addAclEntryItem(aclItem);
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

    private Component createRuleRow(final AbstractOrderedLayout parentContainer, final AbstractJcrNodeAdapter ruleItem, final Label emptyLabel) {

        final HorizontalLayout ruleLayout = new HorizontalLayout();
        ruleLayout.setSpacing(true);

        NativeSelect accessRights = new NativeSelect();
        accessRights.addItem(Permission.ALL);
        accessRights.setItemCaption(Permission.ALL, i18n.translate("security.web.field.getPost"));
        accessRights.addItem(Permission.READ);
        accessRights.setItemCaption(Permission.READ, i18n.translate("security.web.field.get"));
        accessRights.addItem(Permission.NONE);
        accessRights.setItemCaption(Permission.NONE, i18n.translate("security.web.field.deny"));
        accessRights.setNullSelectionAllowed(false);
        accessRights.setImmediate(true);
        accessRights.setInvalidAllowed(false);
        accessRights.setNewItemsAllowed(false);
        Property permissionsProperty = ruleItem.getItemProperty(PERMISSIONS_PROPERTY_NAME);
        if (permissionsProperty == null) {
            permissionsProperty = new DefaultProperty<Long>(Long.class, Permission.ALL);
            ruleItem.addItemProperty(PERMISSIONS_PROPERTY_NAME, permissionsProperty);
        }
        accessRights.setPropertyDataSource(permissionsProperty);
        ruleLayout.addComponent(accessRights);

        TextField path = new TextField();
        path.setWidth("375px");
        Property pathProperty = ruleItem.getItemProperty(PATH_PROPERTY_NAME);
        if (pathProperty == null) {
            pathProperty = new DefaultProperty<String>(String.class, "/*");
            ruleItem.addItemProperty(PATH_PROPERTY_NAME, pathProperty);
        }
        path.setPropertyDataSource(pathProperty);
        ruleLayout.addComponent(path);

        final Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(i18n.translate("security.web.field.delete"));
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
}
