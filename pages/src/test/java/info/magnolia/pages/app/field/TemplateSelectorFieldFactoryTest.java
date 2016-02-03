/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.pages.app.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TemplateSelectorFieldFactoryTest {

    private Collection<TemplateDefinition> templates;
    private ConfiguredTemplateDefinition templateDefinition;
    private TemplateSelectorFieldFactory templateFieldFactory;

    @Before
    public void setUp() {
        Context context = mock(Context.class);
        when(context.getLocale()).thenReturn(Locale.ENGLISH);
        MgnlContext.setInstance(context);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager(mock(Node2BeanProcessor.class)));

        JcrNodeAdapter item = mock(JcrNodeAdapter.class);
        when(item.getJcrItem()).thenReturn(mock(Node.class));

        templates = new ArrayList<TemplateDefinition>();
        TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        when(templateDefinitionAssignment.getAvailableTemplates(any(Node.class))).thenReturn(templates);

        templateDefinition = new ConfiguredTemplateDefinition(mock(TemplateAvailability.class));
        templateFieldFactory = new TemplateSelectorFieldFactory(new TemplateSelectorDefinition(), item, templateDefinitionAssignment);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void optionDefinitionReturnsTitle() throws Exception {
        // GIVEN
        templateDefinition.setId("foo");
        templateDefinition.setTitle("bar");
        templates.add(templateDefinition);

        // WHEN
        List<SelectFieldOptionDefinition> defs = templateFieldFactory.getSelectFieldOptionDefinition();

        // THEN
        assertThat(defs.get(0).getLabel(), is("bar"));
    }

    @Test
    public void optionDefinitionFallsbackToId() {
        // GIVEN
        templateDefinition.setId("foo");
        templateDefinition.setTitle(null);
        templates.add(templateDefinition);

        // WHEN
        List<SelectFieldOptionDefinition> defs = templateFieldFactory.getSelectFieldOptionDefinition();

        // THEN
        assertThat(defs.get(0).getLabel(), is("foo"));
    }
}
