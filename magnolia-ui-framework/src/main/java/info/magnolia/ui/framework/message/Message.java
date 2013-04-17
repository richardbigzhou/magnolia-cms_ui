/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.framework.message;

import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Models a message. Except for timestamp all fields are optional. The sender is {@link #DEFAULT_SENDER} unless diversely set after object construction.
 */
public class Message implements Cloneable, Map<String, Object> {

    public static String ID = "id";
    public static String TIMESTAMP = "timestamp";
    public static String MESSAGETYPE = "messagetype";
    public static String MESSAGE_VIEW = "messageView";

    public static String SUBJECT = "subject";
    public static String MESSAGE = "message";
    public static String CLEARED = "cleared";
    public static String SENDER = "sender";
    public static String DEFAULT_SENDER = "system";

    private Map<String, Object> data = new HashMap<String, Object>();

    public Message() {
        this(System.currentTimeMillis());
        setSender(MgnlContext.getInstance().getUser().getName());
    }

    public Message(long timestampInMillis) {
        setTimestamp(timestampInMillis);
        setCleared(false);
    }

    public Message(final MessageType type, final String subject, final String message) {
        this();
        setSubject(subject);
        setMessage(message);
        setType(type);
    }

    public long getTimestamp() {
        return ((Long) data.get(TIMESTAMP)).longValue();
    }

    private void setTimestamp(long timestamp) {
        data.put(TIMESTAMP, timestamp);
    }

    public String getMessage() {
        return data.get(MESSAGE) != null ? data.get(MESSAGE).toString() : null;
    }

    public void setMessage(String message) {
        data.put(MESSAGE, message);
    }

    public String getSubject() {
        return data.get(SUBJECT) != null ? data.get(SUBJECT).toString() : null;
    }

    public void setSubject(String subject) {
        data.put(SUBJECT, subject);
    }

    public MessageType getType() {
        return data.get(MESSAGETYPE) != null ? MessageType.valueOf(data.get(MESSAGETYPE).toString()) : null;
    }

    public void setType(MessageType type) {
        data.put(MESSAGETYPE, type.name());
    }

    public void setId(String id) {
        data.put(ID, id);
    }

    public String getId() {
        return data.get(ID) != null ? data.get(ID).toString() : null;
    }

    public boolean isCleared() {
        return data.get(CLEARED) != null && ((Boolean) data.get(CLEARED));
    }

    public void setCleared(boolean cleared) {
        data.put(CLEARED, cleared);
    }

    public String getSender() {
        return data.get(SENDER) != null ? data.get(SENDER).toString() : null;
    }

    public void setSender(String sender) {
        data.put(SENDER, sender);
    }

    @Override
    protected Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }

    // Map methods

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        if (TIMESTAMP.equals(key)) {
            throw new IllegalArgumentException("Cannot replace timestamp of the message.");
        }
        return data.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        if (TIMESTAMP.equals(key)) {
            throw new IllegalArgumentException("Cannot remove timestamp from the message.");
        }
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m.containsKey(TIMESTAMP)) {
            // timestamp cannot be replaced, even by "batch" operation
            m.remove(TIMESTAMP);
        }
        data.putAll(m);
    }

    @Override
    public void clear() {
        // preserve timestamp
        long timestamp = getTimestamp();
        data.clear();
        data.put(TIMESTAMP, timestamp);
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<Object> values() {
        return data.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return data.entrySet();
    }
}
