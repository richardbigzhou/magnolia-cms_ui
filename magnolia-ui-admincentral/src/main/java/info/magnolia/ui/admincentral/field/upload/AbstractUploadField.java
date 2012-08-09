/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.upload;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.vaadin.easyuploads.FileBuffer;

import com.vaadin.data.Buffered;
import com.vaadin.data.Property;
import com.vaadin.data.Validatable;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;


/**
 * Basic implementation of {@link Field} for Upload.
 */
public abstract class AbstractUploadField extends CssLayout implements Field {


    public AbstractUploadField() {
    }

    /**
     * Refresh the current field/group of fields
     * contains in the root component.
     */
    public abstract void updateDisplay();

    public abstract FileBuffer getReceiver();

    /**
     * The tab order number of this field.
     */
    private int tabIndex = 0;
    /**
     * Are the invalid values allowed in fields ?
     */
    private boolean invalidAllowed = true;
    /**
     * Connected data-source.
     */
    private Property dataSource = null;
    /**
     * Auto commit mode.
     */
    private boolean writeTroughMode = true;

    /**
     * The list of validators.
     */
    private LinkedList<Validator> validators = null;
    /**
     * The error message for the exception that is thrown when the field is
     * required but empty.
     */
    private String requiredError = "";
    /**
     * Required field.
     */
    private boolean required = false;

    /**
     * Is the field modified but not committed.
     */
    private boolean modified = false;

    /**
     * Current source exception.
     */
    private Buffered.SourceException currentBufferedSourceException = null;

    /**
     * Is automatic validation enabled.
     */
    private boolean validationVisible = true;

    private static final Method VALUE_CHANGE_METHOD;

