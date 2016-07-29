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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * A field implementation dedicated to access control lists. In effect, this is a multi-value field
 * for {@link AccessControlField AccessControlFields}, supporting addition and removal of entries.
 *
 * <p>New entries are created through the </p>
 */
public class AccessControlListField extends CustomField<AccessControlList> {
    private final Map<Long, String> permissions;
    private final NewEntryHandler newEntryHandler;

    private VerticalLayout layout;
    private List<Field<AccessControlList.Entry>> entryFields = new ArrayList<>();
    private ValueChangeListener valueChangeListener;

    private String addButtonCaption = "Add";
    private String removeButtonCaption = "Remove";
    private String emptyPlaceholderCaption = "No access";
    private EntryFieldFactory entryFieldFactory;

    /**
     * Creates an AccessControlListField with the given set of permissions.
     *
     * @param permissions a map whose keys are permission values, and whose values are the corresponding captions to display in the permission select.
     * @param newEntryHandler the handler creating actual entries with default values, to append to this field.
     */
    public AccessControlListField(Map<Long, String> permissions, NewEntryHandler newEntryHandler) {
        this.permissions = permissions;
        this.newEntryHandler = newEntryHandler;
    }

    @Override
    protected Component initContent() {
        layout = new VerticalLayout();
        layout.setSpacing(false);

        AccessControlList<AccessControlList.Entry> acl = getValue();
        List<Component> entryRows = buildEntryRows(acl);
        layout.addComponents(entryRows.toArray(new Component[0]));

        if (acl.getEntries().isEmpty()) {
            layout.addComponent(new Label(emptyPlaceholderCaption));
        }

        if (newEntryHandler != null) {
            final Button addButton = new Button(addButtonCaption);
            addButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    AccessControlList.Entry entry = newEntryHandler.createEntry();
                    AccessControlList<AccessControlList.Entry> value = getValue();
                    if (value == null) {
                        value = new AccessControlList<>();
                        setValue(value);
                    }
                    value.addEntry(entry);

                    Field<AccessControlList.Entry> entryField = getEntryFieldFactory().createField(entry);
                    // set validation visibility from parent field
                    if (entryField instanceof AbstractField) {
                        ((AbstractField) entryField).setValidationVisible(isValidationVisible());
                    }

                    // update layout
                    entryFields.add(entryField);
                    layout.addComponent(buildEntryRow(entryField), layout.getComponentCount() - 1);
                    if (layout.getComponent(0) instanceof Label) {
                        layout.removeComponent(layout.getComponent(0));
                    }
                }
            });
            layout.addComponent(addButton);
        }
        return layout;
    }

    protected List<Component> buildEntryRows(AccessControlList<AccessControlList.Entry> acl) {
        List<Component> entryRows = new ArrayList<>();
        if (acl != null) {
            for (final AccessControlList.Entry entry : acl.getEntries()) {
                Field<AccessControlList.Entry> entryField = getEntryFieldFactory().createField(entry);
                // set validation visibility from parent field
                if (entryField instanceof AbstractField) {
                    ((AbstractField) entryField).setValidationVisible(isValidationVisible());
                }

                entryFields.add(entryField);
                entryRows.add(buildEntryRow(entryField));
            }
        }
        return entryRows;
    }

    protected Component buildEntryRow(final Field<AccessControlList.Entry> entryField) {
        final HorizontalLayout entryRow = new HorizontalLayout();
        entryRow.setSpacing(true);
        entryRow.setWidth("100%");

        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(removeButtonCaption);
        deleteButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                AccessControlList<AccessControlList.Entry> acl = getValue();
                acl.removeEntry(entryField.getValue());
                entryFields.remove(entryField);
                if (isValidationVisible()) {
                    // make sure to re-validate if problematic entry is removed
                    fireValueChange(false);
                }

                // update layout
                layout.removeComponent(entryRow);
                if (acl.getEntries().size() == 0) {
                    layout.addComponentAsFirst(new Label(emptyPlaceholderCaption));
                }
            }
        });

        entryRow.addComponents(entryField, deleteButton);
        entryRow.setExpandRatio(entryField, 1f);
        return entryRow;
    }

    public String getAddButtonCaption() {
        return addButtonCaption;
    }

    public void setAddButtonCaption(String addButtonCaption) {
        this.addButtonCaption = addButtonCaption;
    }

    public String getRemoveButtonCaption() {
        return removeButtonCaption;
    }

    public void setRemoveButtonCaption(String removeButtonCaption) {
        this.removeButtonCaption = removeButtonCaption;
    }

    public String getEmptyPlaceholderCaption() {
        return emptyPlaceholderCaption;
    }

    public void setEmptyPlaceholderCaption(String emptyPlaceholderCaption) {
        this.emptyPlaceholderCaption = emptyPlaceholderCaption;
    }

    @Override
    public Class<AccessControlList> getType() {
        return AccessControlList.class;
    }

    @Override
    public AccessControlList<AccessControlList.Entry> getValue() {
        return super.getValue();
    }

    @Override
    protected void validate(AccessControlList fieldValue) throws Validator.InvalidValueException {
        List<Validator.InvalidValueException> causes = new ArrayList<>();
        // first invoke self-validation
        try {
            super.validate(fieldValue);
        } catch (Validator.InvalidValueException e) {
            causes.add(e);
        }

        // then validate individual entries
        for (Field<AccessControlList.Entry> entryField : entryFields) {
            try {
                entryField.validate();
            } catch (Validator.InvalidValueException e) {
                causes.add(e);
            }
        }

        if (!causes.isEmpty()) {
            // until we display errors on sub-fields, display causes' messages by passing null in first arg
            // see AbstractErrorMessage#getFormattedHtmlMessage
            throw new Validator.InvalidValueException(null, causes.toArray(new Validator.InvalidValueException[0]));
        }
    }

    /*
     * Make sure to propagate inner value changes when validation is visible (i.e. field is invalid);
     * so that corrected values are validated on the fly and eventually remove validation marks.
     */
    @Override
    public void setValidationVisible(boolean validateAutomatically) {
        super.setValidationVisible(validateAutomatically);
        for (Field<?> entryField : entryFields) {
            if (entryField instanceof AbstractField) {
                ((AbstractField) entryField).setValidationVisible(isValidationVisible());
            }
        }

        if (validateAutomatically && valueChangeListener == null) {
            valueChangeListener = new ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    fireValueChange(false);
                }
            };
            for (Field<?> entryField : entryFields) {
                entryField.addValueChangeListener(valueChangeListener);
            }
        } else if (!validateAutomatically && valueChangeListener != null) {
            for (Field<?> entryField : entryFields) {
                entryField.removeValueChangeListener(valueChangeListener);
            }
            valueChangeListener = null;
        }
    }

    public EntryFieldFactory getEntryFieldFactory() {
        if (entryFieldFactory == null) {
            this.entryFieldFactory = new DefaultEntryFieldFactory();
        }
        return entryFieldFactory;
    }

    public void setEntryFieldFactory(EntryFieldFactory entryFieldFactory) {
        this.entryFieldFactory = entryFieldFactory;
    }

    /**
     * The handler used to create new ACL entries.
     */
    public interface NewEntryHandler {
        /**
         * Creates and returns a new {@link AccessControlList.Entry Entry} to feed into this field's {@linkplain AccessControlList acl} value.
         */
        AccessControlList.Entry createEntry();
    }

    /**
     * A factory creating individual fields for ACL entries.
     */
    public interface EntryFieldFactory {
        /**
         * Creates a {@link Field} and binds the given {@link AccessControlList.Entry Entry} to it.
         */
        Field<AccessControlList.Entry> createField(AccessControlList.Entry entry);
    }

    /**
     * Default implementation creates a standard AccessControlField with validator.
     */
    public class DefaultEntryFieldFactory implements EntryFieldFactory {
        @Override
        public Field<AccessControlList.Entry> createField(AccessControlList.Entry entry) {
            AccessControlField entryField = new AccessControlField(permissions);
            entryField.setPropertyDataSource(new ObjectProperty<>(entry));
            return entryField;
        }
    }
}
