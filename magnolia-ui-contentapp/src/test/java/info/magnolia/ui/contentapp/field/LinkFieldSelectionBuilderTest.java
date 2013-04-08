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
package info.magnolia.ui.contentapp.field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.admincentral.field.builder.LinkFieldBuilder;
import info.magnolia.ui.contentapp.browser.BrowserView;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogContentPresenter;
import info.magnolia.ui.form.field.builder.AbstractBuilderTest;
import info.magnolia.ui.form.field.builder.AbstractFieldBuilderTest;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.workbench.tree.TreeContentViewDefinition;

import javax.jcr.RepositoryException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.ui.Field;

/**
 * Tests.
 */
public class LinkFieldSelectionBuilderTest extends AbstractBuilderTest<LinkFieldSelectionDefinition> {

    private LinkFieldSelectionBuilder builder;

    private ChooseDialogContentPresenter presenter;

    private EventBus eventBus;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        presenter = mock(ChooseDialogContentPresenter.class);
        eventBus = new SimpleEventBus();
        // make sure that workbench view registers a content view so that restore selection doesn't fail.
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BrowserView parentView = (BrowserView) args[0];
                parentView.getWorkbenchView().addContentView(ViewType.TREE, mock(ContentView.class), new TreeContentViewDefinition());
                return null;
            }
        }).when(presenter).startChooseDialog(any(WorkbenchView.class));

    }

    @Test
    public void buildFieldSimpleTest() {
        // GIVEN
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field field = builder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndContentViewField);
        assertEquals(true, ((TextAndContentViewField) field).getTextField().isVisible());
    }

    @Test
    public void fieldEventTest() throws RepositoryException {
        // GIVEN
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();

        // WHEN
        eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), (JcrItemAdapter) baseItem));

        // THEN
        // as No columnName defined return the Item path as Value property
        assertEquals(baseNode.getPath(), field.getValue());
    }

    @Test
    public void fieldEventCustomPropertyTest() throws RepositoryException {
        // GIVEN
        baseNode.setProperty("newProperty", "initial");
        baseItem = new JcrNodeAdapter(baseNode);
        baseItem.addItemProperty("newProperty", DefaultPropertyUtil.newDefaultProperty("newProperty", null, "initial"));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, presenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();

        // WHEN
        eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), (JcrItemAdapter) baseItem));

        // THEN
        assertEquals("initial", field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldSelectionDefinition fieldDefinition = new LinkFieldSelectionDefinition();
        fieldDefinition = (LinkFieldSelectionDefinition) AbstractFieldBuilderTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        this.definition = fieldDefinition;
    }

}
