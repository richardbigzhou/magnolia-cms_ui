/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget;

import info.magnolia.ui.vaadin.gwt.client.layout.CssRule;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.LazyThumbnailLayoutConnector.ThumbnailService;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchRecognizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.googlecode.mgwt.dom.client.recognizer.pinch.UIObjectToOffsetProvider;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapRecognizer;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.VConsole;

/**
 * Client side impl of lazy asset thumbnails layout.
 */
public class LazyThumbnailLayoutWidget extends FlowPanel {

    private static final int QUERY_TIMER_DELAY = 250;

    private static final int MIN_WIDTH = 50;

    private static final int MAX_WIDTH = 230;

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    private int thumbnailAmount = 0;

    private ThumbnailWidget selectedThumbnail = null;

    private final List<ThumbnailWidget> thumbnails = new ArrayList<ThumbnailWidget>();

    private final List<ThumbnailWidget> thumbnailStubs = new ArrayList<ThumbnailWidget>();

    private final ScrollPanel scroller = new ScrollPanel();

    private final Slider thumbnailSizeSlider = new Slider();

    private final CssRule thumbnailImageStyle = CssRule.create(".thumbnail-image");

    private final CssRule thumbnailStyle = CssRule.create(".thumbnail");

    private final FlowPanel imageContainer = new FlowPanel();

    private ThumbnailService thumbnailService;

    public LazyThumbnailLayoutWidget() {

        addStyleName("thumbnail-layout");

        thumbnailStyle.setProperty("margin", "3px");

        scroller.setWidget(imageContainer);
        scroller.addStyleName("thumbnail-scroller");
        scroller.getElement().getStyle().setPosition(Position.ABSOLUTE);

        add(thumbnailSizeSlider);
        add(scroller);

        bindHandlers();
        reset();
    }

