/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.workbench;

import java.util.Iterator;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * The status bar view interface is implemented purely on the server-side - without custom widgets. In fact, it extends
 * the {@link HorizontalLayout} directly, rather than using a vaadin {@link com.vaadin.ui.CustomComponent CustomComponent}, because this
 * view impl is never exposed explicitly.<br />
 * Color change is ensured by toggling CSS preset styles.
 */
public class StatusBarViewImpl extends HorizontalLayout implements StatusBarView {

    private static final String STYLE_NAME = "statusbar";

    private String color;

    public StatusBarViewImpl() {
        setWidth(100, Unit.PERCENTAGE);
        setSpacing(true);
        setStyleName(STYLE_NAME);
    }

    /**
     * Adds component based on its alignment, first added first aligned. Concretely, we have to insert components
     * at the correct index in this {@link HorizontalLayout}.<br />
     * <br />
     * For left alignment: new insertion position is after last left-aligned component (LTR).<br />
     * For right alignment: new insertion position is before first right-aligned component (RTL).<br />
     * For center alignment: same as for right alignment, equals to position after last centered component (LTR).
     */
    @Override
    public void addComponent(Component c, Alignment align) {

        // compute index based on requested alignment, first come first aligned
        int index = 0;
        Iterator<Component> it = iterator();

        if (align.isLeft()) {
            while (it.hasNext() && getComponentAlignment(it.next()).isLeft()) {
                index++;
            }
        } else {
            while (it.hasNext() && !getComponentAlignment(it.next()).isRight()) {
                index++;
            }
        }

        addComponent(c, index);
        setComponentAlignment(c, align);
    }

    @Override
    public void setColor(String colorStyleName) {
        removeStyleName(this.color);
        addStyleName(colorStyleName);
        this.color = colorStyleName;
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }


}
