/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.api.message;

import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Models a message. Except for timestamp all fields are optional.
 */
public class Message implements Cloneable {

    public static final String MESSAGE_VIEW = "messageView";

    private String id;
    private final long timestamp;
    private MessageType type;
    private String subject;
    private String message;
    private String sender;
    private boolean cleared;
    /**
     * View associated with this message.
     */
    private String view;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public Message() {
        this(System.currentTimeMillis());
        setSender(MgnlContext.getInstance().getUser().getName());
    }

    public Message(long timestampInMillis) {
        this.timestamp = timestampInMillis;
        setCleared(false);
    }

    public Message(final MessageType type, final String subject, final String message) {
        this();
        setSubject(subject);
        setMessage(message);
        setType(type);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }

    public void addProperty(final String name, final Object value) {
        properties.put(name, value);
    }

    public Object getProperty(final String name) {
        return properties.get(name);
    }

    public boolean hasProperty(final String name) {
        return properties.containsKey(name);
    }

    public Set<String> getPropertNames() {
        return properties.keySet();
    }
}
