/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.contentapp.field;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.LinkFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a {@link TextAndContentViewField} field based on a
 * field definition. This field is used to create an Input text field with a
 * large ContentView area. The column value to handle is coming from the
 * definition. If this value is not set, or the column is not part of the row
 * elements, the <b>Node Path is used</b>.
 */
public class LinkFieldSelectionFactory extends AbstractFieldFactory<LinkFieldSelectionDefinition, String> {

    private static final Logger log = LoggerFactory.getLogger(LinkFieldSelectionFactory.class);

    private final EventBus chooseDialogEventBus;

    private final WorkbenchPresenter workbenchPresenter;

    private final String propertyName;

    private TextAndContentViewField textContent;

    @Inject
    public LinkFieldSelectionFactory(LinkFieldSelectionDefinition definition, Item relatedFieldItem, WorkbenchPresenter contentPresenter,
                                     @Named(ChooseDialogEventBus.NAME) final EventBus chooseDialogEventBus) {
        super(definition, relatedFieldItem);
        this.workbenchPresenter = contentPresenter;
        this.chooseDialogEventBus = chooseDialogEventBus;
        // Item is build by the LinkFieldFactory and has only one property.
        // This property has the name of property we are supposed to propagate.
        propertyName = String.valueOf(relatedFieldItem.getItemPropertyIds().iterator().next());
        // This will allow to set the selected value to the desired property
        // name (handle by AbstractFieldFactory.getOrCreateProperty())
        definition.setName(propertyName);
    }

    @Override
    protected Field<String> createFieldComponent() {
        textContent = new TextAndContentViewField(definition.isDisplayTextField(), definition.isDisplayTextFieldOnTop());

        // TODO 20130513 mgeljic restore choose dialogs as real dialogs with a configured workbench field or drop that field type completely.
        WorkbenchView workbenchView = workbenchPresenter.start(null, null, null);
        textContent.setContentView(workbenchView);

        // Set selected item.
        restoreContentSelection();

        // On a selected Item, propagate the specified Column Value to the TextField.
        chooseDialogEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                JcrItemId firstItemId = (JcrItemId) event.getFirstItemId();
                final Node selected = SessionUtil.getNodeByIdentifier(firstItemId.getWorkspace(), firstItemId.getUuid());
                if (selected != null) {
                    try {
                        boolean isPropertyExisting = StringUtils.isNotBlank(propertyName)
                                && !LinkFieldFactory.PATH_PROPERTY_NAME.equals(propertyName) && selected.hasProperty(propertyName);
                        textContent.setValue(isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath());
                    } catch (RepositoryException e) {
                        log.error("Not able to access the configured property. Value will not be set.", e);
                    }
                }
            }
        });
        return textContent;
    }

    @Override
    protected Class<String> getDefaultFieldType() {
        return String.class;
    }

    /**
     * Set the selected item <b> only </b> if the property is the Item id (node path).
     */
    private void restoreContentSelection() {
        final String propertyValue = String.valueOf(item.getItemProperty(propertyName).getValue());
        // TODO 20130513 mgeljic get fallback root path from workbench definition
        final String path = LinkFieldFactory.PATH_PROPERTY_NAME.equals(propertyName) && StringUtils.isNotBlank(propertyValue) ? propertyValue : "/";
        workbenchPresenter.select(path);
    }
}
