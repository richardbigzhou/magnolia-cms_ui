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

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.ui.framework.command.ImportZipCommand;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Triggers zip archive upload command.
 * @param <T> definition class.
 * @see info.magnolia.ui.framework.command.ImportZipCommand
 */
public class ZipUploadDialogAction<T extends ZipUploadActionDefinition> extends AbstractAction<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CommandsManager commandsManager;

    private Map<String, Object> params;

    private EditorValidator validator;

    private EditorCallback callback;

    private Item item;

    public ZipUploadDialogAction(T definition, final CommandsManager commandsManager, final EditorValidator validator, final EditorCallback callback, Item item) {
        super(definition);
        this.commandsManager = commandsManager;
        this.validator = validator;
        this.callback = callback;
        this.item = item;
    }

    @Override
    public void execute() throws ActionExecutionException {
        validator.showValidation(true);
        if (validator.isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;
            JcrNodeAdapter importNode = (JcrNodeAdapter) itemChanged.getChild("import");
            if (importNode != null) {
                executeCommand(itemChanged);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No Import performed.");
        }
    }

    private void executeCommand(JcrNodeAdapter itemChanged) throws ActionExecutionException {
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
        params.put(ImportZipCommand.STREAM_PROPERTY, ((DefaultProperty<BinaryImpl>) importXml.getItemProperty(JcrConstants.JCR_DATA)).getValue().getStream());
        params.put(ImportZipCommand.ENCODING_PROPERTY, itemChanged.getItemProperty(ImportZipCommand.ENCODING_PROPERTY).getValue());
        params.put("repository", itemChanged.getWorkspace());
        params.put("path", itemChanged.getJcrItem().getPath());
    }
}
