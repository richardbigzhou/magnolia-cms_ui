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
package info.magnolia.ui.admincentral.shellapp.pulse.dashboard;

import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;

/**
 * Implementation of {@link PulseDashboardView}.
 */
public class PulseDashboardViewImpl extends CustomComponent implements PulseDashboardView {

    private final SplitFeed splitPanel = new SplitFeed();

    public PulseDashboardViewImpl() {
        super();
        setSizeFull();
        setCompositionRoot(splitPanel);
        splitPanel.addStyleName("pulse-dashboard");
        construct();
    }

    private void construct() {

        // feed entries
        FeedSection activityStream = new FeedSection();

        Label lt = new Label("Today");
        lt.addStyleName("category-separator");
        activityStream.addComponent(lt);

        List<DashboardEntry> entries = getFeedEntries();
        for (int i = 0; i < entries.size(); i++) {
            if (i == 3) {
                final Label ly = new Label("Yesterday");
                ly.addStyleName("category-separator");
                activityStream.addComponent(ly);
            }
            activityStream.addComponent(entries.get(i));
        }

        // recent pages
        Label recentPage = new Label(getRecentPages(), ContentMode.HTML);

        splitPanel.getLeftContainer().setTitle("Activity Stream");
        splitPanel.getLeftContainer().setTitleLinkEnabled(true);
        splitPanel.getLeftContainer().addComponent(activityStream);

        splitPanel.getRightContainer().setTitle("Pages I Changed Recently");
        splitPanel.getRightContainer().setTitleLinkEnabled(true);
        splitPanel.getRightContainer().addComponent(recentPage);

    }

    private List<DashboardEntry> getFeedEntries() {
        List<DashboardEntry> entries = new ArrayList<DashboardEntry>();

        Calendar cal = Calendar.getInstance();
        entries.add(new DashboardEntry(Icon.ACCEPTED,
                "Peter Twist has accepted and published your page ",
                Page.ABOUT,
                cal.getTime()));

        cal.add(Calendar.MINUTE, -12);
        entries.add(new DashboardEntry(Icon.REJECTED,
                "Peter Twist has rejected your changes to page ",
                Page.FITNESS,
                cal.getTime(),
                "It needs to be a bit more to the point..."));

        cal.add(Calendar.MINUTE, -90);
        entries.add(new DashboardEntry(Icon.WARNING,
                "An urgent new task has been assigned to you, due today:",
                Page.FITNESS,
                cal.getTime(),
                "Rework Fitness counts ASAP"));

        cal.add(Calendar.HOUR, -21);
        entries.add(new DashboardEntry(Icon.ACCEPTED,
                "Peter Twist has accepted and published your page ",
                Page.ABOUT,
                cal.getTime()));

        cal.add(Calendar.MINUTE, -111);
        entries.add(new DashboardEntry(Icon.REJECTED,
                "Peter Twist has rejected your changes to page ",
                Page.MORE,
                cal.getTime(),
                "Check the typos :)"));

        return entries;
    }

    private String getRecentPages() {
        List<RecentPageEntry> entries = new ArrayList<RecentPageEntry>();
        entries.add(new RecentPageEntry(Page.ABOUT, Page.COMPANY, Icon.ACCEPTED));
        entries.add(new RecentPageEntry(Page.FITNESS, Page.HINTS, Icon.PENDING));
        entries.add(new RecentPageEntry(Page.MORE, Page.HINTS, Icon.REJECTED));

        StringBuilder s = new StringBuilder();
        s.append("<table class=\"recent-pages\"><thead><tr>" +
                "<th>Page Title</th>" +
                "<th>Parent Page</th>" +
                "<th>Status</th>" +
                "</tr></thead><tbody>");

        for (RecentPageEntry entry : entries) {

            String statusClass = entry.getStatus().getName();
            if (entry.getStatus().getColor() != null) {
                statusClass += " " + entry.getStatus().getColor();
            }

            s.append("<tr>" +
                    "<td>" + entry.getPage().getTitle() + "</td>" +
                    "<td>" + entry.getParentPage().getTitle() + "</td>" +
                    "<td><span class=\"status-icon " + statusClass + "\"></span></td>" +
                    "</tr>");
        }

        s.append("</tbody></table>");
        return s.toString();
    }

