/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.actionbar.definition;

import static org.junit.Assert.*;

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


/**
 * Tests for {@link ActionbarSectionDefinitionKeyGenerator}.
 */
public class ActionbarSectionDefinitionKeyGeneratorTest {

    @Test
    public void getProperKeyForAppActionbarSectionLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        ActionbarSectionDefinitionKeyGenerator generator = new ActionbarSectionDefinitionKeyGenerator();
        // structure
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName("test-section");
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        ConfiguredAppDescriptor app = new ConfiguredAppDescriptor();
        app.setName("test-app");
        TestSubApp subapp = new TestSubApp();
        subapp.setName("test-subapp");
        // hierarchy
        app.addSubApp(subapp);
        subapp.setActionbar(actionbar);
        actionbar.addSection(section);
        // i18nizer
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        app = i18nizer.decorate(app);

        // WHEN
        List<String> keys = new ArrayList<String>(2);
        generator.keysFor(keys,
                ((TestSubApp) app.getSubApps().get("test-subapp")).getActionbar().getSections().get(0),
                section.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(2, keys.size());
        assertEquals("test-app.test-subapp.actionbar.sections.test-section.label", keys.get(0));
        assertEquals("test-app.test-subapp.actionbar.sections.test-section", keys.get(1));
    }

    @Test
    public void getProperKeyForMessageViewActionbarSectionLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        ActionbarSectionDefinitionKeyGenerator generator = new ActionbarSectionDefinitionKeyGenerator();
        // structure
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName("test-section");
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        TestMessageView messageView = new TestMessageView("test-module:testMessageView");
        // hierarchy
        messageView.setActionbar(actionbar);
        actionbar.addSection(section);
        // i18nizer
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        messageView = i18nizer.decorate(messageView);

        // WHEN
        List<String> keys = new ArrayList<String>(2);
        generator.keysFor(keys,
                messageView.getActionbar().getSections().get(0),
                section.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(2, keys.size());
        assertEquals("test-module.testMessageView.actionbar.sections.test-section.label", keys.get(0));
        assertEquals("test-module.testMessageView.actionbar.sections.test-section", keys.get(1));
    }

    /**
     * Sub-app with an action bar definition.
     * (Because the ContentSubAppDefinition is defined in a dependent module, so we cannot use it.)
     */
    private static class TestSubApp extends ConfiguredSubAppDescriptor {
        private ActionbarDefinition actionbar;

        public TestSubApp() {
            super();
        }

        public ActionbarDefinition getActionbar() {
            return actionbar;
        }

        public void setActionbar(ActionbarDefinition actionbar) {
            this.actionbar = actionbar;
        }
    }

    /**
     * A class that contains an actionbar, but is not an SubAppDescriptor - e.g. MessageViewDefinition.
     * But the MessageViewDefinition is in a dependent module, so we cannot use it here.
     */
    @I18nable
    private static class TestMessageView {
        private String id;
        private ActionbarDefinition actionbar;

        public TestMessageView(String id) {
            this.id = id;
        }

        public ActionbarDefinition getActionbar() {
            return actionbar;
        }

        public void setActionbar(ActionbarDefinition actionbar) {
            this.actionbar = actionbar;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

}
