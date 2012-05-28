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
package info.magnolia.ui.admincentral.components;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

/**
 * Entry in the feed (pulse).
 * @version $Id$
 */
@SuppressWarnings("serial")
public class ActivityItem extends CustomLayout {

    public ActivityItem(String type, String description, String comment, String status, Date date){
        try {
            //FIXME fgrilli a workaround to get the custom layout template contents (Vaadin could not find it)
            final InputStream inputStream = ClasspathResourcesUtil.getStream("/VAADIN/themes/testtheme/layouts/activityItem.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String templateContents = writer.toString();
            setTemplateContents(templateContents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM HH:mm yyyy");
        addStyleName(type);
        final Label statusLabel = new Label();
        statusLabel.addStyleName("status");
        statusLabel.addStyleName(status);
        statusLabel.setSizeUndefined();
        addComponent(statusLabel, "status");
        addComponent(new Label(description), "description");
        if (StringUtils.isNotBlank(comment)) {
            Label commentLabel = new Label(comment);
            commentLabel.setStyleName("comment-label");
            addComponent(commentLabel, "comment");
        }
        addComponent(new Label(formatter.format(date)), "time");

    }
}
