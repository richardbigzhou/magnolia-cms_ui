/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.ExportJcrNodeToYamlCommand;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.util.TempFileStreamResource;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Item;

import com.vaadin.server.Page;

/**
 * Action for exporting a node to YAML format. Uses the {@link ExportJcrNodeToYamlCommand} to perform the export operation.
 * Then sends the result to the client browser.
 *
 * @see ExportYamlActionDefinition
 */
public class ExportYamlAction extends AbstractCommandAction<ExportYamlActionDefinition> {

    private static final String YAML_EXTENSION = ".yaml";

    private TempFileStreamResource tempFileStreamResource;

    @Inject
    public ExportYamlAction(ExportYamlActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) throws ActionExecutionException {
        super(definition, item, commandsManager, uiContext, i18n);
    }

    @Override
    protected void onPreExecute() throws Exception {
        tempFileStreamResource = new TempFileStreamResource();
        tempFileStreamResource.setTempFileExtension(YAML_EXTENSION);
        tempFileStreamResource.setTempFileName(getCurrentItem().getItemId().getUuid());

        super.onPreExecute();
    }

    /**
     * After command execution we push the created YAML to the client browser.<br>
     * The created data is put in the temporary file 'fileOutput' linked to 'fileOutputStream' sent to the export YAML command.<br>
     * This temporary file is the used to create a {@code FileInputStream} that ensure that this temporary file is removed once the <br>
     * fileInputStream is closed by Vaadin resource component.
     * Directs the created YAML file to the user.
     */
    @Override
    protected void onPostExecute() throws Exception {
        final ExportJcrNodeToYamlCommand exportYamlCommand = (ExportJcrNodeToYamlCommand) getCommand();
        tempFileStreamResource.setFilename(exportYamlCommand.getFileName());
        // Directs the created file to user.
        Page.getCurrent().open(tempFileStreamResource, "", true);
    }

    @Override
    protected Map<String, Object> buildParams(Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        try {
            params.put(ExportJcrNodeToYamlCommand.EXPORT_OUTPUT_STREAM, tempFileStreamResource.getTempFileOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to bind command to temp file output stream: ", e);
        }
        return params;
    }
}
