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
package info.magnolia.ui.framework.action;

import info.magnolia.cms.core.Path;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ExportCommand;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.util.FileDownloader;
import info.magnolia.ui.framework.util.FileDownloaderImpl;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Item;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Action for exporting a node to XML format. Uses the export command to perform the serialization to XML then sends the
 * result to the client browser.
 *
 * @see ExportActionDefinition
 */
public class ExportAction extends AbstractCommandAction<ExportActionDefinition> {

    private File fileOutput;
    private FileOutputStream fileOutputStream;

    private final FileDownloader fileDownloader;

    @Inject
    public ExportAction(ExportActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n, FileDownloader fileDownloader) throws ActionExecutionException {
        super(definition, item, commandsManager, uiContext, i18n);
        this.fileDownloader = fileDownloader;
        try {
            // Create a temporary file that will hold the data created by the export command.
            fileOutput = File.createTempFile(item.getItemId().getUuid(), ".xml", Path.getTempDirectory());
            // Create a FileOutputStream link to the temporary file. The command use this FileOutputStream to populate data.
            fileOutputStream = new FileOutputStream(fileOutput);
        } catch (Exception e) {
            throw new ActionExecutionException("Not able to create a temporary file.", e);
        }
    }

    /**
     * @deprecated since 5.4.1, use {@link #ExportAction(ExportActionDefinition, JcrItemAdapter, CommandsManager, UiContext, SimpleTranslator, FileDownloader)} instead.
     */
    @Deprecated
    public ExportAction(ExportActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) throws ActionExecutionException {
        this(definition, item, commandsManager, uiContext, i18n, new FileDownloaderImpl());
    }

    /**
     * After command execution we push the created XML to the client browser.<br>
     * The created data is put in the temporary file 'fileOutput' linked to 'fileOutputStream' sent to the export command.<br>
     * This temporary file is the used to create a {@code FileInputStream} that ensure that this temporary file is removed once the <br>
     * fileInputStream is closed by Vaadin resource component.
     */
    @Override
    protected void onPostExecute() throws Exception {
        try {
            ExportCommand exportCommand = (ExportCommand) getCommand();
            fileDownloader.downloadFile(exportCommand.getFileName(), exportCommand.getMimeExtension(), FileUtils.openInputStream(fileOutput));
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
            FileUtils.deleteQuietly(fileOutput);
        }
    }

    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put(ExportCommand.EXPORT_EXTENSION, ".xml");
        params.put(ExportCommand.EXPORT_FORMAT, Boolean.TRUE);
        params.put(ExportCommand.EXPORT_KEEP_HISTORY, Boolean.FALSE);
        params.put(ExportCommand.EXPORT_OUTPUT_STREAM, fileOutputStream);
        return params;
    }
}
