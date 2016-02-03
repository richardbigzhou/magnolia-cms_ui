/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for horizontal bars with buttons.
 */
public abstract class AbstractBar extends FlowPanel {

    private final static String EDITOR_BAR_CLASS_NAME = "mgnlEditorBar";
    private final static String EDITOR_BAR_LABEL_CLASS_NAME = "mgnlEditorBarLabel";
    private final static String EDITOR_BAR_BUTTONS_CLASS_NAME = "mgnlEditorBarButtons";
    private final static String FOCUS_CLASS_NAME = "focus";
    private final static String CHILD_FOCUS_CLASS_NAME = "childFocus";
    private final static String MGNL_LEVEL_CLASS_NAME = "mgnlLevel-";

    protected final static String EDITOR_CLASS_NAME = "mgnlEditor";
    protected final static String AREA_CLASS_NAME = "area";
    protected final static String COMPONENT_CLASS_NAME = "component";

    protected final static String ICON_CLASS_NAME = "editorIcon";
    protected final static String EDIT_CLASS_NAME = "icon-edit";
    protected final static String ADD_CLASS_NAME = "icon-add-item";
    
    private final static int MAX_LEVEL = 6;
    private final int level;

    private FlowPanel buttonWrapper;

    public AbstractBar(MgnlElement mgnlElement) {

        setStyleName(EDITOR_BAR_CLASS_NAME);
        addStyleName(EDITOR_CLASS_NAME);
        this.level = mgnlElement.getLevel();

        setVisible(false);
    }

    protected void initLayout() {
        buttonWrapper = new FlowPanel();
        buttonWrapper.setStylePrimaryName(EDITOR_BAR_BUTTONS_CLASS_NAME);
        add(buttonWrapper);

        String label = getLabel();
        if (label != null && !label.isEmpty()) {
            Label areaName = new Label(label);
            // tooltip. Nice to have when area label is truncated because too long.
            areaName.setTitle(label);
            areaName.setStylePrimaryName(EDITOR_BAR_LABEL_CLASS_NAME);
            String mgnlLevel = String.valueOf(level);
            if (level > MAX_LEVEL) {
                mgnlLevel = "max";
            }
            areaName.addStyleName(MGNL_LEVEL_CLASS_NAME + mgnlLevel);
            // setStylePrimaryName(..) replaces gwt default css class, in this case gwt-Label
            add(areaName);
        }

        createControls();
    }

    protected abstract String getLabel();

    protected abstract void createControls();

    @Override
    public void onAttach() {
        super.onAttach();
    }

    protected void setId(String id) {
        getElement().setId(id);
    }

    protected void addButton(final Widget button) {
        buttonWrapper.add(button);
    }

    /**
     * Shorthand for <code>getElement().getStyle()</code>.
     *
     * @return the element's underlying {@link Style}. You can use this object to manipulate the css
     *         style attribute of this bar widget.
     */
    protected Style getStyle() {
        return getElement().getStyle();
    }

    public void removeFocus() {
        removeStyleName(FOCUS_CLASS_NAME);
        removeStyleName(CHILD_FOCUS_CLASS_NAME);
    }

    public void setFocus(boolean child) {
        String CLASS_NAME = (child) ? CHILD_FOCUS_CLASS_NAME : FOCUS_CLASS_NAME;
        addStyleName(CLASS_NAME);
    }
}
