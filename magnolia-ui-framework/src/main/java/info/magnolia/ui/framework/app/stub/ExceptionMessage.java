/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.app.stub;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.util.EscapeUtil;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Convenience class capable of crating a {@link info.magnolia.ui.api.message.Message} based on the
 * exception passed as a c-tor argument.
 */
class ExceptionMessage extends Message {

    public static final String STACK_TRACE_KEY = "ui-framework.message.exception.stacktrace";
    public static final String EXCEPTION_MSG_KEY = "ui-framework.message.exception.message";

    private Throwable relatedException;

    private SimpleTranslator i18n;

    ExceptionMessage(Throwable relatedException, String subjectMessage, SimpleTranslator i18n) {
        this.relatedException = relatedException;
        this.i18n = i18n;
        setType(MessageType.ERROR);
        setSubject(subjectMessage);
        setMessage(prepareMessage());
    }

    private String prepareMessage() {
        final StringBuilder sb = new StringBuilder();
        if (relatedException.getLocalizedMessage() != null) {
            sb.append(i18n.translate(EXCEPTION_MSG_KEY)).append(" ").append(EscapeUtil.escapeXss(relatedException.getLocalizedMessage()));
            sb.append(". ");
        }
        sb.append(i18n.translate(STACK_TRACE_KEY)).append(" ").append(ExceptionUtils.getStackTrace(relatedException));
        return sb.toString();
    }


}
