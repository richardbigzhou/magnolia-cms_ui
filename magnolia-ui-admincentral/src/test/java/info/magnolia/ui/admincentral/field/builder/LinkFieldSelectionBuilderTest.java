/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import info.magnolia.ui.admincentral.content.view.PickerDialogContentPresenter;
import info.magnolia.ui.admincentral.field.TextAndContentViewField;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.model.field.definition.LinkFieldSelectionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link LinkFieldSelectionBuilder}.
 */
public class LinkFieldSelectionBuilderTest extends AbstractBuilderTest<LinkFieldSelectionDefinition> {

    private LinkFieldSelectionBuilder builder;

    @Test
    public void buildFieldSimpleTest() {
        // GIVEN
        PickerDialogContentPresenter presenter = mock(PickerDialogContentPresenter.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = builder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndContentViewField);
        assertEquals(true, ((TextAndContentViewField)field).getTextField().isVisible());
    }

    @Test
    public void fieldEventTest() throws RepositoryException {
        // GIVEN
        PickerDialogContentPresenter presenter = mock(PickerDialogContentPresenter.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();
        // WHEN
        //eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), baseNode.getPath()));

        // THEN
        // as No columnName defined return the Item path as Value property
        assertEquals(baseNode.getPath(), field.getValue());
    }

    @Test
    public void fieldEventCustomPropertyTest() throws RepositoryException {
        // GIVEN
        PickerDialogContentPresenter presenter = mock(PickerDialogContentPresenter.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        baseNode.setProperty("newProperty", "initial");
        baseItem = new JcrNodeAdapter(baseNode);
        baseItem.addItemProperty("newProperty", DefaultPropertyUtil.newDefaultProperty("newProperty", null, "initial"));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();

        // WHEN
        //eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), baseNode.getPath()));

        // THEN
        assertEquals("initial", field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldSelectionDefinition fieldDefinition = new LinkFieldSelectionDefinition();
        //fieldDefinition = (LinkFieldSelectionDefinition)AbstractFieldBuilderTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        this.definition = fieldDefinition;
    }

}
