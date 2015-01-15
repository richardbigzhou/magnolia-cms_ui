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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.workbench.event.ItemEditedEvent;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Responsible for triggering (by sending an Event) the saving of the changes made to node properties in the Configuration app.
 * Sends an ItemEditedEvent so that the BrowserPresenter will handle updating the item in the same manner as if it had
 * been changed via inplace editing.
 * 
 * @see SaveConfigDialogActionDefinition
 */
public class SaveConfigDialogAction extends AbstractAction<SaveConfigDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveConfigDialogAction.class);

    protected final Item item;

    protected final EditorValidator validator;
    protected final EditorCallback callback;

    private final EventBus subAppEventBus;

    @Inject
    public SaveConfigDialogAction(SaveConfigDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus) {
        super(definition);
        this.item = item;
        this.validator = validator;
        this.callback = callback;
        this.subAppEventBus = subAppEventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            subAppEventBus.fireEvent(new ItemEditedEvent(item));
            callback.onSuccess(getDefinition().getName());
        } else {
            log.debug("Validation error(s) occurred. No save performed.");
        }
    }

}
