/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.mediaeditor.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.mediaeditor.MediaEditorEventBus;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;

/**
 * Abstract action for media editor related operations.
 */
public abstract class MediaEditorAction extends AbstractAction<MediaEditorActionDefinition> {

    private Logger log = Logger.getLogger(getClass());

    protected final static String DEFAULT_FORMAT = "png";

    protected EditHistoryTrackingProperty dataSource;

    protected EventBus eventBus;

    public MediaEditorAction(MediaEditorActionDefinition definition, EditHistoryTrackingProperty dataSource, @Named(MediaEditorEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.dataSource = dataSource;
        this.eventBus = eventBus;
    }

    protected InputStream createStreamSource(final BufferedImage img, final String formatName) {
        ByteArrayOutputStream out2 = null;
        try {
            if (img == null) {
                return null;
            }
            out2 = new ByteArrayOutputStream();
            ImageIO.write(img, formatName, out2);
            return new ByteArrayInputStream(out2.toByteArray());
        } catch (IOException e) {
            log.error("Error occurred while creating image stream: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out2);
        }
        return null;
    }
}
