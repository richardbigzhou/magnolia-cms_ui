/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.server;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;

/**
 * A subclass of {@link StreamResource} which makes sure #getStream always returns the same instance of {@link DownloadStream}.<br/>
 * (The superclass always creates a new instance of {@link DownloadStream} when calling #getStream, that way DownloadStream#params gets lost and makes it difficult to add custom response headers.
 * See JIRA MGNLUI-3274.)
 */
public class DownloadStreamResource extends StreamResource {

    private DownloadStream downloadStream = null;

    /**
     * Creates a new stream resource for downloading from stream.
     *
     * @param streamSource the source Stream.
     * @param filename the name of the file.
     */
    public DownloadStreamResource(StreamSource streamSource, String filename) {
        super(streamSource, filename);
    }

    /**
     * Returns the {@link DownloadStream} instance.
     * Unlike the method of the superclass, this one doesn't create a new instance if it is already existing.
     */
    @Override
    public DownloadStream getStream() {
        if (downloadStream == null) {
            downloadStream = super.getStream();
        }
        return downloadStream;
    }

}
