/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
package info.magnolia.ui.form.field.converter;

import info.magnolia.context.MgnlContext;

import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter used to convert a UUID to a Path and Path to UUID.
 * In general, if the translation is not possible, return null.
 */
public class BaseIdentifierToPathConverter implements IdentifierToPathConverter {

    private static final Logger log = LoggerFactory.getLogger(BaseIdentifierToPathConverter.class);

    private String workspace;

    @Override
    public String convertToModel(String path, Class<? extends String> targetType, Locale locale) throws ConversionException {
        // Null is required for the property to be removed if path is empty
        String res = null;
        if (StringUtils.isBlank(path)) {
            return res;
        }
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            res = session.getNode(path).getIdentifier();
        } catch (RepositoryException e) {
            log.error("Unable to convert Path to UUID", e);
        }
        return res;
    }

    @Override
    public String convertToPresentation(String uuid, Class<? extends String> targetType, Locale locale) throws ConversionException {
        String res = StringUtils.EMPTY;
        if (StringUtils.isBlank(uuid)) {
            return res;
        }
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            res = session.getNodeByIdentifier(uuid).getPath();
        } catch (RepositoryException e) {
            log.error("Unable to convert UUID to Path", e);
        }
        return res;
    }

    @Override
    public Class<String> getModelType() {
        return String.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }

    @Override
    public void setWorkspaceName(String workspaceName) {
        this.workspace = workspaceName;
    }


}
