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
package info.magnolia.ui.vaadin.gwt.client.editor.jsni;

import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Frame;

/**
 * ReadyStateWatch. Unused, maybe usable for IE.
 */
public class ReadyStateWatch {
    /**
     * Enum.
     */
    public static enum ReadyState {
        COMPLETE {
            @Override
            public String toString() {
                return "complete";
            }
        },
        LOADING {
            @Override
            public String toString() {
                return "loading";
            }
        },
        INTERACTIVE {
            @Override
            public String toString() {
                return "interactive";
            }
        },
        UNINITIALIZED {
            @Override
            public String toString() {
                return "uninitialized";
            }
        }
    }

    Frame iframe;

    public ReadyStateWatch(Frame iframe) {
        this.iframe = iframe;
        addNativeReadyStateWatch(this, iframe.getElement().<IFrameElement>cast());
    }

    public HandlerRegistration addReadyStateChangeHandler(ValueChangeHandler<ReadyState> handler) {
        return iframe.addHandler(handler, ValueChangeEvent.getType());
    }

    public ReadyState getReadyState() {
        try {
            return ReadyState.valueOf(iframe.getElement().getPropertyString("readyState"));
        } catch (Exception e) {
            return null;
        }
    }

    // used in native function
    private void fireReadyStateChange() {
        iframe.fireEvent(new ValueChangeEvent<ReadyState>(getReadyState()) {
        });
    }

    private static native void addNativeReadyStateWatch(ReadyStateWatch self, IFrameElement e) /*-{

        var element = e;
        var handleStateChange = function(){
            self.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.ReadyStateWatch::fireReadyStateChange()();
        };
        if (element.addEventListener) {
            element.addEventListener("onreadystatechange", handleStateChange, false);
        }else if (element.attachEvent) {
            element.attachEvent("onreadystatechange",handleStateChange);
        }else{
            element.onreadystatechange=handleStateChange;
        }
    }-*/;
}
