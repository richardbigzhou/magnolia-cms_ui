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
package info.magnolia.ui.form;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for dialog items, provides resolution of {@link Messages} in the hierarchical.
 *
 * @see Messages
 * @see FormItem
 */
public abstract class AbstractFormItem implements FormItem {

    private FormItem parent;

    private static String[] UI_BASENAMES;

    static {
        String uiPackagePrefix = "info.magnolia.ui.";
        String[] uiModules = {"model", "framework", "widget.actionbar", "widget.dialog", "widget.editor", "widget.magnoliashell", "widget.tabsheet", "vaadin.integration"};
        List<String> basenames = new ArrayList<String>();
        for (String module : uiModules) {
            basenames.add(uiPackagePrefix + module + ".messages");
        }
        UI_BASENAMES = basenames.toArray(new String[]{});
    }

    @Override
    public void setParent(FormItem parent) {
        this.parent = parent;
    }

    @Override
    public FormItem getParent() {
        return parent;
    }

    @Override
    public Messages getMessages() {
        Messages messages = null;
        if (getParent() != null) {
            messages = getParent().getMessages();
        } else {
            messages = MessagesUtil.chain(UI_BASENAMES);
        }
        if (StringUtils.isNotBlank(getI18nBasename())) {
            messages = MessagesUtil.chain(getI18nBasename(), messages);
        }
        return messages;
    }

    protected abstract String getI18nBasename();

    public String getMessage(String key) {
        return getMessages().getWithDefault(key, key);
    }
}
