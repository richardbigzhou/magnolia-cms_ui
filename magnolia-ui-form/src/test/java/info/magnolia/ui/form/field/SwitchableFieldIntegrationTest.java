/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.form.field;

import static com.google.common.collect.Lists.newArrayList;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;

import javax.jcr.Node;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class SwitchableFieldIntegrationTest extends FieldIntegrationTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerFieldType(TextFieldDefinition.class, TextFieldFactory.class);
        registerFieldType(OptionGroupFieldDefinition.class, OptionGroupFieldFactory.class);
        registerFieldType(SelectFieldDefinition.class, SelectFieldFactory.class);
    }

    @Test
    public void discardUnselectedOptions() throws Exception {
        // GIVEN
        SwitchableFieldDefinition definition = switchableFieldDefinition("switchable");

        Node node = session.getRootNode().addNode("node");
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        SwitchableFieldFactory<SwitchableFieldDefinition> factory = new SwitchableFieldFactory<>(definition, nodeAdapter, fieldFactoryFactory, componentProvider, i18nAuthoringSupport);
        SwitchableField field = (SwitchableField) factory.createField();
        Iterator<Component> componentIterator = field.iterator();
        // CustomField#getContent is lazy, we trigger it via ComponentIterator#next()
        VerticalLayout layout = (VerticalLayout) componentIterator.next();

        OptionGroup options = (OptionGroup) layout.getComponent(0);
        TextField t1 = (TextField) layout.getComponent(1);
        TextField t2 = (TextField) layout.getComponent(2);

        // WHEN
        options.setValue("t1");
        enterText(t1, "v1");
        options.setValue("t2");
        enterText(t2, "v2");
        nodeAdapter.applyChanges();

        // THEN
        assertThat(node, hasProperty("switchable", "t2"));
        assertThat(node, hasProperty("switchablet2", "v2"));
        assertThat(node, not(hasProperty("switchablet1")));
    }

    @Test
    public void doNotKeepEmptyProperties() throws Exception {
        // GIVEN
        SwitchableFieldDefinition definition = switchableFieldDefinition("switchable");

        Node node = session.getRootNode().addNode("node");
        node.setProperty("switchable", "t2");
        node.setProperty("switchablet1", "");
        node.setProperty("switchablet2", "boo2");
        node.getSession().save();

        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        SwitchableFieldFactory<SwitchableFieldDefinition> factory = new SwitchableFieldFactory<>(definition, nodeAdapter, fieldFactoryFactory, componentProvider, i18nAuthoringSupport);
        SwitchableField field = (SwitchableField) factory.createField();
        Iterator<Component> componentIterator = field.iterator();
        // CustomField#getContent is lazy, we trigger it via ComponentIterator#next()
        VerticalLayout layout = (VerticalLayout) componentIterator.next();
        TextField t2 = (TextField) layout.getComponent(2);

        // WHEN
        enterText(t2, "");
        nodeAdapter.applyChanges();

        // THEN
        assertThat(node, hasProperty("switchable", "t2"));
        assertThat(node, not(hasProperty("switchablet1")));
        assertThat(node, not(hasProperty("switchablet2")));
    }

    private SwitchableFieldDefinition switchableFieldDefinition(String name) {
        SwitchableFieldDefinition switchable = new SwitchableFieldDefinition();
        switchable.setName(name);

        TextFieldDefinition t1 = new TextFieldDefinition();
        t1.setName("t1");
        TextFieldDefinition t2 = new TextFieldDefinition();
        t2.setName("t2");
        switchable.setFields(Lists.<ConfiguredFieldDefinition>newArrayList(t1, t2));

        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setName("t1");
        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setName("t2");
        switchable.setOptions(newArrayList(option1, option2));
        return switchable;
    }
}
