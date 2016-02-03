/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.form.action;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for saving Items in Forms.<br>
 * The name of the created node will be set to:<br>
 * <b>the value of the property called 'jcrName' if</b>
 * <ul>
 * <li>the node has no property called 'jcrName'</li>
 * </ul>
 * <b>the value of the property called 'name' if</b>
 * <ul>
 * <li>the node has a property called 'name' AND</li>
 * <li>the node has no property called 'jcrName' AND</li>
 * </ul>
 * <b>a default name if non of these conditions are valid</b> <br>
 * To change this behavior extend this Action and override {@link SaveFormAction#setNodeName(Node, JcrNodeAdapter)}
 *
 * @see SaveFormActionDefinition
 */
public class SaveFormAction extends AbstractAction<SaveFormActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveFormAction.class);

    protected EditorCallback callback;
    protected final EditorValidator validator;
    protected final JcrNodeAdapter item;

    public SaveFormAction(SaveFormActionDefinition definition, JcrNodeAdapter item, EditorCallback callback, EditorValidator validator) {
        super(definition);
        this.callback = callback;
        this.validator = validator;
        this.item = item;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            try {
                final Node node = item.applyChanges();
                // Set the Node name.
                setNodeName(node, item);
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No save performed.");
        }
    }

    /**
     * Set the node Name.
     * Node name is set to: <br>
     * the value of the property 'name' if it is present.
     */
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        String propertyName = "name";
        if (node.hasProperty(propertyName) && !node.hasProperty(ModelConstants.JCR_NAME)) {
            Property property = node.getProperty(propertyName);
            String newNodeName = property.getString();
            if (!node.getName().equals(Path.getValidatedLabel(newNodeName))) {
                newNodeName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), Path.getValidatedLabel(newNodeName));
                item.setNodeName(newNodeName);
                NodeUtil.renameNode(node, newNodeName);
            }
        }

    }
}
