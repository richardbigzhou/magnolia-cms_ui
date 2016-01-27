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

import static com.google.common.base.Enums.getIfPresent;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.vaadin.aceeditor.AceMode.forFileEnding;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CodeFieldDefinition;

import javax.inject.Inject;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;

import com.google.common.base.Optional;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a code field, based on the {@link AceEditor} add-on for Vaadin.
 */
public class CodeFieldFactory extends AbstractFieldFactory<CodeFieldDefinition, String> {

    private static final String FREEMARKER_LANGUAGE = "freemarker";
    private static final String ACE_EDITOR_FTL_ID = "ftl";
    private static final String ACE_EDITOR_RESOURCES = "/.resources/ace/";

    private AceEditor field;

    @Inject
    public CodeFieldFactory(CodeFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #CodeFieldFactory(CodeFieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public CodeFieldFactory(CodeFieldDefinition definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<String> createFieldComponent() {
        field = newAceEditor();
        // Add a TextChange Listener. This is needed as the current AceEditor implementation do not update the
        // linked datasource in case of text change.
        field.addTextChangeListener(createTextChangeListener(field));
        // Set style
        field.setStyleName("textcodefield");
        field.setUseWorker(false);

        return field;
    }

    /**
     * @return newly constructed {@link AceEditor}.
     */
    protected AceEditor newAceEditor() {
        AceEditor aceEditor = new AceEditor();
        if (MgnlContext.isWebContext()) {
            String aceEditorResourcePath = MgnlContext.getContextPath() + ACE_EDITOR_RESOURCES;
            aceEditor.setModePath(aceEditorResourcePath);
            aceEditor.setWorkerPath(aceEditorResourcePath);
            aceEditor.setThemePath(aceEditorResourcePath);
        }
        // If language is set, then we don't want to conflict with it.
        if (isNotBlank(definition.getLanguage())) {
            aceEditor.setMode(getModeType(definition.getLanguage()));
        } else if (isNotBlank(definition.getFileNameProperty())) {
            Property<?> fileNameProperty = item.getItemProperty(definition.getFileNameProperty());
            if (fileNameProperty != null) {
                String fileName = String.valueOf(fileNameProperty.getValue());
                String extension = getExtension(fileName);
                AceMode mode = getAceModeByFileExtension(extension);
                if (mode != null) {
                    aceEditor.setMode(mode);
                }
            }
        }
        if (definition.getHeight() > 0) {
            aceEditor.setHeight(definition.getHeight(), Unit.PIXELS);
        }
        return aceEditor;
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
     * Get Ace-editor compliant mode type name for a language.
     *
     * TODO: currently only 'freemarker' -> 'ftl' case is handled, otherwise the method returns the string from definition.
     * If needed we could craft our own mapping from human-readable language names into the Ace editor mode identifiers.
     */
    private String getModeType(String language) {
        return FREEMARKER_LANGUAGE.equals(language) ? ACE_EDITOR_FTL_ID : language;
    }

    /**
     * Get the {@link AceMode} by file extension.
     * <p>
     * First tries to match the given extension against an AceMode value, otherwise looks into AceMode's additional mappings (<code>endingModeMap</code>).
     */
    private AceMode getAceModeByFileExtension(String extension) {
        // Trying the get AceMode from the Enum
        Optional<AceMode> aceModeValue = getIfPresent(AceMode.class, extension);
        if (aceModeValue.isPresent()) {
            return aceModeValue.get();
        } else {
            // Trying to get AceMode from the AceMode.endingModeMap
            AceMode aceMode = forFileEnding(extension);
            return aceMode != null ? aceMode : null;
        }
    }

}
