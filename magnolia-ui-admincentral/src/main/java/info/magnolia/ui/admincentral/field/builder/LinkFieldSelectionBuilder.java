/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.field.builder;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.content.view.ChooseDialogContentPresenter;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.field.TextAndContentViewField;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchViewImpl;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.LinkFieldSelectionDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a {@link TextAndContentViewField} field based on a field definition.
 * This field is used to create an Input text field with a large ContentView area.
 * The column value to handle is coming from the definition. If this value is not set, or the column is not part of the
 * row elements, the <b>Node Path is used</b>.
 */
public class LinkFieldSelectionBuilder extends AbstractFieldBuilder<LinkFieldSelectionDefinition> {
    private static final Logger log = LoggerFactory.getLogger(LinkFieldSelectionBuilder.class);
    private TextAndContentViewField textContent;
    private EventBus appEventBus;
    private ChooseDialogContentPresenter contentPresenter;

    @Inject
    public LinkFieldSelectionBuilder(LinkFieldSelectionDefinition definition, Item relatedFieldItem, ChooseDialogContentPresenter contentPresenter,  @Named("choosedialog") final EventBus eventbus) {
        super(definition, relatedFieldItem);
        this.contentPresenter = contentPresenter;
        this.appEventBus = eventbus;
    }


    @Override
    protected Field buildField() {
        // create a new Field.
        textContent = new TextAndContentViewField(definition.isDisplayTextField(), definition.isDisplayTextFieldOnTop());
        // create a new View
        ContentWorkbenchViewImpl parentView = new ContentWorkbenchViewImpl();
        contentPresenter.initContentView(parentView);
        textContent.setContentView(parentView);

        //AddHandler.
        // On a selected Item, propagate the specified Column Value to the TextField.
        appEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                Node selected = SessionUtil.getNode(event.getWorkspace(), event.getPath());
                if(selected != null) {
                    try {
                        if(StringUtils.isNotBlank(definition.getColumnName()) && selected.hasProperty(definition.getColumnName())) {
                            textContent.setValue(selected.getProperty(definition.getColumnName()).getString());
                        } else {
                            textContent.setValue(selected.getPath());
                        }
                    }
                    catch (RepositoryException e) {
                        log.error("Not able to access the configured property. Value will not be set.",e);
                    }
                }

            }

        });
        return textContent;
    }


    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }
}
