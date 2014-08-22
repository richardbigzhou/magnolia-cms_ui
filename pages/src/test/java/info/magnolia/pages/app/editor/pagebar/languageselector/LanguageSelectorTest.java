/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.pagebar.languageselector;

import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.parameters.DefaultPageEditorStatus;
import info.magnolia.pages.app.editor.parameters.PageEditorStatus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link LanguageSelector}.
 */
public class LanguageSelectorTest {

    private LanguageSelector selector;
    private DefaultI18NAuthoringSupport i18NAuthoringSupport;
    private I18nContentSupport i18nContentSupport;
    private MockSession session;
    private LanguageSelectorView view;

    @Before
    public void setUp() throws Exception {

        MockWebContext ctx = new MockWebContext();
        this.session = new MockSession(RepositoryConstants.WEBSITE);

        ctx.addSession(null, session);
        MockRepositoryAcquiringStrategy strategy = new MockRepositoryAcquiringStrategy();
        strategy.addSession(RepositoryConstants.WEBSITE, session);
        ctx.setRepositoryStrategy(strategy);
        MgnlContext.setInstance(ctx);

        this.i18NAuthoringSupport = mock(DefaultI18NAuthoringSupport.class);
        this.i18nContentSupport = mock(I18nContentSupport.class);

        when(i18NAuthoringSupport.createI18NURI(any(Node.class), any(Locale.class))).thenReturn("/");

        view = mock(LanguageSelectorView.class);
        PageEditorPresenter pageEditorPresenter = mock(PageEditorPresenter.class);
        PageEditorStatus pageEditorStatus = new DefaultPageEditorStatus(mock(DefaultI18NAuthoringSupport.class));
        when(pageEditorPresenter.getStatus()).thenReturn(pageEditorStatus);
        this.selector = new LanguageSelector(view, i18NAuthoringSupport, i18nContentSupport, pageEditorPresenter);

    }

    @Test
    public void testDifferentI18NContentSupportSettingsForDifferentPages() throws RepositoryException {
        // GIVEN
        List<Locale> locales1 = Arrays.asList(new Locale[]{Locale.GERMAN, Locale.FRENCH});
        List<Locale> locales2 = Arrays.asList(new Locale[] {Locale.ENGLISH, Locale.JAPAN});
        Node node1 = session.getRootNode().addNode("test1");
        Node node2 = session.getRootNode().addNode("test2");

        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);

        when(i18NAuthoringSupport.createI18NURI(eq(node1), any(Locale.class))).thenReturn("/test1");
        when(i18NAuthoringSupport.createI18NURI(eq(node2), any(Locale.class))).thenReturn("/test2");
        when(i18NAuthoringSupport.getAvailableLocales(node1)).thenReturn(locales1);
        when(i18NAuthoringSupport.getAvailableLocales(node2)).thenReturn(locales2);
        when(i18NAuthoringSupport.getDefaultLocale(node1)).thenReturn(Locale.GERMAN);
        when(i18NAuthoringSupport.getDefaultLocale(node2)).thenReturn(Locale.ENGLISH);

        // WHEN
        selector.onLocationUpdate(new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/test1", null));

        // THEN
        verify(view).setAvailableLanguages(locales1);
        verify(view).setCurrentLanguage(Locale.GERMAN);

        // WHEN
        selector.onLocationUpdate(new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, "/test2", null));

        // THEN
        verify(view).setAvailableLanguages(locales2);
        verify(view).setCurrentLanguage(Locale.ENGLISH);
    }
}
