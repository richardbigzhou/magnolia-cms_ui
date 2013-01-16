/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.ui.legacy.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.Components;

/**
 * The deactivation command which do real deactivation.
 * 
 * @author jackie
 */
public class DeactivationCommand extends BaseRepositoryCommand {

    @Override
    public boolean execute(Context ctx) throws Exception {
        try{
            Syndicator syndicator = Components.getComponentProvider().newInstance(Syndicator.class);
            syndicator.init(ctx.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this.getRepository()), new Rule());
            final Content node = getNode(ctx);
            syndicator.deactivate(node);
        }
        catch(Exception e){
            log.error("Exception caught during deactivation.", e);
            AlertUtil.setException(MessagesManager.get("tree.error.deactivate"), e, ctx);
            return false;
        }
        return true;
    }

}
