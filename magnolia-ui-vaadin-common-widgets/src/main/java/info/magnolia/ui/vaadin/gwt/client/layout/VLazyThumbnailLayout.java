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
package info.magnolia.ui.vaadin.gwt.client.layout;

import info.magnolia.ui.vaadin.gwt.client.icon.GwtIcon;
import info.magnolia.ui.vaadin.gwt.client.mgwt.SliderClientBundle;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchRecognizer;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchStartEvent;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.csstools.client.CSSRule;
import org.vaadin.csstools.client.ComputedStyle;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.googlecode.mgwt.dom.client.recognizer.pinch.UIObjectToOffsetProvider;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapRecognizer;
import com.googlecode.mgwt.ui.client.widget.MSlider;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Client side impl of lazy asset thumbnails layout.
 * 
 */
public class VLazyThumbnailLayout extends Composite implements Paintable, ClientSideHandler {

    private static final int QUERY_TIMER_DELAY = 250;

    private static final int MIN_WIDTH = 50;
    private static final int MAX_WIDTH = 230;

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    private int thumbnailAmount = 0;

    private VThumbnail selectedThumbnail = null;

    private final List<VThumbnail> thumbnails = new ArrayList<VThumbnail>();

    private final List<VThumbnail> thumbnailStubs = new ArrayList<VThumbnail>();

    private final ScrollPanel scroller = new ScrollPanel();
    private final FlowPanel basePanel = new FlowPanel();

    private final SliderClientBundle thumbnailBundle = GWT.create(SliderClientBundle.class);
    private final MSlider thumbnailSizeSlider = new MSlider(thumbnailBundle.css());

    private final CSSRule thumbnailImageStyle = CSSRule.create(".thumbnail-image");
    private final CSSRule thumbnailStyle = CSSRule.create(".thumbnail");

    private final FlowPanel imageContainer = new FlowPanel();