    @Override
    public HasComponents asVaadinComponent() {
        return this;
    }

    /**
     * The Class DashboardSection.
     */
    public static class DashboardSection extends CssLayout {

        public DashboardSection() {
            addStyleName("pulse-dashboard-section");
            setWidth("100%");
        }
    }

    /**
     * The Class DashboardEntry.
     */
    public static class DashboardEntry extends CssLayout {

        private final Label iconElement = new Label();

        private final CssLayout wrapper = new CssLayout();

        private final Label textElement = new Label();

        private final Label commentDivet = new Label("<span class=\"comment-divet icon-arrow2_n\"></span>", ContentMode.HTML);

        private final Label commentElement = new Label();

        private final Label dateElement = new Label();

        private final Date date;

        public DashboardEntry(final Icon icon, final String text, final Page page, final Date date) {
            addStyleName("v-dashboard-entry");
            setWidth("100%");
            // setMargin(true, false, true, false);
            // setSpacing(true);
            iconElement.setContentMode(ContentMode.HTML);
            iconElement.setWidth(null);
            iconElement.setStyleName("icon");
            textElement.setContentMode(ContentMode.HTML);
            textElement.setStyleName("text");
            commentElement.setStyleName("comment");
            commentElement.setContentMode(ContentMode.HTML);
            dateElement.setStyleName("date");
            wrapper.addStyleName("content");
            wrapper.setWidth("100%");
            wrapper.addComponent(textElement);
            wrapper.addComponent(dateElement);
            addComponent(iconElement);
            addComponent(wrapper);
            // setExpandRatio(wrapper, 1.0f);

            this.date = date;
            setIcon(icon);
            setText(text, page);
            setDate(date);
        }

        public DashboardEntry(final Icon icon, final String text, final Page page, final Date date, final String comment) {
            this(icon, text, page, date);
            setComment(comment);
            wrapper.addComponent(commentDivet, 1);
            wrapper.addComponent(commentElement, 2);
        }

        public void setText(String text, Page page) {
            textElement.setValue(text + "<br /><strong>" + page.getTitle() + "</strong>");
        }

        public void setIcon(Icon icon) {
            String className = icon.getName();
            if (icon.getColor() != null) {
                className += " " + icon.getColor();
            }
            iconElement.setValue("<span class=\"" + className + "\"></span>");
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            dateElement.setValue(formatter.format(date));
        }

        public void setComment(String comment) {
            commentElement.setValue(comment);
        }
    }

    /**
     * The Class RecentPageEntry.
     */
    public static class RecentPageEntry {

        private final Page page;

        private final Page parentPage;

        private final Icon status;

        public RecentPageEntry(Page page, Page parentPage, Icon status) {
            this.page = page;
            this.parentPage = parentPage;
            this.status = status;
        }

        public Page getPage() {
            return page;
        }

        public Page getParentPage() {
            return parentPage;
        }

        public Icon getStatus() {
            return status;
        }
    }

    /**
     * Icon supporting class.
     */
    public static enum Icon {
        ACCEPTED("icon-shape-circle", "color-green"),
        REJECTED("icon-shape-circle", "color-red"),
        PENDING("icon-shape-circle", "color-yellow"),
        WARNING("icon-shape-triangle", "color-yellow");

        private String name;

        private String color;

        Icon(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * Page enum supporting class.
     */
    public static enum Page {
        ABOUT("About us"),
        COMPANY("The Company"),
        FITNESS("Fitness counts"),
        HINTS("Hints & Tips"),
        MORE("More than enough");

        private String title;

        Page(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

}
