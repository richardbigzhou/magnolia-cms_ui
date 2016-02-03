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
package info.magnolia.ui.api.message;

import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Models a message. Except for timestamp all fields are optional.
 */
public class Message implements Cloneable {

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String ID = "id";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String TIMESTAMP = "timestamp";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String MESSAGETYPE = "messagetype";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String MESSAGE_VIEW = "messageView";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String SUBJECT = "subject";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String MESSAGE = "message";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String CLEARED = "cleared";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement. Message will get explicit attribute for it.
     */
    public static String SENDER = "sender";
    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
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

    public String getView() {
        return data.get(MESSAGE_VIEW) != null ? data.get(MESSAGE_VIEW).toString() : null;
    }

    public void setView(String view) {
        data.put(MESSAGE_VIEW, view);
    }

    @Override
    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }

    // Map methods

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public int size() {
        return data.size();
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1. Use {@link #hasProperty(String)} instead.
     */
    public boolean containsKey(Object key) {
        return hasProperty((String) key);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1. Use {@link #getProperty(String)} instead.
     */
    public Object get(Object key) {
        return data.get(key);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1. Use {@link #addProperty(String, Object)} instead.
     */
    public Object put(String key, Object value) {
        if (TIMESTAMP.equals(key)) {
            throw new IllegalArgumentException("Cannot replace timestamp of the message.");
        }
        return data.put(key, value);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public Object remove(Object key) {
        if (TIMESTAMP.equals(key)) {
            throw new IllegalArgumentException("Cannot remove timestamp from the message.");
        }
        return data.remove(key);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m.containsKey(TIMESTAMP)) {
            // timestamp cannot be replaced, even by "batch" operation
            m.remove(TIMESTAMP);
        }
        data.putAll(m);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public void clear() {
        // preserve timestamp
        long timestamp = getTimestamp();
        data.clear();
        data.put(TIMESTAMP, timestamp);
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public Set<String> keySet() {
        return data.keySet();
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public Collection<Object> values() {
        return data.values();
    }

    /**
     * @deprecated since 5.0.2 - will be removed in 5.1 without replacement.
     */
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return data.entrySet();
    }

    public void addProperty(final String name, final Object value) {
        data.put(name, value);
    }

    public Object getProperty(final String name) {
        return data.get(name);
    }

    public boolean hasProperty(final String name) {
        return data.containsKey(name);
    }

    public Set<String> getPropertNames() {
        return data.keySet();
    }
}
