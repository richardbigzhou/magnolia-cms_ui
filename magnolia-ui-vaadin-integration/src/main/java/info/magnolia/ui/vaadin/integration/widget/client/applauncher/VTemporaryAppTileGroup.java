/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The temporary app group.
 */
public class VTemporaryAppTileGroup extends VAppTileGroup {

    private boolean isOpen = false;

    public VTemporaryAppTileGroup(EventBus eventBus, String color) {
        super(color);
        construct();
    }

    @Override
    protected void construct() {
        addStyleName("temporary");

    }

    @Override
    public void addAppThumbnail(VAppTile thumbnail) {
        super.addAppThumbnail(thumbnail);
        thumbnail.getElement().getStyle().setOpacity(0d);
    }

    public void openSection() {

        isOpen = true;
        if (getWidgetCount() != 0) {
            final Iterator<Widget> it = iterator();
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    final Widget w = it.next();
                    w.getElement().getStyle().setProperty("opacity", "1");
                    w.removeStyleName("anim-turn-in-off");
                    w.addStyleName("anim-turn-in");
                    return it.hasNext();
                }
            }, 50);
            cleanUpTransition();
        }
    }

    private void cleanUpTransition() {
        cleanUpTimer.cancel();
        cleanUpTimer.schedule(50 * getWidgetCount());
    }

    public void close() {
        isOpen = false;
        if (getWidgetCount() != 0) {
            final Iterator<Widget> it = new ReverseIterator();
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    final Widget w = it.next();
                    w.getElement().getStyle().setProperty("opacity", "0");
                    w.removeStyleName("anim-turn-in");
                    w.addStyleName("anim-turn-in-off");
                    return it.hasNext();
                }
            }, 50);
            cleanUpTransition();
        }
    }

    public void conceal() {
        getElement().getStyle().setDisplay(Display.NONE);
        close();
    }

    public void reveal() {
        getElement().getStyle().setDisplay(Display.BLOCK);
        openSection();
    }

    private class ReverseIterator implements Iterator<Widget> {

        private int index = getWidgetCount();

        @Override
        public boolean hasNext() {
            return index > 0;
        }

        @Override
        public Widget next() {
            if (index == 0) {
                throw new NoSuchElementException();
            }
            return getWidget(--index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Timer cleanUpTimer = new Timer() {
        @Override
        public void run() {
            final Iterator<Widget> it = iterator();
            while (it.hasNext()) {
                final Widget w = it.next();
                if (isOpen && !w.getStyleName().contains("anim-turn-in")) {
                    w.getElement().getStyle().setProperty("opacity", "1");
                    w.removeStyleName("anim-turn-in-off");
                    w.addStyleName("anim-turn-in");
                } else if (!isOpen && !w.getStyleName().contains("anim-turn-in-off")) {
                    w.getElement().getStyle().setProperty("opacity", "0");
                    w.removeStyleName("anim-turn-in");
                    w.addStyleName("anim-turn-in-off");
                }
            }
        }
    };

}
