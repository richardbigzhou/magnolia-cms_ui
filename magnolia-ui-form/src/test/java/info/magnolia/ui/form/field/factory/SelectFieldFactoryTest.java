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
package info.magnolia.ui.form.field.factory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.SelectFieldFactory}.
 */
public class SelectFieldFactoryTest extends AbstractFieldFactoryTestCase<SelectFieldDefinition> {

    private SelectFieldFactory<SelectFieldDefinition> dialogSelect;
    private Node remoteSelectOptionsNode;

    @Test
    public void createField() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertTrue(field instanceof ComboBox);
        Collection<?> items = ((ComboBox) field).getItemIds();
        assertThat(items.size(), is(3));
        assertThat(field.getValue().toString(), is("1"));
    }

    @Test
    public void createFieldSelectsDefaultOptionIfConfigured() throws Exception {
        // GIVEN
        SelectFieldOptionDefinition option = definition.getOptions().get(1);
        option.setSelected(true);
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals(option.getValue(), field.getValue().toString());
    }

    @Test
    public void createFieldSelectsFirstOptionIfNoDefaultConfigured() throws Exception {
        // GIVEN
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN first option is selected
        assertThat(field.getValue().toString(), is("1"));
    }

    @Test
    public void createFieldUsesNodeNamesIfOptionValuesAreNotSet() throws Exception {
        // GIVEN
        List<SelectFieldOptionDefinition> options = definition.getOptions();
        for (SelectFieldOptionDefinition option : options) {
            option.setValue(null);
            option.setName(option.getLabel().toLowerCase());
        }
        initializeSelectFieldFactory();

        // WHEN
        dialogSelect.createField();

        // THEN
        options = definition.getOptions();
        for (SelectFieldOptionDefinition option : options) {
            assertEquals(option.getName(), option.getValue());
        }
    }

    @Test
    public void createFieldDoesntSelectDefaultIfValueAlreadyExists() throws Exception {
        // GIVEN
        SelectFieldOptionDefinition option = definition.getOptions().get(1);
        option.setSelected(true);
        baseNode.setProperty(propertyName, "3");
        baseItem = new JcrNodeAdapter(baseNode);
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertThat(field.getValue().toString(), is("3"));
    }

    @Test
    public void createFieldWorksWithRemoteOptions() throws Exception {
        // GIVEN
        // Initialize a Options node.
        initRemoteOptionsNode();

        // Set remote Options in configuration
        definition.setPath(remoteSelectOptionsNode.getPath());
        definition.setRepository(workspaceName);
        definition.setOptions(new ArrayList<SelectFieldOptionDefinition>());
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        Collection<String> items = ((Collection<String>) ((ComboBox) field).getItemIds());
        assertThat(items, contains("en", "fr"));
        assertThat(field.getValue().toString(), is("en"));
    }

    @Test
    public void remoteOptionsGetRebuiltUponFieldRecreation() throws Exception {
        // GIVEN
        // Initialize a Options node.
        initRemoteOptionsNode();

        // Set remote Options in configuration
        definition.setOptions(Collections.<SelectFieldOptionDefinition>emptyList());
        definition.setPath(remoteSelectOptionsNode.getPath());
        definition.setRepository(workspaceName);

        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());

        initializeSelectFieldFactory();

        // WHEN we initially create a select field
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();
        // THEN initially pre-created options from the remote location are present
        assertThat(field.getItemIds(), Matchers.<Object>contains("en", "fr"));

        // WHEN we add another 'remote' option and re-create a field
        addRemoteSelectOption("Qux", "qux");
        initializeSelectFieldFactory();
        field = (AbstractSelect) dialogSelect.createField();

        // THEN initially pre-created options from the remote location are present
        assertThat(field.getItemIds(), Matchers.<Object>contains("en", "fr", "qux"));
    }

    @Test
    public void createFieldWithRemoteOptionsIgnoresNonMgnlNodeTypes() throws Exception {
        // GIVEN
        // Create a Options node.
        Node options = session.getRootNode().addNode("options");
        Node optionEn = options.addNode("en");
        optionEn.setProperty("value", "en");
        optionEn.setProperty("label", "English");
        Node optionFr = options.addNode("fr", "nt:hierarchyNode");
        optionFr.setProperty("value", "fr");
        optionFr.setProperty("label", "Francais");
        // Set remote Options in configuration
        definition.setPath(options.getPath());
        definition.setRepository(workspaceName);
        definition.setOptions(new ArrayList<SelectFieldOptionDefinition>());
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        Collection<?> items = ((ComboBox) field).getItemIds();
        assertThat("Only get one option as fr option node is not of 'mgnl' type", items.size(), is(1));
        assertThat(field.getValue().toString(), is("en"));
    }

    @Test
    public void createFieldWorksWithDifferentOptionValueAndLabelNames() throws Exception {
        // GIVEN
        // Create a Options node.
        Node options = session.getRootNode().addNode("options");
        Node optionEn = options.addNode("en");
        optionEn.setProperty("x", "en");
        optionEn.setProperty("z", "English");
        Node optionFr = options.addNode("fr");
        optionFr.setProperty("x", "fr");
        optionFr.setProperty("z", "Francais");
        optionFr.setProperty("selected", "true");
        // Set remote Options in configuration
        definition.setPath(options.getPath());
        definition.setRepository(workspaceName);
        definition.setOptions(new ArrayList<SelectFieldOptionDefinition>());
        // Define the name of value and label
        definition.setValueProperty("x");
        definition.setLabelProperty("z");
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());

        initializeSelectFieldFactory();

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        Collection<String> items = ((Collection<String>) ((ComboBox) field).getItemIds());
        assertThat(items, contains("en", "fr"));
        assertThat(field.getValue().toString(), is("fr"));
    }

    @Test
    public void createFieldSortsOptionsAlphabeticallyAscendingByDefault() throws Exception {
        // GIVEN
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();

        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("bb");
        option1.setValue("1");
        options.add(option1);

        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("AA");
        option2.setValue("2");
        options.add(option2);

        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("cc");
        option3.setValue("3");
        options.add(option3);

        definition.setOptions(options);

        initializeSelectFieldFactory();

        // WHEN
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();

        // THEN
        Collection<String> items = (Collection<String>) field.getItemIds();
        assertThat(items, contains("2", "1", "3"));
        assertThat(field.getValue().toString(), is("2"));
    }

    @Test
    public void createFieldDoesntSortOptionsIfSpecified() throws Exception {
        // GIVEN
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();

        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("bb");
        option1.setValue("1");
        options.add(option1);

        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("AA");
        option2.setValue("2");
        options.add(option2);

        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("cc");
        option3.setValue("3");
        options.add(option3);

        definition.setOptions(options);
        definition.setSortOptions(false);

        initializeSelectFieldFactory();

        // WHEN
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();

        // THEN
        Collection<String> items = (Collection<String>) field.getItemIds();
        assertThat(items, contains("1", "2", "3"));
        assertThat(field.getValue().toString(), is("1"));
    }

    @Test
    public void testCreateDefaultValueFromLong() throws Exception {
        // GIVEN
        initializeSelectFieldFactory();
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();
        field.removeAllItems();
        field.addItem(1L); // long value

        Property<Long> dataSource = new DefaultProperty<Long>(1L);

        // WHEN
        Object defaultValue = dialogSelect.createDefaultValue(dataSource);

        // THEN
        assertThat(defaultValue.toString(), is("1"));
    }

    @Test
    public void createFieldSortsOptionsByComparator() throws Exception {
        // GIVEN
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<>();

        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("aa");
        option1.setValue("1");
        options.add(option1);

        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("bb");
        option2.setValue("2");
        options.add(option2);

        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("cc");
        option3.setValue("3");
        options.add(option3);

        SelectFieldOptionDefinition option4 = new SelectFieldOptionDefinition();
        option4.setLabel("CC");
        option4.setValue("4");
        options.add(option4);

        SelectFieldOptionDefinition option5 = new SelectFieldOptionDefinition();
        option5.setLabel("BB");
        option5.setValue("5");
        options.add(option5);

        SelectFieldOptionDefinition option6 = new SelectFieldOptionDefinition();
        option6.setLabel("AA");
        option6.setValue("6");
        options.add(option6);

        definition.setOptions(options);
        definition.setComparatorClass(TestComparator.class);

        dialogSelect = new SelectFieldFactory<>(definition, baseItem, uiContext, i18NAuthoringSupport);
        componentProvider.setImplementation(TestComparator.class, TestComparator.class.getName());
        dialogSelect.setComponentProvider(componentProvider);

        // WHEN
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();

        // THEN
        Collection<String> items = (Collection<String>) field.getItemIds();
        assertThat(items, contains("1", "6", "2", "5", "3", "4"));
        assertThat(field.getValue().toString(), is("1"));
    }

    @Test
    public void createFieldSortsOptionsByNullSafeLabelComparator() throws Exception {
        // GIVEN
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<>();

        SelectFieldOptionDefinition nullOption = new SelectFieldOptionDefinition();
        nullOption.setLabel(null);
        nullOption.setValue("1");
        options.add(nullOption);

        SelectFieldOptionDefinition emptyOption = new SelectFieldOptionDefinition();
        emptyOption.setLabel("");
        emptyOption.setValue("2");
        options.add(emptyOption);

        SelectFieldOptionDefinition blankOption = new SelectFieldOptionDefinition();
        blankOption.setLabel("   ");
        blankOption.setValue("3");
        options.add(blankOption);

        definition.setOptions(options);
        initializeSelectFieldFactory();

        // WHEN
        AbstractSelect field = (AbstractSelect) dialogSelect.createField();

        // THEN
        Collection<String> items = (Collection<String>) field.getItemIds();

        assertThat(items, contains("1", "2", "3"));
        assertThat(field.getValue().toString(), is("1"));
    }

    @Test
    public void labelsForOptionsAreNotTranslatedWithOldI18n() throws Exception {
        // GIVEN
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<>();

        SelectFieldOptionDefinition nullOption = new SelectFieldOptionDefinition();
        nullOption.setLabel(null);
        nullOption.setValue("1");
        options.add(nullOption);

        definition.setOptions(options);
        initializeSelectFieldFactory();

        // WHEN
        List<SelectFieldOptionDefinition> optionDefinition = dialogSelect.getSelectFieldOptionDefinition();

        // THEN
        assertThat(optionDefinition, hasSize(1));
        assertNull(optionDefinition.get(0).getLabel());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        SelectFieldDefinition fieldDefinition = new SelectFieldDefinition();
        fieldDefinition = (SelectFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        fieldDefinition.setDefaultValue(null);
        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("One");
        option1.setValue("1");

        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("Two");
        option2.setValue("2");

        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("Three");
        option3.setValue("3");

        fieldDefinition.addOption(option1);
        fieldDefinition.addOption(option2);
        fieldDefinition.addOption(option3);

        this.definition = fieldDefinition;
    }

    protected void initRemoteOptionsNode() throws RepositoryException {
        remoteSelectOptionsNode = session.getRootNode().addNode("options");

        addRemoteSelectOption("English", "en");
        addRemoteSelectOption("Francais", "fr");
    }

    private void addRemoteSelectOption(String label, String value) throws RepositoryException {
        Node optionNode = remoteSelectOptionsNode.addNode(label);
        optionNode.setProperty("label", label);
        optionNode.setProperty("value", value);
    }

    private void initializeSelectFieldFactory() {
        dialogSelect = new SelectFieldFactory<>(definition, baseItem, uiContext, i18NAuthoringSupport);
        dialogSelect.setComponentProvider(componentProvider);
    }

    public static class TestComparator implements Comparator<SelectFieldOptionDefinition> {

        private final Collator col;

        public TestComparator() {
            col = Collator.getInstance();
            col.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(SelectFieldOptionDefinition o1, SelectFieldOptionDefinition o2) {
            return col.compare(o1.getLabel(), o2.getLabel());
        }
    }
}
