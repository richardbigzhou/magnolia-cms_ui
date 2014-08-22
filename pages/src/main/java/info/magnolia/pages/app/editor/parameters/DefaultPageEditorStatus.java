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

import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;

import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link PageEditorStatus}.
 */
public class DefaultPageEditorStatus implements PageEditorStatus {

    private static final Logger log = LoggerFactory.getLogger(DefaultPageEditorStatus.class);

    private final I18NAuthoringSupport i18NAuthoringSupport;
    private Locale locale;
    private String version;
    private String nodePath;
    private DetailView.ViewType viewType;
    private PlatformType platFormType = PlatformType.DESKTOP;

    @Inject
    public DefaultPageEditorStatus(I18NAuthoringSupport i18NAuthoringSupport) {
        this.i18NAuthoringSupport = i18NAuthoringSupport;
    }

    @Override
    public void updateStatusFromLocation(DetailLocation location) {
        this.nodePath = location.getNodePath();
        this.version = location.getVersion();
        this.viewType = location.getViewType();
    }

    @Override
    public boolean isLocationChanged(DetailLocation location) {
        DetailView.ViewType viewType = location.getViewType();
        String path = location.getNodePath();

        if ((path.equals(nodePath) && DetailView.ViewType.VIEW.equals(viewType) == isPreview())
                && (location.getVersion() == null ? version == null : location.getVersion().equals(version))) {
            return false;
        }
        return true;
    }

    @Override
    public String getNodePath() {
        return nodePath;
    }

    @Override
    public PlatformType getPlatformType() {
        return platFormType;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isPreview() {
        return DetailView.ViewType.VIEW.equals(viewType);
    }

    @Override
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    @Override
    public void setPlatformType(PlatformType platform) {
        this.platFormType = platform;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public PageEditorParameters getParameters() {
        PageEditorParameters parameters = new PageEditorParameters();
        parameters.setContextPath(MgnlContext.getContextPath());
        parameters.setNodePath(nodePath);
        parameters.setPlatformType(platFormType);
        parameters.setPreview(isPreview());

        String uri = createUri(nodePath, isPreview(), version, platFormType, locale);
        parameters.setUrl(uri);
        return parameters;
    }

    protected String createUri(String nodePath, boolean isPreview, String version, PlatformType platformType, Locale locale) {
        String uri = "";
        try {
            Node node = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).getNode(nodePath);
            uri = i18NAuthoringSupport.createI18NURI(node, locale);
            StringBuffer sb = new StringBuffer(uri);


            LinkUtil.addParameter(sb, PREVIEW_PARAMETER, Boolean.toString(isPreview));

            LinkUtil.addParameter(sb, CHANNEL_PARAMETER, platformType.getId());

            if (StringUtils.isNotEmpty(version)) {
                LinkUtil.addParameter(sb, VERSION_PARAMETER, version);
            }
            uri = sb.toString();

        } catch (RepositoryException e) {
            log.error("Could not get page node from location object.", e);
        }
        return uri;
    }
}
