/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.jquerywrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * JQuery library wrapper. The functionality covered is ruled by the needs of MagnoliaShell (and its parts) implementation.
 */
public class JQueryWrapper extends JavaScriptObject {

    protected JQueryWrapper() {
    }

    public final static native JQueryWrapper select(final String style) /*-{
        return $wnd.jQuery(style);
    }-*/;

    public final static native JQueryWrapper selectId(final String id) /*-{
        return $wnd.jQuery('#' + id);
    }-*/;

    public final static native JQueryWrapper select(final Element el) /*-{
        return $wnd.jQuery(el);
    }-*/;

    public final static JQueryWrapper select(final Widget w) {
        return select(w.getElement());
    }

    public final native void on(String eventId, Callbacks callbacks) /*-{
        //var jq = this;
        var win = $wnd;
        win.alert('reg');
        this.on(eventId,
            function(event) {
                win.alert('Fire');
                if (callbacks != null) {
                    callbacks.fire(null);
                }
        });
    }-*/;

    public final native boolean is(String selector) /*-{
        return this.is(selector);
    }-*/;

    public final native JQueryWrapper children(String selector) /*-{
        return this.children(selector);
    }-*/;

    public final native JQueryWrapper find(String selector) /*-{
        return this.find(selector);
    }-*/;

    public final native void on(String eventId, String selector, Callbacks callbacks) /*-{
        this.on(
              eventId, selector,
            $entry(function (event) {
                      var jq = @info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper::select(Lcom/google/gwt/user/client/Element;)(event.target);
                      if (callbacks != null) {
                          callbacks.fire(jq);
                      }
            }));
    }-*/;

    public final native void animate(int duration, AnimationSettings settings) /*-{
          var json = settings.@info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings::asJSO()();
          var jq = this;
          this.animate(json, duration,
              $entry(function () {
                    if (settings != null) {
                        settings.@info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings::getCallbacks()()
                                   .fire(jq);
                    }
              }));
    }-*/;

    public final native void animate(int duration, double delay, AnimationSettings settings) /*-{
        var json = settings.@info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings::asJSO()();
        var jq = this;
        this.delay(delay).animate(json, duration,
            $entry(function () {
                    if (settings != null) {
                        settings.@info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings::getCallbacks()().fire(jq);
                    }
            }));
    }-*/;

    public final native JQueryWrapper stop() /*-{
          this.stop();
          return this;
    }-*/;

    public final native void fadeIn(int duration, Callbacks callbacks) /*-{
          var jq = this;
          this.fadeIn(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void fadeOut(int duration, Callbacks callbacks) /*-{
          var el = this.get();
          var jq = this;
          this.fadeOut(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void slideUp(int duration, Callbacks callbacks) /*-{
          var jq = this;
          this.slideUp(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void slideDown(int duration, Callbacks callbacks) /*-{
          var jq = this;
          this.slideDown(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void show(int duration, Callbacks callbacks) /*-{
          var jq = this;
          this.show(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void hide(int duration, Callbacks callbacks) /*-{
          var jq = this;
          this.hide(duration, function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void ready(final Callbacks callbacks) /*-{
          var jq = this;
          this.ready(function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native void unload(final Callbacks callbacks) /*-{
          var jq = this;
          this.unload(function() {
               if (callbacks != null) {
                    callbacks.fire(jq);
               }
          });
    }-*/;

    public final native String attr(final String property) /*-{
          return this.attr(property);
    }-*/;

    public final native void setAttr(final String property, final String value) /*-{
          this.attr(property, value);
    }-*/;

    public final native String css(final String property) /*-{
          return this.css(property);
    }-*/;

    public final native void setCss(final String property, final String value) /*-{
          this.css(property, value);
    }-*/;

    public final void setCss(JSONObject value) {
        final JavaScriptObject jso = value.isObject().getJavaScriptObject();
        setCss(jso);
    }

    public final native void setCss(JavaScriptObject value) /*-{
          this.css(value);
    }-*/;

    public final native void setCssPx(final String property, int pxVal) /*-{
          var pxVal = pxVal + 'px';
          this.css(property, pxVal);
    }-*/;

    public final native Integer cssInt(final String property) /*-{
          var result = this.css(property);
          return @info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper::parseInt(Ljava/lang/String;)(result);
    }-*/;

    public final native Position position() /*-{
        return this.position();
    }-*/;

    public native final Element get(int index) /*-{
          return this.get(index);
    }-*/;

    public native final JsArray<Element> get() /*-{
          return this.get();
    }-*/;

    public native final void setScrollTop(int scrollTop)  /*-{
        this.scrollTop(scrollTop);
    }-*/;

    public native final int getScrollTop()  /*-{
        return this.scrollTop();
    }-*/;

    public native final int size() /*-{
        return this.size();
    }-*/;

    public final boolean isAnimationInProgress() {
        return is(":animated");
    }

    public static native Integer parseInt(final String value) /*-{
          var number = parseInt(value, 10);
          if (isNaN(number))
               return null;
          else
               return @java.lang.Integer::valueOf(I)(number);
    }-*/;

    public final native void removeClass(String className) /*-{
        this.removeClass(className);
    }-*/;
}
