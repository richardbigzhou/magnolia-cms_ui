/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom.processor;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class AreaProcessorTest {

    private MgnlArea area;
    private AreaProcessor processor;

    @Before
    public void setUp() {
        final Model model = mock(Model.class);
        area = mock(MgnlArea.class);
        processor = new AreaProcessor(model, area);
    }

    @Test
    public void testHasComponentPlaceHolderWhenNotOptionalAndAreaDefinitionTypeNoComponent() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_NO_COMPONENT);
        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testHasComponentPlaceHolderWhenNotOptionalAndNoAvailableComponents() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_AVAILABLE_COMPONENTS, "");
        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testHasComponentPlaceHolderWhenNotOptionalAndNotTypeSingle() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, "testType");
        attributes.put(AreaProcessor.ATTRIBUTE_AVAILABLE_COMPONENTS, "testComponent");
        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasComponentPlaceHolderWhenNotOptionalAndTypeSingleAndEmptyComponents() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_SINGLE);
        attributes.put(AreaProcessor.ATTRIBUTE_AVAILABLE_COMPONENTS, "testComponent");

        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasComponentPlaceHolderWhenNotOptionalAndTypeSingleAndNonEmptyComponents() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_SINGLE);
        attributes.put(AreaProcessor.ATTRIBUTE_AVAILABLE_COMPONENTS, "testComponent");
        List<MgnlComponent> components = new ArrayList<MgnlComponent>();
        components.add(mock(MgnlComponent.class));
        when(area.getComponents()).thenReturn(components);
        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testHasComponentPlaceHolderWhenOptionalAndNotCreated() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_OPTIONAL, "true");
        attributes.put(AreaProcessor.ATTRIBUTE_CREATED, "false");
        // WHEN
        final boolean result = processor.hasComponentPlaceHolder(attributes);

        // THEN
        assertFalse(result);
    }

   @Test
   public void testHasControlBarWhenNotEditable() {
       // GIVEN
       final Map<String, String> attributes = new HashMap<String, String>();
       attributes.put(AreaProcessor.ATTRIBUTE_EDITABLE, "false");

       // WHEN
       final boolean result = processor.hasControlBar(attributes);

       // THEN
       assertFalse(result);
   }

    @Test
    public void testHasControlBarWhenOptional() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_OPTIONAL, "true");

        // WHEN
        final boolean result = processor.hasControlBar(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasControlBarWhenOfTypeSingle() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_SINGLE);

        // WHEN
        final boolean result = processor.hasControlBar(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasControlBarWhenShowNewComponent() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_NO_COMPONENT);
        attributes.put(AreaProcessor.ATTRIBUTE_SHOW_NEW_COMPONENT_AREA, "true");

        // WHEN
        final boolean result = processor.hasControlBar(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasControlBarWhenHavingADialog() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_NO_COMPONENT);
        attributes.put(AreaProcessor.ATTRIBUTE_DIALOG, "testDialog");

        // WHEN
        final boolean result = processor.hasControlBar(attributes);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasControlBarWhenNotHavingADialog() {
        // GIVEN
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(AreaProcessor.ATTRIBUTE_TYPE, AreaDefinition.TYPE_NO_COMPONENT);

        // WHEN
        final boolean result = processor.hasControlBar(attributes);

        // THEN
        assertFalse(result);
    }
}
