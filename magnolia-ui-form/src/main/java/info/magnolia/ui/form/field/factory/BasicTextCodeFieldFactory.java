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
package info.magnolia.ui.form.field.factory;


import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;

import com.vaadin.data.Item;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an Text code field definition.
 * 
 * @param <D> type of definition
 */
public class BasicTextCodeFieldFactory<D extends BasicTextCodeFieldDefinition> extends AbstractFieldFactory<D, String> {

    private static final Logger log = LoggerFactory.getLogger(BasicTextCodeFieldFactory.class);
    private AceEditor field;

    public BasicTextCodeFieldFactory(D definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field<String> createFieldComponent() {
        field = newAceEditor();
        // Add a TextChange Listener. This is needed as the current AceEditor implementation do not update the
        // linked datasource in case of text change.
        field.addTextChangeListener(createTextChangeListener(field));
        // Set style
        field.setStyleName("textcodefield");

        return field;
    }

    /**
     * @return newly constructed {@link AceEditor}.
     */
    protected AceEditor newAceEditor() {
        AceEditor aceEditor =  new AceEditor();
        aceEditor.setMode(checkModeType(definition.getLanguage()));
        return aceEditor;
    }

    /**
     * Set {@link AceEditor} mode.
     */
    protected void setAceEditorMode() {
        field.setMode(checkModeType(definition.getLanguage()));
    }

    /**
     * @return current {@link AceEditor} field.
     */
    protected AceEditor getField() {
        return field;
    }

    /**
     * Create a {@link TextChangeListener} in order to populate the typed text in the
     * related property datasource.
     */
    private TextChangeListener createTextChangeListener(final AceEditor field) {
        return new TextChangeListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void textChange(TextChangeEvent event) {
                field.getPropertyDataSource().setValue(event.getText());
            }
        };
    }

    /**
     * Define the Ace Field mode type.
     * If equivalent {@link AceMode} mode found, return Text mode.
     */
    private String checkModeType(String language) {
        try {
            return  AceMode.valueOf(language).name();
        } catch(IllegalArgumentException iae) {
            log.warn("Couldn't found the specified language {}. Text language will be used. ",language);
            return AceMode.text.name();
        }
    }

}
