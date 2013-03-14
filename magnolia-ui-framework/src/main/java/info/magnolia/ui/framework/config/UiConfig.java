/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.framework.config;

import info.magnolia.ui.model.action.builder.ActionConfig;
import info.magnolia.ui.actionbar.definition.builder.ActionbarConfig;
import info.magnolia.ui.dialog.config.DialogConfig;
import info.magnolia.ui.form.config.ValidatorConfig;
import info.magnolia.ui.form.config.FieldsConfig;
import info.magnolia.ui.form.config.FormConfig;
import info.magnolia.ui.workbench.config.WorkbenchConfig;
import info.magnolia.ui.workbench.config.ColumnConfig;

/**
 * Main config object for creating UI definitions.
 */
public class UiConfig {

    public final WorkbenchConfig workbenches = new WorkbenchConfig();
    public final ActionbarConfig actionbars = new ActionbarConfig();
    public final FormConfig forms = new FormConfig();
    public final DialogConfig dialogs = new DialogConfig();
    public final ActionConfig actions = new ActionConfig();
    public final FieldsConfig fields = new FieldsConfig();
    public final ColumnConfig columns = new ColumnConfig();
    public final ValidatorConfig validators = new ValidatorConfig();
}