    private final ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("addThumbnails", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final JsArray<VThumbnailData> urls = parseStringArray(String.valueOf(params[0]));
                    addImages(urls);
                }
            });

            register("setThumbnailAmount", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setThumbnailAmount((Integer) params[0]);
                }
            });

            register("setThumbnailSize", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setThumbnailSize((Integer) params[0], (Integer) params[1]);
                }
            });

            register("clear", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    reset();
                }
            });
        }
    };

    public VLazyThumbnailLayout() {

        // Prepare thumbnail size slider.
        thumbnailSizeSlider.setWidth("125px");
        GwtIcon iconSizeSmall = new GwtIcon();
        iconSizeSmall.updateIconName("slider-min");
        iconSizeSmall.updateSize(30);
        iconSizeSmall.updateColor("#aaaaaa");

        GwtIcon iconSizeLarge = new GwtIcon();
        iconSizeLarge.updateIconName("slider-max");
        iconSizeLarge.updateSize(30);
        iconSizeLarge.updateColor("#aaaaaa");

        basePanel.add(iconSizeSmall);
        basePanel.add(thumbnailSizeSlider);
        basePanel.add(iconSizeLarge);

        scroller.setWidget(imageContainer);
        basePanel.add(scroller);

        initWidget(basePanel);

        thumbnailStyle.setProperty("margin", "3px");

        basePanel.addStyleName("thumbnail-view-base-panel");
        scroller.addStyleName("thumbnail-scroller");

        bindHandlers();
    }

    protected void bindHandlers() {

        scroller.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                createStubsAndQueryThumbnails();
            }
        });

        final TouchDelegate touchDelegate = new TouchDelegate(this);
        touchDelegate
                .addTouchHandler(new MagnoliaPinchRecognizer(touchDelegate, new UIObjectToOffsetProvider(scroller)));
        MultiTapRecognizer multitapRecognizer = new MultiTapRecognizer(touchDelegate, 1, 2);
        addHandler(new MultiTapHandler() {
            @Override
            public void onMultiTap(MultiTapEvent event) {
                final Element element = Util.getElementFromPoint(event.getTouchStarts().get(0).get(0).getPageX(), event
                        .getTouchStarts().get(0).get(0).getPageY());
                final VThumbnail thumbnail = Util.findWidget(element, VThumbnail.class);
                proxy.call("thumbnailDoubleClicked", thumbnail.getId());
            }
        }, MultiTapEvent.getType());

        touchDelegate.addTouchHandler(multitapRecognizer);

        addHandler(new MagnoliaPinchMoveEvent.Handler() {
            @Override
            public void onPinchMove(MagnoliaPinchMoveEvent event) {
                double scaleFactor = 1 / event.getScaleFactor();
                int width = Math.max((int) (ComputedStyle.parseInt(thumbnailStyle.getProperty("width")) * scaleFactor),
                        25);
                int height = Math.max(
                        (int) (ComputedStyle.parseInt(thumbnailStyle.getProperty("height")) * scaleFactor), 25);

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

                // TODO: CLZ Look into scroll synchronization -
                // scroller.setVerticalScrollPosition((int)
                // (scroller.getVerticalScrollPosition() * scaleFactor));
                setThumbnailSize(width, width);
            }
        });

        addHandler(new MagnoliaPinchStartEvent.Handler() {
            @Override
            public void onPinchStart(MagnoliaPinchStartEvent event) {
                thumbnailStubs.addAll(thumbnails);
                thumbnails.clear();
                proxy.call("clear");
            }
        }, MagnoliaPinchStartEvent.TYPE);

        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS | Event.ONSCROLL);

        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Element element = event.getNativeEvent().getEventTarget().cast();
                final VThumbnail thumbnail = Util.findWidget(element, VThumbnail.class);
                if (thumbnail != null) {
                    if (selectedThumbnail != null) {
                        selectedThumbnail.setSelected(false);
                    }
                    thumbnail.setSelected(true);
                    selectedThumbnail = thumbnail;
                    proxy.call("thumbnailSelected", thumbnail.getId());
                }
            }
        }, ClickEvent.getType());

        addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {

            }
        }, DoubleClickEvent.getType());
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
        final VThumbnail thumbnailStub = new VThumbnail();
        thumbnailStubs.add(thumbnailStub);
        imageContainer.add(thumbnailStub);
    }

    private void setThumbnailSize(int width, int height) {
        VConsole.log("Thumbnails: setThumbnailSize: " + width + " h:" + height);

        this.thumbnailHeight = height;
        this.thumbnailWidth = width;
        // Scale the thumbnail divs.
        thumbnailStyle.setProperty("width", width + "px");
        thumbnailStyle.setProperty("height", width + "px");
        String fontSize = Integer.toString((int) (width * 0.75));
        thumbnailStyle.setProperty("fontSize", fontSize + "px");

        // Scale the size of the image in the thumbnails.
        thumbnailImageStyle.setProperty("maxWidth", width + "px");
        thumbnailImageStyle.setProperty("maxHeight", width + "px");

        createStubsAndQueryThumbnails();
    }

    private final Timer queryTimer = new Timer() {
        @Override
        public void run() {
            doQueryThumbnails(thumbnailStubs.size());
        };
    };

    private void addImages(JsArray<VThumbnailData> urls) {
        for (int i = 0; i < urls.length() && !thumbnailStubs.isEmpty(); ++i) {
            final VThumbnail thumbnail = thumbnailStubs.remove(0);
            final VThumbnailData data = urls.get(i);
            thumbnail.setData(data);
            thumbnails.add(thumbnail);
        }
    }

    private void setThumbnailAmount(int thumbnailAmount) {
        this.thumbnailAmount = thumbnailAmount;
        int width = getOffsetWidth();
        int totalThumbnailWidth = (thumbnailWidth + getHorizontalMargin());
        if (totalThumbnailWidth != 0) {
            int thumbnailsInRow = (int) (width / totalThumbnailWidth * 1d);
            if (thumbnailsInRow != 0) {
                int rows = (int) (thumbnailAmount / thumbnailsInRow * 1d) * (thumbnailHeight + getVerticalMargin());
                imageContainer.getElement().getStyle().setHeight(rows, Unit.PX);
                createStubsAndQueryThumbnails();
            }
        }
    }

    private void doQueryThumbnails(int amount) {
        if (amount > 0) {
            proxy.call("loadThumbnails", amount);
        }
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unnknown server call: " + method);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        proxy.update(this, uidl, client);
    }

    @Override
    public boolean initWidget(Object[] params) {
        reset();
        return false;
    }

    private void reset() {
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

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        addStubs(calculateThumbnailsNeeded());
        queryTimer.schedule(QUERY_TIMER_DELAY);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        final Integer heightPx = ComputedStyle.parseInt(height);
        if (heightPx != null) {
            scroller.setHeight((heightPx - thumbnailSizeSlider.getOffsetHeight()) + "px");
            addStubs(calculateThumbnailsNeeded());
            queryTimer.schedule(QUERY_TIMER_DELAY);
        }
    }

    public static native JsArray<VThumbnailData> parseStringArray(String json) /*-{
                                                                               return eval('(' + json + ')');
                                                                               }-*/;

    private int getHorizontalMargin() {
        return ComputedStyle.parseInt(thumbnailStyle.getProperty("marginTop")) * 2;
    }

    private int getVerticalMargin() {
        return ComputedStyle.parseInt(thumbnailStyle.getProperty("marginLeft")) * 2;
    }
}
