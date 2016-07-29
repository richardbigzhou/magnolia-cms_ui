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

import static info.magnolia.security.app.dialog.field.AccessControlListField.NewEntryHandler;

import info.magnolia.cms.security.Permission;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.objectfactory.Components;
import info.magnolia.security.app.dialog.field.AccessControlListField.EntryFieldFactory;
import info.magnolia.security.app.dialog.field.validator.WebAccessControlValidator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.google.common.collect.ImmutableMap;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Field;

/**
 * Field factory for the web access ACL field; unlike other field factories, it does not read ACLs straight from the JCR adapter.
 *
 * <p>First, reading and saving entries from/to the role node is delegated to an {@link AccessControlList}.
 * This typed ACL is then carried over as a property of the dialog item to the save action,
 * where it gets removed from the item, not to interfere with the JCR adapter.
 *
 * @see WebAccessFieldDefinition
 * @see info.magnolia.security.app.dialog.action.SaveRoleDialogAction
 */
public class WebAccessFieldFactory extends AbstractAccessFieldFactory<WebAccessFieldDefinition, AccessControlList> {

    private static final String ACL_NODE_NAME = "acl_uri";
    private final SimpleTranslator i18n;

    @Inject
    public WebAccessFieldFactory(WebAccessFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, SimpleTranslator i18n) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.i18n = i18n;
    }

    /**
     * @deprecated since 5.4.7 - use {@link #WebAccessFieldFactory(WebAccessFieldDefinition, Item, UiContext, I18NAuthoringSupport, SimpleTranslator)} instead.
     */
    @Deprecated
    public WebAccessFieldFactory(WebAccessFieldDefinition definition, Item relatedFieldItem, SimpleTranslator i18n) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class), i18n);
    }

    @Override
    protected Field<AccessControlList> createFieldComponent() {
        final Map<Long, String> permissionItems = ImmutableMap.of(
                Permission.ALL, i18n.translate("security.web.field.getPost"),
                Permission.READ, i18n.translate("security.web.field.get"),
                Permission.NONE, i18n.translate("security.web.field.deny"));

        final String validatorErrorMessage = i18n.translate("security-app.role.web.errorMessage");

        AccessControlListField aclField = new AccessControlListField(permissionItems, new NewEntryHandler() {
            @Override
            public AccessControlList.Entry createEntry() {
                return new AccessControlList.Entry(Permission.ALL, "/*");
            }
        });
        aclField.setEntryFieldFactory(new EntryFieldFactory() {
            @Override
            public Field<AccessControlList.Entry> createField(AccessControlList.Entry entry) {
                AccessControlField entryField = new AccessControlField(permissionItems);
                entryField.setPropertyDataSource(new ObjectProperty<>(entry));
                entryField.addValidator(new WebAccessControlValidator(validatorErrorMessage));
                return entryField;
            }
        });

        aclField.setAddButtonCaption(i18n.translate("security.web.field.addNew"));
        aclField.setRemoveButtonCaption(i18n.translate("security.web.field.delete"));
        aclField.setEmptyPlaceholderCaption(i18n.translate("security.web.field.noAccess"));

        return aclField;
    }

    @Override
    protected Property<AccessControlList> initializeProperty() {
        // prepare backing AccessControlList bean
        JcrNodeAdapter roleItem = (JcrNodeAdapter) item;
        AccessControlList<AccessControlList.Entry> acl = new AccessControlList<>();
        roleItem.addItemProperty(ACL_NODE_NAME, new ObjectProperty<>(acl));

        // feed entries if role node exists
        if (!roleItem.isNew()) {
            try {
                Node roleNode = roleItem.getJcrItem();
                if (roleNode.hasNode(ACL_NODE_NAME)) {
                    Node aclNode = roleNode.getNode(ACL_NODE_NAME);
                    acl.readEntries(aclNode);
                }
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }

        return new ObjectProperty<AccessControlList>(acl);
    }
}
