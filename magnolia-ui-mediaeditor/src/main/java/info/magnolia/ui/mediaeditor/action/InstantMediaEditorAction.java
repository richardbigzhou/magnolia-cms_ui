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
package info.magnolia.ui.mediaeditor.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.mediaeditor.MediaEditorEventBus;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;

/**
 * Instantly modifies the data without installation of any UI components.
 */
public abstract class InstantMediaEditorAction extends MediaEditorAction {

    private Logger log = Logger.getLogger(getClass());

    public InstantMediaEditorAction(MediaEditorActionDefinition definition, EditHistoryTrackingProperty dataSource, @Named(MediaEditorEventBus.NAME) EventBus eventBus) {
        super(definition, dataSource, eventBus);
    }

    @Override
    public void execute() throws ActionExecutionException {
        dataSource.startAction(StringUtils.lowerCase(getDefinition().getLabel()));
        InputStream result = new ByteArrayInputStream(dataSource.getValue());
        try {
            result = performModification(result);
            dataSource.setValue(IOUtils.toByteArray(result));
            eventBus.fireEvent(new MediaEditorInternalEvent(MediaEditorInternalEvent.EventType.APPLY));
        } catch (IOException e) {
            log.error("Failed to perform instant operation on media.", e);
            IOUtils.closeQuietly(result);
        }
    }

    protected abstract InputStream performModification(InputStream stream) throws IOException;
}
