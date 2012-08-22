/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.field.translator;

import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.propertytranslator.PropertyTranslator;

/**
 * {@link PropertyTranslator} used to convert a UUID to a Path and Path to UUID.
 * In general, if the translation is not possible, return null.
 */
@SuppressWarnings("unchecked")
public class UuidToPathTranslator extends PropertyTranslator{
    private static final Logger log = LoggerFactory.getLogger(UuidToPathTranslator.class);
    private String workspace;

    public UuidToPathTranslator(String workspace) {
        this.workspace = workspace;
    }


    /**
     * Transform the UUID to Path.
     */
    @Override
    public Object translateFromDatasource(Object uuid) {
        String res = null;
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            res = session.getNodeByIdentifier(uuid.toString()).getPath();
        } catch (RepositoryException e) {
            log.error("Unable to convert UUID to Path",e);
        }
        return res;
    }
    /**
     * Transform the Path to UUID.
     */
    @Override
    public Object translateToDatasource(Object path) throws Exception {
        String res = null;
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            res = session.getNode(path.toString()).getIdentifier();
        } catch (RepositoryException e) {
            log.error("Unable to convert Path to UUID",e);
        }
        return res;
    }

}
