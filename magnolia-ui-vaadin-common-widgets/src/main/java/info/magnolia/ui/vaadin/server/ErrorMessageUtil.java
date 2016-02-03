/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.vaadin.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vaadin.server.CompositeErrorMessage;
import com.vaadin.server.ErrorMessage;

/**
 * A util class to facilitate working with Vaadin's {@link ErrorMessage} API.
 */
public final class ErrorMessageUtil {
    private ErrorMessageUtil() {
    }

    public static List<ErrorMessage> getCauses(final ErrorMessage message) {
        List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        if (message instanceof CompositeErrorMessage) {
            Iterator<ErrorMessage> iter = ((CompositeErrorMessage) message).iterator();
            CollectionUtils.addAll(errors, iter);
        } else {
            errors.add(message);
        }
        return errors;
    }

    /**
     * @return the underlying error message as an HTML formatted string.
     * @see ErrorMessage#getFormattedHtmlMessage()
     */
    public static List<String> getCausesMessages(final ErrorMessage message) {
        return Lists.transform(getCauses(message), new Function<ErrorMessage, String>() {
            @Override
            public String apply(ErrorMessage error) {
                return error.getFormattedHtmlMessage();
            }
        });
    }
}
