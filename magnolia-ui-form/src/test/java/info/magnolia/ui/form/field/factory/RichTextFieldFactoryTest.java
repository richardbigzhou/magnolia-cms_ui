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
package info.magnolia.ui.form.field.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextField;

import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Field;
import com.vaadin.util.CurrentInstance;

/**
 * Tests.
 */
public class RichTextFieldFactoryTest extends AbstractFieldFactoryTestCase<RichTextFieldDefinition> {

    private RichTextFieldFactory richTextFieldFactory;

    @Test
    public void testGetField() throws Exception {

        VaadinRequest request = mock(VaadinRequest.class);
        CurrentInstance.set(VaadinRequest.class, request);
        // GIVEN
        richTextFieldFactory = new RichTextFieldFactory(definition, baseItem, null, null, null);
        richTextFieldFactory.setComponentProvider(componentProvider);
        // WHEN
        Field field = richTextFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof MagnoliaRichTextField);
        assertEquals(0, ((AbstractJcrNodeAdapter) baseItem).getChildren().size());
    }


    @Override
    protected void createConfiguredFieldDefinition() {
        RichTextFieldDefinition fieldDefinition = new RichTextFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
