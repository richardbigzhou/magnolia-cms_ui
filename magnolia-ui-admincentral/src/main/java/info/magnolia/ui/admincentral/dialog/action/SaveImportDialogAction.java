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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.commands.impl.ImportCommand;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Call Import Command in order to perform the import action.
 *
 */
public class SaveImportDialogAction extends AbstractAction<SaveImportDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveImportDialogAction.class);

    private final Item item;
    private CommandsManager commandsManager;
    private EditorValidator validator;
    private EditorCallback callback;

    private Map<String, Object> params;

    public SaveImportDialogAction(SaveImportDialogActionDefinition definition, final Item item, final CommandsManager commandsManager, final EditorValidator validator, final EditorCallback callback) {
        super(definition);
        this.item = item;
        this.commandsManager = commandsManager;
        this.validator = validator;
        this.callback = callback;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;
            JcrNodeAdapter importXml = (JcrNodeAdapter) itemChanged.getChild("import");
            if (importXml != null) {
                executeCommand(itemChanged);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No Import performed.");
        }

    }

    public final void executeCommand(JcrNodeAdapter itemChanged) throws ActionExecutionException {

        final String commandName = getDefinition().getCommand();
        final String catalog = getDefinition().getCatalog();
        final Command command = this.commandsManager.getCommand(catalog, commandName);

        if (command == null) {
            throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", commandName));
        }

        long start = System.currentTimeMillis();
        try {
            // Set the parameter used by the command.
            setParams(itemChanged);
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", new Object[] { commandName, catalog, params });
            commandsManager.executeCommand(command, params);
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start);
            throw new ActionExecutionException(e);
        }
    }

    private void setParams(JcrNodeAdapter itemChanged) throws RepositoryException {
        params = new HashMap<String, Object>();
        JcrNodeAdapter importXml = (JcrNodeAdapter) itemChanged.getChild("import");

        params.put(ImportCommand.IMPORT_XML_STREAM, ((DefaultProperty<BinaryImpl>) importXml.getItemProperty(JcrConstants.JCR_DATA)).getValue().getStream());
        params.put(ImportCommand.IMPORT_IDENTIFIER_BEHAVIOR, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        params.put(ImportCommand.IMPORT_XML_FILE_NAME, importXml.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        params.put("repository", itemChanged.getWorkspace());
        params.put("path", itemChanged.getJcrItem().getPath());
    }

}
