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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.vaadin.client.ApplicationConnection;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

/**
 * GWT Animation wrapper for JQuery Animations.
 */
public class JQueryAnimation extends Animation {

    private Object lock = new Object();

    private AnimationSettings settings = new AnimationSettings();

    private JQueryWrapper jQueryWrapper;

    private Element currentElement;

    private ApplicationConnection connection;

    private boolean isBlocking = false;

    private boolean isClearTopAfterThisAnimation = false;

    public void setProperty(String property, Object value) {
        settings.setProperty(property, value);
    }

    public void addCallback(JQueryCallback callback) {
        settings.addCallback(callback);
    }

    public void clearTopAfterThisAnimation() {
        isClearTopAfterThisAnimation = true;
    }

    public JQueryAnimation(ApplicationConnection connection) {
        this.connection = connection;
        if (connection != null) {
            this.isBlocking = true;
        }
        addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                query.setCss("transition", "");
                query.setCss("transform", "");

                getJQueryWrapper().setCss("-webkit-transform", "");
                getJQueryWrapper().setCss("-webkit-transition", "");

                if (isClearTopAfterThisAnimation) {
                    currentElement.getStyle().clearTop();
                }
                isClearTopAfterThisAnimation = false;
                onComplete();
            }
        });
    }

    public JQueryAnimation() {
        this(null);
    }

    @Override
    public void run(int duration, double startTime, Element element) {
        this.currentElement = element;
        this.jQueryWrapper = null;
        cancel();
        onStart();
        getJQueryWrapper().animate(duration, settings);
    }

    protected JQueryWrapper getJQueryWrapper() {
        if (jQueryWrapper == null) {
            jQueryWrapper = JQueryWrapper.select((com.google.gwt.user.client.Element)currentElement);
        }
        return jQueryWrapper;
    }

    @Override
    protected void onUpdate(double progress) {}

    @Override
    public void cancel() {
        if (isRunning()) {
            getJQueryWrapper().stop();
            getJQueryWrapper().setCss("transition", "");
            getJQueryWrapper().setCss("transform", "");
            getJQueryWrapper().setCss("top", "");
            getJQueryWrapper().setCss("-webkit-transform", "");
            getJQueryWrapper().setCss("-webkit-transition", "");
            getJQueryWrapper().setCss("-moz-transform", "");
            getJQueryWrapper().setCss("-moz-transition", "");
            getJQueryWrapper().setCss("-o-transform", "");
            getJQueryWrapper().setCss("-o-transition", "");
        }
        onComplete();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isBlocking && connection != null) {
            connection.suspendReponseHandling(lock);
        }
    }

    @Override
    protected void onComplete() {
        if (isBlocking && connection != null) {
            connection.resumeResponseHandling(lock);
        }
    }

    @Override
    public boolean isRunning() {
        return getJQueryWrapper().isAnimationInProgress();
    }

    public Element getCurrentElement() {
        return currentElement;
    }
}
