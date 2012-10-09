/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import com.google.gwt.user.client.ui.Widget;



/**
 * Shell apps viewport client side.
 */
public class VShellAppsViewport extends VShellViewport {

    private final Map<String, Widget> shellAppWidgets = new HashMap<String, Widget>();

    public VShellAppsViewport() {
        super();
        setForceContentAlign(true);

        setViewportShowAnimationDelegate(AnimationDelegate.SLIDING_DELEGATE);
        setViewportHideAnimationDelegate(AnimationDelegate.FADING_DELEGATE);
        setContentShowAnimationDelegate(AnimationDelegate.FADING_DELEGATE);
        setContentHideAnimationDelegate(AnimationDelegate.FADING_DELEGATE);
        setCurtainAnimated(true);
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            // reset to fade out transition if closing shell app turned it to slide out
            setViewportHideAnimationDelegate(AnimationDelegate.FADING_DELEGATE);
        }
        super.setActive(active);
    }

    @Override
    /**
     * Get a map of widgets so that we can launch them from client side on demand.
     */
    public void setVisibleWidget(Widget w) {
        super.setVisibleWidget(w);

        String id = w.getElement().getId();
        //Add it to a map of widgets if its not already there.
        if (shellAppWidgets.get(id) == null){
            shellAppWidgets.put(id, w);
        }
    }

    /**
     * @param shellAppType
     * returns true if it was able to find the widget
     */
    public boolean setVisibleWidgetByShellAppType(ShellAppType shellAppType) {
        // Get Widget w, based on shellAppType
        Widget w = getWidgetFromShellAppType(shellAppType);
        if (w != null && w!=getVisibleWidget()){
            super.setVisibleWidget(w);
            return true;
        }
        return false;

    }

    private Widget getWidgetFromShellAppType(ShellAppType shellAppType){
        Widget w = null;
        w = shellAppWidgets.get(shellAppType.getClassId());
        return w;
    }


}
