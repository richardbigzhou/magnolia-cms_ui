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
package info.magnolia.ui.admincentral;

import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ListenerMethod.MethodException;
import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.ServerRpcManager.RpcInvocationException;

/**
 * The {@link AdmincentralErrorHandler} logs unhandled exceptions and sends error messages to the pulse.
 * <p>
 * It replaces Vaadin's default behavior for component errors, which would otherwise display error icons and stack traces in tooltips.
 */
public class AdmincentralErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(AdmincentralErrorHandler.class);

    private static final String DEFAULT_MESSAGE = "AdmincentralUI has encountered an unhandled exception.";

    private final MessagesManager messagesManager;

    public AdmincentralErrorHandler(MessagesManager messagesManager) {
        this.messagesManager = messagesManager;
    }

    @Override
    public void error(ErrorEvent event) {
        log.error(DEFAULT_MESSAGE, event.getThrowable());

        Message message = getErrorMessage(event.getThrowable());
        messagesManager.sendLocalMessage(message);
    }

    private Message getErrorMessage(Throwable e) {

        Message message = new Message();
        message.setType(MessageType.ERROR);

        addMessageDetails(message, e);

        // append details for RPC exceptions
        if (e instanceof RpcInvocationException) {
            e = e.getCause();
            addMessageDetails(message, e);
        }
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
            addMessageDetails(message, e);
        }
        if (e instanceof MethodException) {
            e = e.getCause();
            addMessageDetails(message, e);
        }

        // append other potential causes
        while (e != null && e != e.getCause()) {
            e = e.getCause();
            addMessageDetails(message, e);
        }

        if (StringUtils.isBlank(message.getSubject())) {
            message.setSubject(DEFAULT_MESSAGE);
        }

        return message;
    }

    private void addMessageDetails(Message message, Throwable e) {
        if (e != null) {

            // message details
            String content = message.getMessage();
            if (content == null) {
                content = "";
            } else {
                content += "\ncaused by ";
            }
            content += e.getClass().getSimpleName();
            if (StringUtils.isNotBlank(e.getMessage())) {
                content += ": " + e.getMessage();

                // message subject
                message.setSubject(e.getMessage());
            }
            message.setMessage(content);
        }
    }
}
