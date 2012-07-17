/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.editor.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * SortComponentEvent.
 */
public class SortComponentEvent extends GwtEvent<SortComponentEventHandler> {

    public static Type<SortComponentEventHandler> TYPE = new Type<SortComponentEventHandler>();

    private String workSpace;
    private String parentPath;
    private String sourcePath;
    private String targetPath;
    private String order;

    public SortComponentEvent(String workSpace,String parentPath, String sourcePath, String targetPath, String order) {
        this.workSpace = workSpace;
        this.parentPath= parentPath;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.order = order;
    }

    @Override
    public Type<SortComponentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SortComponentEventHandler handler) {
        handler.onSortComponent(this);
    }

    public String getWorkSpace() {
        return workSpace;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getOrder() {
        return order;
    }

    public String getParentPath() {
        return parentPath;
    }
}
