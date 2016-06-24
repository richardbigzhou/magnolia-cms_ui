/**
 * This file Copyright (c) 2016 Magnolia International
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

import static com.vaadin.server.Sizeable.Unit.*;

import java.util.Map;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

/**
 * A field implementation dedicated to access control entries (permission, access type and path).
 *
 * <p>Support for the access type, as well as chooser pluggability are optional (these only apply to workspace ACLs, not URI ACLs).
 */
public class AccessControlField extends CustomField<AccessControlList.Entry> {

    private NativeSelect permissionSelect = new NativeSelect();
    private NativeSelect accessTypeSelect;
    private TextField path = new TextField();
    private FieldGroup fieldGroup = new BeanFieldGroup<>(AccessControlList.Entry.class);

    private Map<Long, String> permissions;
    private Map<Long, String> accessTypes;
    private String chooseButtonCaption = "Choose";
    private PathChooserHandler pathChooserHandler;
    private ValueChangeListener valueChangeListener;

    /**
     * Creates an AccessControlField with a permission select and a path text-field.
     *
     * @param permissions a map whose keys are permission values, and whose values are the corresponding captions to display in the permission select.
     */
    public AccessControlField(Map<Long, String> permissions) {
        this(permissions, null);
    }

    /**
     * Creates an AccessControlField with a permission type select, access-type select and a path text-field.
     *
     * @param permissions a map whose keys are permission values, and whose values are the corresponding captions to display in the permission select.
     * @param accessTypes a map whose keys are access type values, and whose values are the corresponding captions to display in the access type select. In the case of the web access field, this param can be ignored.
     */
    public AccessControlField(Map<Long, String> permissions, Map<Long, String> accessTypes) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("AccessControlField requires a non-empty collection of permission entries.");
        }
        this.permissions = permissions;
        this.accessTypes = accessTypes;
    }

    @Override
    protected Component initContent() {

        // init fields
        permissionSelect.setNullSelectionAllowed(false);
        permissionSelect.setImmediate(true);
        permissionSelect.setInvalidAllowed(false);
        permissionSelect.setNewItemsAllowed(false);
        for (Map.Entry<Long, String> entry : permissions.entrySet()) {
            permissionSelect.addItem(entry.getKey());
            permissionSelect.setItemCaption(entry.getKey(), entry.getValue());
        }

        if (accessTypes != null && !accessTypes.isEmpty()) {
            accessTypeSelect = new NativeSelect();
            accessTypeSelect.setNullSelectionAllowed(false);
            accessTypeSelect.setImmediate(true);
            accessTypeSelect.setInvalidAllowed(false);
            accessTypeSelect.setNewItemsAllowed(false);
            accessTypeSelect.setWidth(150, PIXELS);
            for (Map.Entry<Long, String> entry : accessTypes.entrySet()) {
                accessTypeSelect.addItem(entry.getKey());
                accessTypeSelect.setItemCaption(entry.getKey(), entry.getValue());
            }
        }

        path.setWidth(100, PERCENTAGE);

        // bind fields + layout
        HorizontalLayout ruleLayout = new HorizontalLayout();
        ruleLayout.setSpacing(true);
        ruleLayout.setWidth("100%");

        fieldGroup.bind(permissionSelect, "permissions");
        ruleLayout.addComponent(permissionSelect);

        if (accessTypeSelect != null) {
            fieldGroup.bind(accessTypeSelect, "accessType");
            ruleLayout.addComponent(accessTypeSelect);
        }

        fieldGroup.bind(path, "path");
        ruleLayout.addComponent(path);
        ruleLayout.setExpandRatio(path, 1.0f);

        if (pathChooserHandler != null) {
            Button chooseButton = new Button(chooseButtonCaption);
            chooseButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    pathChooserHandler.openChooser(path.getPropertyDataSource());
                }
            });
            ruleLayout.addComponent(chooseButton);
        }

        // make sure to propagate value changes to the underlying item without requiring a fieldGroup commit
        fieldGroup.setBuffered(false);

        return ruleLayout;
    }

    @Override
    public Class<? extends AccessControlList.Entry> getType() {
        return AccessControlList.Entry.class;
    }

    @Override
    protected void setInternalValue(AccessControlList.Entry newValue) {
        super.setInternalValue(newValue);
        fieldGroup.setItemDataSource(new BeanItem<>(newValue));
    }

    /*
     * Make sure to propagate inner value changes when validation is visible (i.e. field is invalid);
     * so that corrected values are validated on the fly and eventually remove validation marks.
     */
    @Override
    public void setValidationVisible(boolean validateAutomatically) {
        super.setValidationVisible(validateAutomatically);

        if (validateAutomatically && valueChangeListener == null) {
            valueChangeListener = new ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    fireValueChange(false);
                }
            };
            for (Field<?> field : fieldGroup.getFields()) {
                field.addValueChangeListener(valueChangeListener);
            }
        } else if (!validateAutomatically && valueChangeListener != null) {
            for (Field<?> field : fieldGroup.getFields()) {
                field.removeValueChangeListener(valueChangeListener);
            }
            valueChangeListener = null;
        }
    }

    public String getChooseButtonCaption() {
        return chooseButtonCaption;
    }

    public void setChooseButtonCaption(String chooseButtonCaption) {
        this.chooseButtonCaption = chooseButtonCaption;
    }

    public PathChooserHandler getPathChooserHandler() {
        return pathChooserHandler;
    }

    public void setPathChooserHandler(PathChooserHandler pathChooserHandler) {
        this.pathChooserHandler = pathChooserHandler;
    }

    /**
     * A hook to the path field for the current entry, in order to choose and update its path.
     */
    public interface PathChooserHandler {

        /**
         * Invoked with the current entry's path property to use as a default value.
         * Implementations are expected to set the value back to this property when done.
         */
        void openChooser(Property<String> pathProperty);
    }
}
