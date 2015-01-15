/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor;
import info.magnolia.ui.contentapp.ContentApp;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes descriptor type for a single content app.
 */
public class AppNodeVisitor implements NodeVisitor {

    public static final String CLASS_PROPERTY_NAME  = "class";
    private final static String APP_CLASS_PROPERTY_NAME = "appClass";
    private final static String OBSOLETE_APP_PROPERTY_NAME = "app"; // was in Magnolia 5.0

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void visit(Node node) throws RepositoryException {
        if (node.hasProperty(APP_CLASS_PROPERTY_NAME)) {
            Property p = node.getProperty(APP_CLASS_PROPERTY_NAME);
            try {
                Class<?> clazz = Class.forName(p.getValue().getString());
                if (ContentApp.class.isAssignableFrom(clazz)) {
                    node.setProperty(CLASS_PROPERTY_NAME, ConfiguredContentAppDescriptor.class.getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                log.error("Failed to resolve app class: " +  p.getValue().getString(), e);
            }
        }
    }
}


