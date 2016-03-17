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

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.junit.Test;

import com.vaadin.ui.TextField;

public class TextFieldIntegrationTest extends FieldIntegrationTestCase {

    /**
     * Removing empty text properties involves several parties:
     * <ul>
     * <li>First, {@link TextFieldFactory} configures Vaadin's {@link TextField}'s null representation as empty string.</li>
     * <li>Then, upon receiving value changes from the client, Vaadin's {@link com.vaadin.ui.AbstractTextField AbstractTextField}
     * converts the null representation into null value;
     * mind that invoking {@link TextField#setValue(String)} directly with empty-string does not perform such conversion.</li>
     * <li>Finally, JCR's <em>"no such thing as a null value"</em> is doing the rest, eventually removing the property.</li>
     *
     * @see TextFieldFactory#createFieldComponent()
     * @see com.vaadin.ui.AbstractTextField#changeVariables(Object, java.util.Map)
     * @see <a href="http://www.day.com/specs/jcr/2.0/10_Writing.html#10.9.1%20Setting%20a%20Property%20to%20Null">10.9.1 Setting a Property to Null</a>
     */
    @Test
    public void removeEmptyProperties() throws Exception {
        // GIVEN
        TextFieldDefinition definition = textFieldDefinition("firstName");

        Node node = session.getRootNode().addNode("node");
        node.setProperty("firstName", "init1");
        node.getSession().save();

        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        TextFieldFactory factory = new TextFieldFactory(definition, nodeAdapter);
        factory.setComponentProvider(componentProvider);
        TextField field = (TextField) factory.createField();

        // WHEN
        enterText(field, "");
        nodeAdapter.applyChanges();

        // THEN
        assertThat(node, not(hasProperty("firstName")));
    }

    private TextFieldDefinition textFieldDefinition(String name) {
        TextFieldDefinition text = new TextFieldDefinition();
        text.setName(name);
        return text;
    }
}
