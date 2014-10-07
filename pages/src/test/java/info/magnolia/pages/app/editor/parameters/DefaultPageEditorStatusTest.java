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
package info.magnolia.pages.app.editor.parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultPageEditorStatus}.
 */
public class DefaultPageEditorStatusTest {

    private PageEditorStatus status;
    private MockSession session;

    @Before
    public void setUp() throws Exception {
        MockWebContext ctx = new MockWebContext();
        this.session = new MockSession(RepositoryConstants.WEBSITE);
        ctx.addSession(null, session);
        MockRepositoryAcquiringStrategy strategy = new MockRepositoryAcquiringStrategy();
        strategy.addSession(RepositoryConstants.WEBSITE, session);
        ctx.setRepositoryStrategy(strategy);
        MgnlContext.setInstance(ctx);

        I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);

        I18NAuthoringSupport i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        when(i18NAuthoringSupport.createI18NURI(any(Node.class), any(Locale.class))).thenReturn("/");
        this.status = new DefaultPageEditorStatus(i18NAuthoringSupport);
    }

    @Test
    public void testUpdateStatusFromLocation() throws Exception {
        session.getRootNode().addNode("test");
        DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/test", "1.2");
        status.updateStatusFromLocation(location);


        // WHEN
        String url = status.getParameters().getUrl();

        assertThat(url, containsString(DefaultPageEditorStatus.PREVIEW_PARAMETER + "=false"));
        assertThat(url, containsString(DefaultPageEditorStatus.CHANNEL_PARAMETER + "=" + PlatformType.DESKTOP.getId()));
        assertThat(url, containsString(DefaultPageEditorStatus.VERSION_PARAMETER + "=1.2"));
    }

    @Test
    public void testUpdateStatusFromLocationWithPlatform() throws Exception {
        session.getRootNode().addNode("test");
        DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, "/test", null);
        status.updateStatusFromLocation(location);
        status.setPlatformType(PlatformType.MOBILE);


        // WHEN
        String url = status.getParameters().getUrl();

        assertThat(url, containsString(DefaultPageEditorStatus.PREVIEW_PARAMETER + "=true"));
        assertThat(url, containsString(DefaultPageEditorStatus.CHANNEL_PARAMETER + "=" + PlatformType.MOBILE.getId()));
        assertThat(url, not(containsString(DefaultPageEditorStatus.VERSION_PARAMETER)));
    }

    @Test
    public void testHasLocationChanged() throws Exception {
        session.getRootNode().addNode("test");
        DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/test", "1.2");
        status.updateStatusFromLocation(location);


        // WHEN
        boolean notChanged = status.isLocationChanged(new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/test", "1.2"));
        boolean versionChanged = status.isLocationChanged(new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/test", null));
        boolean viewTypeChanged = status.isLocationChanged(new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, "/test", "1.2"));

        assertFalse(notChanged);
        assertTrue(versionChanged);
        assertTrue(viewTypeChanged);
    }


}