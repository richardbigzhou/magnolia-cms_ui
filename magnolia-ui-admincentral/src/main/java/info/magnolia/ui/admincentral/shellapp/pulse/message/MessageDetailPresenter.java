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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.AbstractPulseDetailPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.vaadin.integration.MessageItem;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.util.BeanItem;

/**
 * The message detail presenter.
 */
public final class MessageDetailPresenter extends AbstractPulseDetailPresenter<Message> {

    @Inject
    public MessageDetailPresenter(Message message, PulseDetailView view, PulseDetailActionExecutor itemActionExecutor, AvailabilityChecker checker, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer) {
        super(message, view, itemActionExecutor, checker, itemViewDefinitionRegistry, formbuilder, actionbarPresenter, i18nizer);
    }

    @Override
    protected String getItemViewName() {
        return StringUtils.defaultString(item.getView(), DEFAULT_VIEW);
    }

    @Override
    protected void setItemViewTitle(PulseDetailView view) {
        view.setTitle(item.getSubject());
    }

    @Override
    protected BeanItem<Message> asBeanItem() {
        return new MessageItem(item);
    }
}
