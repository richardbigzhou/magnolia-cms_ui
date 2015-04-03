/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.layout;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Utility class for fetching CSS properties from DOM StyleSheets JS object and
 * for creating new CSS rules dynamically.
 */
public class CssRule {

    private final String selector;
    private final JavaScriptObject rules = null;

    private CssRule(final String selector) {
        this.selector = selector;
    }

    /**
     * @param selector the CSS selector to search for in the stylesheets
     * @param deep should the search follow any @import statements?
     */
    public CssRule(final String selector, final boolean deep) {
        this.selector = selector;
        fetchRule(selector, deep);
    }

    private native void fetchRule(final String selector, final boolean deep)
    /*-{
        var sheets = $doc.styleSheets;
        for(var i = 0; i < sheets.length; i++) {
            var sheet = sheets[i];
            if(sheet.href && sheet.href.indexOf("VAADIN/themes")>-1) {
                this.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules = @info.magnolia.ui.vaadin.gwt.client.layout.CssRule::searchForRule(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Z)(sheet, selector, deep);
                return;
            }
        }
        this.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules = [];
    }-*/;

    /*
     * Loops through all current style rules and collects all matching to
     * 'rules' array. The array is reverse ordered (last one found is first).
     */
    private static native JavaScriptObject searchForRule(
            final JavaScriptObject sheet, final String selector,
            final boolean deep)
    /*-{
        if(!$doc.styleSheets)
            return null;

        selector = selector.toLowerCase();

        var allMatches = [];

        // IE handles imported sheet differently
        if(deep && sheet.imports.length > 0) {
            for(var i=0; i < sheet.imports.length; i++) {
                var imports = @info.magnolia.ui.vaadin.gwt.client.layout.CssRule::searchForRule(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Z)(sheet.imports[i], selector, deep);
                allMatches.concat(imports);
            }
        }

        var theRules = new Array();
        if (sheet.cssRules)
            theRules = sheet.cssRules
        else if (sheet.rules)
            theRules = sheet.rules

        var j = theRules.length;
        for(var i=0; i<j; i++) {
            var r = theRules[i];
            if(r.type == 1 || sheet.imports) {
                var selectors = r.selectorText.toLowerCase().split(",");
                var n = selectors.length;
                for(var m=0; m<n; m++) {
                    if(selectors[m].replace(/^\s+|\s+$/g, "") == selector) {
                        allMatches.unshift(r);
                        break; // No need to loop other selectors for this rule
                    }
                }
            } else if(deep && r.type == 3) {
                // Search @import stylesheet
                var imports = @info.magnolia.ui.vaadin.gwt.client.layout.CssRule::searchForRule(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Z)(r.styleSheet, selector, deep);
                allMatches.concat(imports);
            }
        }

        return allMatches;
    }-*/;

    /**
     * Returns a specific property value from this CSS rule.
     *
     * @param propertyName camelCase CSS property name
     * @return the value of the property as a String
     */
    public native String getProperty(final String propertyName)
    /*-{
        var j = this.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules.length;
        for(var i=0; i<j; i++){
            var value = this.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules[i].style[propertyName];
        if(value)
            return value;
        }
        return null;
    }-*/;

    /**
     * Sets a specific property value for this CSS rule.
     *
     * @param propertyName camelCase CSS property name
     * @param propertyValue the value of the property as a String
     */
    public native void setProperty(final String propertyName,
                                   final String propertyValue)
    /*-{
        this.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules[0].style[propertyName] = propertyValue;
    }-*/;

    public String getSelector() {
        return selector;
    }

    public static CssRule create(String selector) {
        CssRule newRule = new CssRule(selector);
        createRule(selector, newRule);
        return newRule;
    }

    private static native void createRule(final String selector, CssRule rule)
    /*-{
    var sheets = $doc.styleSheets;
    for(var i = 0; i < sheets.length; i++) {
        var sheet = sheets[i];
        if(sheet.href && sheet.href.indexOf("VAADIN/themes")>-1) {
            if(sheet.insertRule) {
                sheet.insertRule(selector + "{}", sheet.cssRules.length);
                var r = sheet.cssRules[sheet.cssRules.length-1];
            } else { // IE
                sheet.addRule(selector, "foo:bar");
                var r = sheet.rules[sheet.rules.length-1];
            }
            rule.@info.magnolia.ui.vaadin.gwt.client.layout.CssRule::rules = [r];
        }
    }
    }-*/;

}
