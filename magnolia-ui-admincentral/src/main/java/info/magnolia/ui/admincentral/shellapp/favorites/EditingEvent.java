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
package info.magnolia.ui.admincentral.shellapp.favorites;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

/**
 * An event sent when editing a favorite entry.
 */
@SuppressWarnings("serial")
public final class EditingEvent extends Event implements Serializable {

    private boolean editing;

    public EditingEvent(Component source, boolean editing) {
        super(source);
        this.editing = editing;
    }

    public boolean isEditing() {
        return editing;
    }

    public static final Method EDITING_METHOD;

    static {
        try {
            EDITING_METHOD = EditingListener.class.getDeclaredMethod(
                    "onEdit", new Class[] { EditingEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException();
        }
    }

    /**
     * EditableListener.
     */
    public interface EditingListener extends Serializable {
        void onEdit(EditingEvent event);
    }

    /**
     * EditingListener.
     */
    public interface EditingNotifier extends Serializable {
        /**
         * Register a listener to handle {@link EditingEvent}s.
         */
        void addEditingListener(EditingListener listener);

        /**
         * Removes an EditingListener.
         */
        void removeEditingListener(EditingListener listener);
    }

}
