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
package info.magnolia.security.app.dialog.field;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SystemLanguagesFieldFactory}.
 */
public class SystemLanguagesFieldFactoryTest extends AbstractFieldFactoryTestCase<SystemLanguagesFieldDefinition> {

    private SystemLanguagesFieldFactory<SystemLanguagesFieldDefinition> systemLanguagesFieldFactory;

    @Override
    protected void createConfiguredFieldDefinition() {
        this.definition = new SystemLanguagesFieldDefinition();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockContext ctx = new MockContext();
        MockSession session = new MockSession(RepositoryConstants.CONFIG);

        Node configRoot = session.getRootNode();
        Node systemLanguages = NodeUtil.createPath(configRoot, SystemLanguagesFieldFactory.SYSTEM_LANGUAGES_PATH.substring(1), NodeTypes.ContentNode.NAME);
        // English
        Node english = NodeUtil.createPath(systemLanguages, "en", NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(english, "language", "en");
        PropertyUtil.setProperty(english, "enabled", true);
        // Swiss German
        Node swissGerman = NodeUtil.createPath(systemLanguages, "de_CH", NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(swissGerman, "language", "de");
        PropertyUtil.setProperty(swissGerman, "country", "CH");
        PropertyUtil.setProperty(swissGerman, "enabled", true);
        // French - disabled
        Node french = NodeUtil.createPath(systemLanguages, "fr", NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(french, "language", "fr");
        PropertyUtil.setProperty(french, "enabled", false);

        ctx.addSession(RepositoryConstants.CONFIG, session);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);

        JcrNodeAdapter item = new JcrNodeAdapter(session.getRootNode());
        systemLanguagesFieldFactory = new SystemLanguagesFieldFactory<>(definition, item, MgnlContext.getInstance());
    }

    @Test
    public void languageNamesInEnglish() throws Exception {
        // GIVEN
        MgnlContext.setLocale(Locale.ENGLISH);

        // WHEN
        List<SelectFieldOptionDefinition> options = systemLanguagesFieldFactory.getSelectFieldOptionDefinition();

        // THEN
        assertEquals(2, options.size());
        assertEquals("en", options.get(0).getValue());
        assertEquals((new Locale("en")).getDisplayLanguage(Locale.ENGLISH), options.get(0).getLabel());
        assertEquals("de_CH", options.get(1).getValue());
        Locale swissGerman = new Locale("de", "CH");
        assertEquals(swissGerman.getDisplayLanguage(Locale.ENGLISH) + " (" + swissGerman.getDisplayCountry(Locale.ENGLISH) + ")", options.get(1).getLabel());
    }

    @Test
    public void languageNamesInGerman() throws Exception {
        // GIVEN
        MgnlContext.setLocale(Locale.GERMAN);

        // WHEN
        List<SelectFieldOptionDefinition> options = systemLanguagesFieldFactory.getSelectFieldOptionDefinition();

        // THEN
        assertEquals(2, options.size());
        assertEquals("en", options.get(0).getValue());
        assertEquals((new Locale("en")).getDisplayLanguage(Locale.GERMAN), options.get(0).getLabel());
        assertEquals("de_CH", options.get(1).getValue());
        Locale swissGerman = new Locale("de", "CH");
        assertEquals(swissGerman.getDisplayLanguage(Locale.GERMAN) + " (" + swissGerman.getDisplayCountry(Locale.GERMAN) + ")", options.get(1).getLabel());
    }

    @Test
    public void selectedLanguageShouldBeTheOneSetInContext() throws Exception {
        // GIVEN
        MgnlContext.setLocale(Locale.GERMAN);

        // WHEN
        List<SelectFieldOptionDefinition> options = systemLanguagesFieldFactory.getSelectFieldOptionDefinition();

        // THEN
        assertTrue(options.get(1).isSelected());
    }
}
