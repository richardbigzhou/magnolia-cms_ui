/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.thumbnail;

import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.net.URL;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;

/**
 * A component capable of displaying an image and holding a reference to the jcr node where the image is stored.
 *
 */
public class Thumbnail extends Embedded {
    public static final Resource IMAGE_NOT_FOUND = new ThemeResource("img/icons/icon-error-red.png");
    private JcrNodeAdapter node;

    public Thumbnail(final Node node, final String path) {
        this.node = new JcrNodeAdapter(node);
        setType(TYPE_IMAGE);
        setSizeUndefined();
        addStyleName("asset");
        if(path != null) {
            setSource(new ExternalResource(path));
        } else {
            setSource(IMAGE_NOT_FOUND);
        }
        try {
            setDescription(node.getName());
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JcrNodeAdapter getNode() {
        return node;
    }
}
