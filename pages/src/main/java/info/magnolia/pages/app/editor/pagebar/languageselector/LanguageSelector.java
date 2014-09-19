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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selector for the {@link Locale}s available on the page.
 */
public class LanguageSelector implements LanguageSelectorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(LanguageSelector.class);

    private LanguageSelectorView view;
    private I18NAuthoringSupport i18NAuthoringSupport;
    private I18nContentSupport i18nContentSupport;
    private PageEditorPresenter pageEditorPresenter;

    @Inject
    public LanguageSelector(LanguageSelectorView view, I18NAuthoringSupport i18NAuthoringSupport, I18nContentSupport i18nContentSupport, PageEditorPresenter pageEditorPresenter) {
        this.view = view;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
        this.i18nContentSupport = i18nContentSupport;
        this.pageEditorPresenter = pageEditorPresenter;
    }

    public View start() {
        view.setListener(this);
        return view;
    }

    @Override
    public void languageSelected(Locale locale) {
        Locale currentLocale = pageEditorPresenter.getStatus().getLocale();
        if (locale != null && !locale.equals(currentLocale)) {
            if (i18NAuthoringSupport instanceof DefaultI18NAuthoringSupport) {
                ((DefaultI18NAuthoringSupport) i18NAuthoringSupport).setAuthorLocale(locale);
            }
            pageEditorPresenter.getStatus().setLocale(locale);

            // only update the page editor if the change came from an actual switch of language.
            if (currentLocale != null) {
                pageEditorPresenter.loadPageEditor();
            }
            pageEditorPresenter.getStatus().setLocale(locale);
        }
    }

    public void onLocationUpdate(DetailLocation location) {
        try {
            Node node = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).getNode(location.getNodePath());
            List<Locale> locales = i18NAuthoringSupport.getAvailableLocales(node);
            view.setAvailableLanguages(locales);

            Locale currentLocale = pageEditorPresenter.getStatus().getLocale();
            Locale locale = currentLocale != null && locales.contains(currentLocale) ? currentLocale : getDefaultLocale(node);
            view.setCurrentLanguage(locale);
        } catch (RepositoryException e) {
            log.error("Unable to get node [{}] from workspace [{}]", location.getNodePath(), RepositoryConstants.WEBSITE, e);
        }
    }

    /**
     * Returns the default locale for the given page.
     *
     * TODO: Once {@link DefaultI18NAuthoringSupport#getDefaultLocale(javax.jcr.Node)} is added to {@link I18NAuthoringSupport} this method should go.
     */
    private Locale getDefaultLocale(Node node) {
        Locale locale;
        if (i18NAuthoringSupport instanceof DefaultI18NAuthoringSupport) {
            locale = ((DefaultI18NAuthoringSupport)i18NAuthoringSupport).getDefaultLocale(node);
        } else {
            locale = i18nContentSupport.getDefaultLocale();
        }
        return locale;
    }


    public void deactivate() {
        view.setVisible(false);
    }
}
