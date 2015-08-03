/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;
import info.magnolia.ui.form.field.factory.LinkFieldFactory;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Tests.
 */
public class LinkFieldSelectionFactoryTest extends AbstractFieldFactoryTestCase<LinkFieldSelectionDefinition> {

    private LinkFieldSelectionFactory builder;

    private WorkbenchPresenter workbenchPresenter;

    private EventBus eventBus;

    private Set<Object> itemIds;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        workbenchPresenter = mock(WorkbenchPresenter.class);
        eventBus = new SimpleEventBus();
        itemIds = new HashSet<Object>();
        // make sure that workbench view registers a content view so that restore selection doesn't fail.
        WorkbenchView workbenchView = mock(WorkbenchView.class);
        doReturn(mock(Component.class)).when(workbenchView).asVaadinComponent();
        doReturn(workbenchView).when(workbenchPresenter).start(any(WorkbenchDefinition.class), any(ImageProviderDefinition.class), any(EventBus.class));
    }

    @Test
    public void buildFieldSimpleTest() {
        // GIVEN
        baseItem.addItemProperty(LinkFieldFactory.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(String.class, null));
        builder = new LinkFieldSelectionFactory(definition, baseItem, workbenchPresenter, eventBus);
        builder.setComponentProvider(componentProvider);

        // WHEN
        Field field = builder.createField();

        // THEN
        assertEquals(true, field instanceof TextAndContentViewField);
        assertEquals(true, ((TextAndContentViewField) field).getTextField().isVisible());
    }

    @Test
    public void fieldEventTest() throws RepositoryException {
        // GIVEN
        baseItem.addItemProperty(LinkFieldFactory.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(String.class, null));
        builder = new LinkFieldSelectionFactory(definition, baseItem, workbenchPresenter, eventBus);
        builder.setComponentProvider(componentProvider);
        Field field = builder.createField();
        itemIds.add(JcrItemUtil.getItemId(baseNode));

        // WHEN
        eventBus.fireEvent(new SelectionChangedEvent(itemIds));

        // THEN
        // as No columnName defined return the Item path as Value property
        assertEquals(baseNode.getPath(), field.getValue());
    }

    @Test
    public void fieldEventCustomPropertyTest() throws RepositoryException {
        // GIVEN
        baseNode.setProperty("newProperty", "initial");
        baseItem = new JcrNodeAdapter(baseNode);
        baseItem.addItemProperty("newProperty", DefaultPropertyUtil.newDefaultProperty(String.class, "initial"));
        builder = new LinkFieldSelectionFactory(definition, baseItem, workbenchPresenter, eventBus);
        builder.setComponentProvider(componentProvider);
        Field field = builder.createField();
        itemIds.add(JcrItemUtil.getItemId(baseNode));

        // WHEN
        eventBus.fireEvent(new SelectionChangedEvent(itemIds));

        // THEN
        assertEquals("initial", field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldSelectionDefinition fieldDefinition = new LinkFieldSelectionDefinition();
        fieldDefinition = (LinkFieldSelectionDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        this.definition = fieldDefinition;
    }

}
