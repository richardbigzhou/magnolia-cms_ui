/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.autosuggest;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * SuggestionUtil.
 */
public class SuggestionUtil {

    private static Element encodingEl = DOM.createDiv();

    private static Element span = Document.get().createSpanElement().cast();
    static {
        span.getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        span.getStyle().setProperty("fontFamily", "Verdana, sans-serif");
        span.getStyle().setFontSize(13D, Unit.PX);
        span.getStyle().setTextAlign(TextAlign.LEFT);
        span.getStyle().setVisibility(Visibility.HIDDEN);
        Document.get().getBody().appendChild(span);
    }

    public static String encodingHTML(String html) {
        encodingEl.setInnerText(html);
        return encodingEl.getInnerHTML();
    }

    public static int getWidthInView(String html) {
        span.setInnerHTML(html);
        int w = span.getOffsetWidth();
        return w;
    }

    public static int getScrollBarWidth(Element scroll, boolean horizontal) {
        if (horizontal) {
            if (scroll.getScrollWidth() > scroll.getOffsetWidth()) {
                return 15;
            }
        } else {
            if (scroll.getScrollHeight() > scroll.getOffsetHeight()) {
                return 15;
            }
        }
        return 0;
    }

    public static int[] getOffset(Element el, Element relativeEl) {

        Element current = el.getParentElement().cast();
        int[] topAndLeft = new int[] { 0, 0 };

        while (current != relativeEl) {
            topAndLeft[0] -= current.getScrollTop();
            topAndLeft[1] -= current.getScrollLeft();
            current = current.getParentElement().cast();
        }

        while (el != relativeEl) {
            topAndLeft[0] += el.getOffsetTop();
            topAndLeft[1] += el.getOffsetLeft();
            el = (Element) el.getOffsetParent();
        }
        return topAndLeft;
    }

    public static String boldMatchStr(String text, String matchStr, boolean startsWith) {
        if (text == null || matchStr == null || text.length() == 0 || matchStr.length() == 0) {
            return null;
        }
        boolean isLowerCase = matchStr.equals(matchStr.toLowerCase());
        String lowerText = text.toLowerCase();
        if (startsWith) {
            if (isLowerCase ? lowerText.startsWith(matchStr) : text.startsWith(matchStr)) {
                int position = 0;
                int end = position + matchStr.length();
                JavaScriptObject jsArray = JavaScriptObject.createArray();
                push(jsArray, "<b>");
                push(jsArray, encodingHTML(text.substring(position, end)));
                push(jsArray, "</b>");
                push(jsArray, encodingHTML(text.substring(end)));
                return join(jsArray);
            }
        } else {
            if (matchStr.length() > 0) {
                int position = isLowerCase ? lowerText.indexOf(matchStr) : text.indexOf(matchStr);
                JavaScriptObject jsArray = JavaScriptObject.createArray();
                int begin = 0;
                while (position > -1) {
                    push(jsArray, encodingHTML(text.substring(begin, position)));
                    begin = position + matchStr.length();
                    push(jsArray, "<b>");
                    push(jsArray, encodingHTML(text.substring(position, begin)));
                    push(jsArray, "</b>");
                    position = isLowerCase ? lowerText.indexOf(matchStr, begin) : text.indexOf(matchStr, begin);
                }
                push(jsArray, encodingHTML(text.substring(begin)));
                return join(jsArray);
            }
        }
        return null;
    }

    private static native void push(JavaScriptObject jsArray, String value) /*-{
                                                                             jsArray[jsArray.length] = value;
                                                                             }-*/;

    private static native String join(JavaScriptObject jsArray) /*-{
                                                                return jsArray.join("");
                                                                }-*/;

    public static String getSamePrefix(List<String> items) {
        if (items == null || items.size() == 0) {
            return "";
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        if (items.size() == 2) {
            return getSamePrefix(items.get(0), items.get(1));
        }

        int half = items.size() / 2;
        String firstPrefix = getSamePrefix(items.subList(0, half));
        if (firstPrefix == null || firstPrefix.length() == 0) {
            return "";
        }
        return getSamePrefix(firstPrefix, getSamePrefix(items.subList(half, items.size())));
    }

    private static String getSamePrefix(String s1, String s2) {
        if (s1.equals(s2)) {
            return s1;
        }
        int minLength = s1.length() <= s2.length() ? s1.length() : s2.length();
        int i = 0;
        while (i < minLength) {
            if (Character.toLowerCase(s1.charAt(i)) != Character.toLowerCase(s2.charAt(i))) {
                break;
            }
            i++;
        }
        return s1.substring(0, i);
    }
}