    protected void bindHandlers() {

        scroller.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                createStubsAndQueryThumbnails();
            }
        });

        final TouchDelegate touchDelegate = new TouchDelegate(this);
        touchDelegate.addTouchHandler(new MagnoliaPinchRecognizer(touchDelegate, new UIObjectToOffsetProvider(scroller)));
        MultiTapRecognizer multitapRecognizer = new MultiTapRecognizer(touchDelegate, 1, 2);
        touchDelegate.addTouchHandler(multitapRecognizer);

        addHandler(new MagnoliaPinchMoveEvent.Handler() {
            @Override
            public void onPinchMove(MagnoliaPinchMoveEvent event) {
                double scaleFactor = 1 / event.getScaleFactor();
                int width = Math.max((int) (ComputedStyle.parseInt(thumbnailStyle.getProperty("width")) * scaleFactor), 25);
                int height = Math.max((int) (ComputedStyle.parseInt(thumbnailStyle.getProperty("height")) * scaleFactor), 25);
                scroller.setVerticalScrollPosition((int) (scroller.getVerticalScrollPosition() * scaleFactor));
                setThumbnailSize(width, height);

            }
        }, MagnoliaPinchMoveEvent.TYPE);

        thumbnailSizeSlider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {

                double scaleFactor = (double) event.getValue() / 100.0;
                int widthRequest = (int) (MIN_WIDTH + (MAX_WIDTH - MIN_WIDTH) * scaleFactor);
                int width = Math.max(widthRequest, 10);
                setThumbnailSize(width, width);
            }
        });

        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS | Event.ONSCROLL);
    }

    private void createStubsAndQueryThumbnails() {
        int thumbnailsNeeded = calculateThumbnailsNeeded();
        addStubs(thumbnailsNeeded);
        queryTimer.schedule(QUERY_TIMER_DELAY);
    }

    private void addStubs(int thumbnailsNeeded) {
        for (int i = 0; i < thumbnailsNeeded; ++i) {
            addStub();
        }
    }

    private void addStub() {
        final ThumbnailWidget thumbnailStub = new ThumbnailWidget();
        thumbnailStubs.add(thumbnailStub);
        imageContainer.add(thumbnailStub);
    }

    public void setThumbnailSize(int width, int height) {
        VConsole.log("Thumbnails: setThumbnailSize: " + width + " h:" + height);
        if (this.thumbnailHeight != height || this.thumbnailWidth != width) {
            this.thumbnailHeight = height;
            this.thumbnailWidth = width;

            // Scale the thumbnail divs.
            thumbnailStyle.setProperty("width", width + "px");
            thumbnailStyle.setProperty("height", width + "px");
            String fontSize = String.valueOf((int) (width * 0.75));
            thumbnailStyle.setProperty("fontSize", fontSize + "px");

            // Scale the size of the image in the thumbnails.
            thumbnailImageStyle.setProperty("maxWidth", width + "px");
            thumbnailImageStyle.setProperty("maxHeight", width + "px");

            createStubsAndQueryThumbnails();
        }
    }

    private final Timer queryTimer = new Timer() {
        @Override
        public void run() {
            doQueryThumbnails(thumbnailStubs.size());
        }

        ;
    };

    public void addImages(List<ThumbnailData> thumbnailsData, ServerConnector connector) {
        final Iterator<ThumbnailData> it = thumbnailsData.iterator();
        while (it.hasNext() && !thumbnailStubs.isEmpty()) {
            final ThumbnailWidget thumbnail = thumbnailStubs.remove(0);
            thumbnail.setData(it.next(), connector);
            thumbnails.add(thumbnail);
        }
    }

    public void setThumbnailAmount(int thumbnailAmount) {
        if (this.thumbnailAmount != thumbnailAmount) {
            this.thumbnailAmount = thumbnailAmount;
            int width = getOffsetWidth();
            int totalThumbnailWidth = (thumbnailWidth + getHorizontalMargin());
            if (totalThumbnailWidth != 0) {
                int thumbnailsInRow = (int) (width / totalThumbnailWidth * 1d);
                if (thumbnailsInRow != 0) {
                    int pixelHeight = (int) Math.ceil(thumbnailAmount / thumbnailsInRow * 1d) * (thumbnailHeight + getVerticalMargin());
                    imageContainer.getElement().getStyle().setHeight(pixelHeight, Unit.PX);
                    createStubsAndQueryThumbnails();
                }
            }
        }
    }

    private void doQueryThumbnails(int amount) {
        if (amount > 0) {
            thumbnailService.loadThumbnails(amount);
        }
    }

    public void reset() {
        thumbnails.clear();
        thumbnailStubs.clear();
        imageContainer.clear();
        selectedThumbnail = null;
        thumbnailAmount = 0;
        int thumbnailsNeeded = calculateThumbnailsNeeded();
        addStubs(thumbnailsNeeded);
        doQueryThumbnails(thumbnailsNeeded);
    }

    private int calculateThumbnailsNeeded() {
        int totalHeight = scroller.getVerticalScrollPosition() + getOffsetHeight();
        int width = getOffsetWidth();
        int thumbnailsInRow = (int) (width / (thumbnailWidth + getHorizontalMargin()) * 1d);
        int rows = (int) Math.ceil(1d * totalHeight / (thumbnailHeight + getVerticalMargin()));
        int totalThumbnailsPossible = Math.min(thumbnailAmount, thumbnailsInRow * rows);
        return Math.max(totalThumbnailsPossible - thumbnailStubs.size() - thumbnails.size(), 0);
    }

    public void generateStubs() {
        addStubs(calculateThumbnailsNeeded());
        queryTimer.schedule(QUERY_TIMER_DELAY);
    }

    private int getHorizontalMargin() {
        return ComputedStyle.parseInt(thumbnailStyle.getProperty("marginTop")) * 2;
    }

    private int getVerticalMargin() {
        return ComputedStyle.parseInt(thumbnailStyle.getProperty("marginLeft")) * 2;
    }

    public void setSelectedThumbnail(String thumbnailId) {
        if (thumbnailId != null) {
            for (ThumbnailWidget thumbnail : thumbnails) {
                if (thumbnail.getId().equals(thumbnailId)) {
                    setSelectedThumbnail(thumbnail);
                    return;
                }
            }
        }
        // Either we we're passed null or there was no thumbnail with this id, so we make nothing selected
        setSelectedThumbnail((ThumbnailWidget)null);
    }

    public void setSelectedThumbnail(ThumbnailWidget thumbnail) {
        if (selectedThumbnail != null) {
            selectedThumbnail.setSelected(false);
        }
        if (thumbnail != null) {
            thumbnail.setSelected(true);
        }
        selectedThumbnail = thumbnail;
    }

    @Override
    public void clear() {
        thumbnailStubs.addAll(thumbnails);
        thumbnails.clear();
    }

    public void setThumbnailService(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }
}
