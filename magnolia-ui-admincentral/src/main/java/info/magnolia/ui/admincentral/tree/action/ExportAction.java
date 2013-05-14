/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ExportCommand;
import info.magnolia.ui.admincentral.tree.action.export.ExportStreamer;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.action.CommandActionBase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.io.OutputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Item;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * UI action that allows to export a Node in a XML format.<br>
 * This Export Action calls the Export command in order to create an XML
 * representation of the node structure. <br>
 * Call then the {@link ExportStreamer#openFileInNewWindow} in order to push XML
 * representation to the Client side.
 */
public class ExportAction extends CommandActionBase<ExportActionDefinition> {

    private OutputStream outputStream;

    @Inject
    public ExportAction(ExportActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, SubAppContext subAppContext) {
        super(definition, item, commandsManager, subAppContext);
    }

    /**
     * Execute Post Command action. The Command create and put the XML tree
     * representation into the OutputStream<br>
     * . Now we have to push it to the client side.
     */
    @Override
    protected void onPostExecute() throws Exception {
        ExportCommand exportCommand = (ExportCommand) getCommand();
        ExportStreamer.openFileInBlankWindow(exportCommand.getFileName(), ((ByteArrayOutputStream) exportCommand.getOutputStream()).toByteArray(), exportCommand.getMimeExtension());
        if (outputStream != null) {
            outputStream.close();
        }
    }

    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put(ExportCommand.EXPORT_EXTENSION, ".xml");
        params.put(ExportCommand.EXPORT_FORMAT, Boolean.TRUE);
        params.put(ExportCommand.EXPORT_KEEP_HISTORY, Boolean.TRUE);
        outputStream = new ByteArrayOutputStream();
        params.put(ExportCommand.EXPORT_OUTPUT_STREAM, outputStream);
        return params;
    }
}
