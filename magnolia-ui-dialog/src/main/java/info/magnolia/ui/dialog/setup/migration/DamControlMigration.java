/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.dialog.setup.migration;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Migrate an Dam control to a AssetLinkField.
 */
public class DamControlMigration implements ControlMigration {


    @Override
    public void migrate(Node controlNode) throws RepositoryException {
        controlNode.getProperty("controlType").remove();

        controlNode.setProperty("targetWorkspace", "dam");
        controlNode.setProperty("appName", "assets");
        controlNode.setProperty("class", LinkFieldDefinition.class.getName());
        if (!controlNode.hasNode("identifierToPathConverter")) {
            controlNode.addNode("identifierToPathConverter", NodeTypes.ContentNode.NAME);
        }
        controlNode.getNode("identifierToPathConverter").setProperty("class", "info.magnolia.dam.app.assets.field.translator.AssetCompositeIdKeyTranslator");

        controlNode.addNode("contentPreviewDefinition", NodeTypes.ContentNode.NAME);
        controlNode.getNode("contentPreviewDefinition").setProperty("contentPreviewClass", "info.magnolia.dam.asset.field.DamFilePreviewComponent");

        if (!controlNode.hasProperty("description")) {
            controlNode.setProperty("description", "Select an asset");
        }

        if (!controlNode.hasProperty("label")) {
            controlNode.setProperty("label", "Image");
        }

        if (controlNode.hasProperty("repository")) {
            controlNode.getProperty("repository").remove();
        }

    }
}