    static {
        try {
            VALUE_CHANGE_METHOD = Property.ValueChangeListener.class
                    .getDeclaredMethod("valueChange",
                            new Class[] { Property.ValueChangeEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding methods in AbstractField");
        }
    }
    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.Component.Focusable#focus()
     */
    @Override
    public void focus() {
        super.focus();
    }

    @Override
    public boolean isInvalidCommitted() {
        return this.invalidAllowed;
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        this.invalidAllowed = isCommitted;
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        if (dataSource != null && !dataSource.isReadOnly()) {
            if ((isInvalidCommitted() || isValid())) {
                final Object newValue = getValue();
                try {

                    // Commits the value to datasource.
                    dataSource.setValue(newValue);

                } catch (final Throwable e) {

                    // Sets the buffering state.
                    currentBufferedSourceException = new Buffered.SourceException(
                            this, e);
                    requestRepaint();

                    // Throws the source exception.
                    throw currentBufferedSourceException;
                }
            } else {
                /* An invalid value and we don't allow them, throw the exception */
                validate();
            }
        }

        boolean repaintNeeded = false;

        // The abstract field is not modified anymore
        if (modified) {
            modified = false;
            repaintNeeded = true;
        }

        // If successful, remove set the buffering state to be ok
        if (currentBufferedSourceException != null) {
            currentBufferedSourceException = null;
            repaintNeeded = true;
        }

        if (repaintNeeded) {
            requestRepaint();
        }
    }

    @Override
    public void discard() throws SourceException {
        if (dataSource != null) {
            Property dataSource2 = dataSource;
            setPropertyDataSource(null);
            setPropertyDataSource(dataSource2);
            modified = false;
        }
    }

    @Override
    public boolean isWriteThrough() {
        return this.writeTroughMode;
    }

    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
        if (writeTroughMode == writeThrough) {
            return;
        }
        writeTroughMode = writeThrough;
        if (writeTroughMode) {
            commit();
        }
    }

    @Override
    public boolean isReadThrough() {
        return false;
    }

    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        // Not yet implemented.
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void addValidator(Validator validator) {
        if (validators == null) {
            validators = new LinkedList<Validator>();
        }
        validators.add(validator);
        requestRepaint();
    }

    @Override
    public void removeValidator(Validator validator) {
        if (validators != null) {
            validators.remove(validator);
        }
        requestRepaint();
    }

    @Override
    public Collection<Validator> getValidators() {
        if (validators == null || validators.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableCollection(validators);
    }

    @Override
    public boolean isValid() {
        if (getReceiver().isEmpty()) {
            if (isRequired()) {
                return false;
            } else {
                return true;
            }
        }

        if (validators == null) {
            return true;
        }

        final Object value = getValue();
        for (final Iterator<Validator> i = validators.iterator(); i.hasNext();) {
            if (!(i.next()).isValid(value)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void validate() throws InvalidValueException {
        if (getReceiver().isEmpty()) {
            if (isRequired()) {
                throw new Validator.EmptyValueException(requiredError);
            } else {
                return;
            }
        }

        // If there is no validator, there can not be any errors
        if (validators == null) {
            return;
        }

        // Initialize temps
        Validator.InvalidValueException firstError = null;
        LinkedList<InvalidValueException> errors = null;
        final Object value = getValue();

        // Gets all the validation errors
        for (final Iterator<Validator> i = validators.iterator(); i.hasNext();) {
            try {
                (i.next()).validate(value);
            } catch (final Validator.InvalidValueException e) {
                if (firstError == null) {
                    firstError = e;
                } else {
                    if (errors == null) {
                        errors = new LinkedList<InvalidValueException>();
                        errors.add(firstError);
                    }
                    errors.add(e);
                }
            }
        }

        // If there were no error
        if (firstError == null) {
            return;
        }

        // If only one error occurred, throw it forwards
        if (errors == null) {
            throw firstError;
        }

        // Creates composite validator
        final Validator.InvalidValueException[] exceptions = new Validator.InvalidValueException[errors
                .size()];
        int index = 0;
        for (final Iterator<InvalidValueException> i = errors.iterator(); i
                .hasNext();) {
            exceptions[index++] = i.next();
        }

        throw new Validator.InvalidValueException(null, exceptions);
    }

    @Override
    public boolean isInvalidAllowed() {
        return invalidAllowed;
    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
        this.invalidAllowed = invalidValueAllowed;
    }

    @Override
    public Object getValue() {
        return getReceiver().getValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        getReceiver().setValue(newValue);
        if (writeTroughMode) {
            commit();
        }
    }

    @Override
    public Class< ? > getType() {
        return Byte[].class;
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        addListener(AbstractField.ValueChangeEvent.class, listener,
            VALUE_CHANGE_METHOD);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        removeListener(AbstractField.ValueChangeEvent.class, listener,
            VALUE_CHANGE_METHOD);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        // Not Implemented
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        // Stops listening the old data source changes
        if (dataSource != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(dataSource.getClass())) {
            ((Property.ValueChangeNotifier) dataSource).removeListener(this);
        }

        Class<?> type = newDataSource == null ? String.class : newDataSource
                .getType();
        if (type != byte[].class) {
            throw new IllegalArgumentException("Property type " + type
                    + " is not compatible with UploadField");
        }

        // Sets the new data source
        dataSource = newDataSource;

        // Gets the value from source
        try {
            if (dataSource != null) {
                getReceiver().setValue(dataSource.getValue());
            }
            modified = false;
        } catch (final Throwable e) {
            currentBufferedSourceException = new Buffered.SourceException(this,
                    e);
            modified = true;
        }

        // Listens the new data source if possible
        if (dataSource instanceof Property.ValueChangeNotifier) {
            ((Property.ValueChangeNotifier) dataSource).addListener(this);
        }

        // Copy the validators from the data source
        if (dataSource instanceof Validatable) {
            final Collection<Validator> validators = ((Validatable) dataSource)
                    .getValidators();
            if (validators != null) {
                for (final Iterator<Validator> i = validators.iterator(); i
                        .hasNext();) {
                    addValidator(i.next());
                }
            }
        }

        updateDisplay();
    }

    @Override
    public Property getPropertyDataSource() {
        return  this.dataSource;
    }

    @Override
    public int getTabIndex() {
        return this.tabIndex;
    }

    @Override
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        this.requiredError = requiredMessage;
        requestRepaint();
    }

    @Override
    public String getRequiredError() {
        return this.requiredError;
    }

    public boolean isValidationVisible() {
        return this.validationVisible;
    }

    public void setValidationVisible(boolean validateAutomatically) {
        if (this.validationVisible != validateAutomatically) {
            requestRepaint();
            this.validationVisible = validateAutomatically;
        }
    }

    public void setCurrentBufferedSourceException(
        Buffered.SourceException currentBufferedSourceException) {
    this.currentBufferedSourceException = currentBufferedSourceException;
    requestRepaint();
    }
}
