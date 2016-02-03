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
package info.magnolia.ui.form.field.component;

import com.vaadin.ui.Component;

/**
 * Main content preview component definition.<br>
 * @param <T>.
 */
public interface ContentPreviewComponent<T extends Object> extends Component {

    /**
     * Define actions to do on Value Change.(refresh the ContentPreview)<br>
     * In general, the implementing classes create a component displaying<br>
     * - a Content detail (File name, size...)<br>
     * - a Content preview (thumbnail, icon,...)<br>
     * Embedded into a main component. <br>
     * On value change, the related item T has to be refreshed. <br>
     * If the item is empty or null, the main component should not be visible.
     */
    public void onValueChange(String itemReference);

}
