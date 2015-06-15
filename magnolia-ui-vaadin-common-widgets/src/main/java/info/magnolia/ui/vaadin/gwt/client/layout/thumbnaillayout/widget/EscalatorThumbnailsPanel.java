/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.LazyThumbnailLayoutConnector;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.ThumbnailLayoutState;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchRecognizer;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchStartEvent;
import info.magnolia.ui.vaadin.gwt.shared.Range;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.googlecode.mgwt.dom.client.recognizer.pinch.UIObjectToOffsetProvider;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapRecognizer;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.client.Util;

/**
 * Implementation of a lazy thumbnail gallery widget.
 *
 * <p/>
 * Laziness is achieved by using a so-called 'escalator' pattern.
 * The real size of the scrollable area is achieved by combing the visible viewport which contains the currently displayed
 * thumbnails with the two spacer elements.
 *
 * <p/>
 * When scrolling occurs the spaces are adjusted to keep the viewport visible and the same thumbnail elements
 * are re-used to create a feeling of a viewport update - as some row moves out of the view it is moved in the DOM to the opposite edge
 * of the viewport (to top or bottom) like an escalator.
 */
public class EscalatorThumbnailsPanel extends FlowPanel {

    private static final String THUMBNAIL_LAYOUT_STYLE_NAME = "thumbnail-layout";
    private static final String THUMBNAIL_SCROLLER_STYLE_NAME = "thumbnail-scroller";
    public static final int MAX_PREFFERED_AMOUNT_OF_THUMBNAILS_IN_ROW = 8;

    /**
     * Listener interface for processing thumbnail click/tap events.
     */
    public interface Listener {

        void onThumbnailClicked(int index, boolean isMetaKeyPressed, boolean isShiftKeyPressed);

        void onThumbnailRightClicked(int index, int xPos, int yPos);

        void onThumbnailDoubleClicked(int index);

    }

    private Thumbnail thumbnailFlyweight;

    private final ScrollPanel scroller = new ScrollPanel();

    private final Element imageContainer = DOM.createDiv();

    private final DivElement upperSpacer = DivElement.as(DOM.createDiv());

    private final DivElement lowerSpacer = DivElement.as(DOM.createDiv());

    private LazyThumbnailLayoutConnector.ThumbnailService thumbnailService;

    private LinkedList<Element> floatingThumbnails = new LinkedList<Element>();

    private int absoluteOffset = 0;

    private int thumbnailAmount;

    private int thumbnailsInRow;

    private int rowsInViewport;

    private ThumbnailsSizeKeeper size;

    private boolean isScrollProcessorLocked = false;

    private Listener listener;

    private final Slider thumbnailSizeSlider = new Slider();

    private final ScrollHandler scrollHandler = new ScrollHandler() {

        private int lastScrollTop = 0;

        @Override
        public void onScroll(ScrollEvent event) {
            if (isScrollProcessorLocked) {
                return;
            }
            int newScrollTop = event.getRelativeElement().getScrollTop();
            int delta = lastScrollTop - newScrollTop;
            escalate(newScrollTop, delta);
            lastScrollTop = newScrollTop;
        }
    };

    /**
     * On scroll - fix the spacer elements and replace the thumbnails that have gone out of view
     * with stubs.
     */
    private void escalate(int newScrollTop, int delta) {
        int lsHeight = lowerSpacer.getOffsetHeight();
        int usHeight = upperSpacer.getOffsetHeight();

        int thumbnailHeight = size.height();


        boolean moveOccurred = false;

        if (delta != 0) {
            /**
             * Scrolling down - moving thumbnail rows from top to bottom
             * in order to mimic an always visible viewport.
             */
            if (delta < 0) {
                while (newScrollTop - usHeight >= thumbnailHeight && lsHeight > 0) {
                    moveOccurred = true;

                    usHeight += thumbnailHeight;
                    lsHeight -= thumbnailHeight;

                    this.absoluteOffset = usHeight / size.height() * thumbnailsInRow;

                    // Tossing around stubs makes no sense in case there's no 'real' thumbnails in viewport
                    if (!areAllVisibleThumbnailsCleared()) {
                        releaseThumbnailRow(true);
                        addStubs(Range.withLength(imageContainer.getChildCount(), thumbnailsInRow));
                    }
                }
            } else {
                /**
                 * Scrolling up - moving thumbnail rows from bottom to top
                 */
                while (usHeight - newScrollTop > 0 && usHeight > 0) {
                    moveOccurred = true;

                    usHeight -= thumbnailHeight;
                    lsHeight += thumbnailHeight;

                    this.absoluteOffset = usHeight / size.height() * thumbnailsInRow;

                    // Tossing around stubs makes no sense in case there's no 'real' thumbnails in viewport
                    if (!areAllVisibleThumbnailsCleared()) {
                        releaseThumbnailRow(false);
                        addStubs(Range.between(0, thumbnailsInRow));
                    }
                }
            }
        }


        if (moveOccurred) {
            setSpacersHeight(usHeight, lsHeight);
            updateViewport();
        }
    }

    public EscalatorThumbnailsPanel(final Listener listener) {
        this.listener = listener;

        final TouchDelegate touchDelegate = new TouchDelegate(this);
        touchDelegate.addTouchHandler(new MagnoliaPinchRecognizer(touchDelegate, new UIObjectToOffsetProvider(scroller)));
        MultiTapRecognizer multitapRecognizer = new MultiTapRecognizer(touchDelegate, 1, 2);
        touchDelegate.addTouchHandler(multitapRecognizer);

        addHandler(new MagnoliaPinchStartEvent.Handler() {
            @Override
            public void onPinchStart(MagnoliaPinchStartEvent event) {
                /**
                 * TODO: Pinch does not work reliably yet, to be sorted out...
                 */
                //scale((float) event.getScaleFactor());
            }
        }, MagnoliaPinchStartEvent.TYPE);

        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final NativeEvent nativeEvent = event.getNativeEvent();
                final Element element = findThumbnail(Element.as(nativeEvent.getEventTarget()));
                if (element != null) {
                    boolean isMetaKeyPressed = nativeEvent.getMetaKey();
                    boolean isShiftPressed = nativeEvent.getShiftKey();
                    EscalatorThumbnailsPanel.this.listener.onThumbnailClicked(
                            getThumbnailIndex(element),
                            isMetaKeyPressed,
                            isShiftPressed);
                }

            }
        }, ClickEvent.getType());

        addDomHandler(new ContextMenuHandler() {

            @Override
            public void onContextMenu(ContextMenuEvent event) {
                Element thumbnail = findThumbnail(Element.as(event.getNativeEvent().getEventTarget()));
                if (thumbnail != null) {
                    EscalatorThumbnailsPanel.this.listener.onThumbnailRightClicked(
                            getThumbnailIndex(thumbnail),
                            event.getNativeEvent().getClientX(),
                            event.getNativeEvent().getClientY());
                }

            }
        }, ContextMenuEvent.getType());

        addHandler(new MultiTapHandler() {
            @Override
            public void onMultiTap(MultiTapEvent event) {
                int x = event.getTouchStarts().get(0).get(0).getPageX();
                int y = event.getTouchStarts().get(0).get(0).getPageY();
                final Element thumbnail = findThumbnail(Util.getElementFromPoint(x, y));
                if (thumbnail != null) {
                    EscalatorThumbnailsPanel.this.listener.onThumbnailDoubleClicked(getThumbnailIndex(thumbnail));
                }
            }
        }, MultiTapEvent.getType());

        thumbnailSizeSlider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                scale(event.getValue() / 100f);
            }
        });

        add(thumbnailSizeSlider);

        this.size = new ThumbnailsSizeKeeper(imageContainer);

        addStyleName(THUMBNAIL_LAYOUT_STYLE_NAME);

        scroller.getElement().appendChild(imageContainer);
        scroller.addStyleName(THUMBNAIL_SCROLLER_STYLE_NAME);
        scroller.addScrollHandler(scrollHandler);

        scroller.getElement().insertFirst(upperSpacer);
        scroller.getElement().insertAfter(lowerSpacer, imageContainer);

        add(scroller);

        thumbnailFlyweight = new Thumbnail();
    }

    public final int getCurrentThumbnailOffset() {
        return absoluteOffset;
    }

    public int getCurrentlyDisplayedThumbnails() {
        return imageContainer.getChildCount();
    }

    public void initialize(int thumbnailAmount, int offset, ThumbnailLayoutState.ThumbnailSize size, float scaleRatio, boolean isFirstUpdateFromState) {
        final NodeList<Node> childNodes = this.imageContainer.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            imageContainer.removeChild(childNodes.getItem(i));
        }

        this.thumbnailAmount = thumbnailAmount;
        this.size.updateAllThumbnailsSize(size.width, size.height);
        // Set initial scale ratio
        if (scaleRatio >= 0) {
            this.size.scale(scaleRatio);
        }

        // Calculate initial sizes
        resize();

        // If amount of thumbnails in a row exceeds the default threshold - scale them so that
        // there's not that many thumbnails in viewport
        if (isFirstUpdateFromState && thumbnailsInRow > MAX_PREFFERED_AMOUNT_OF_THUMBNAILS_IN_ROW) {
            int calculatedThumbnailWidth = imageContainer.getOffsetWidth() / MAX_PREFFERED_AMOUNT_OF_THUMBNAILS_IN_ROW;
            scaleToWidth(calculatedThumbnailWidth);
        }

        thumbnailSizeSlider.setValue((int) (this.size.getScaleRatio() * 100), false);
        scroller.setVerticalScrollPosition((offset / thumbnailsInRow) * size.height);
    }

    public void setThumbnailService(LazyThumbnailLayoutConnector.ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    public void setThumbnailAmount(int amount) {
        if (this.thumbnailAmount != amount) {
            this.thumbnailAmount = amount;
            resize();
        }
    }

    public void setThumbnailSize(int width, int height) {
        if (size.updateAllThumbnailsSize(width, height)) {
            resize();
        }
    }

    public Range getDisplayedRange() {
        return Range.between(absoluteIndex(0), absoluteIndex(imageContainer.getChildCount()));
    }

    public void resize() {
        int initialScrollTop = scroller.getVerticalScrollPosition();
        int viewportWidth = getScrollableWidth();
        int scrollerHeight = getScrollableHeight();
        int thumbnailHeight = size.height();

        if (scrollerHeight == 0 || viewportWidth == 0 || thumbnailHeight == 0) {
            return;
        }

        int thumbnailWidth = size.width();
        this.thumbnailsInRow = viewportWidth / thumbnailWidth;
        int totalRows = (int) Math.ceil((double) thumbnailAmount / thumbnailsInRow);
        this.rowsInViewport = Math.min((int) Math.ceil((double) scrollerHeight / thumbnailHeight) + 1, totalRows);

        final Range totalRange = Range.between(0, thumbnailAmount);

        Range offsetRange = Range.between(0, absoluteOffset);
        Range viewportActualRange = Range.withLength(absoluteOffset, getCurrentlyDisplayedThumbnails());
        Range remainingRange = totalRange.partitionWith(viewportActualRange)[2];

        boolean rangesAreInOrder = false;
        /**
         * Iterate until all calculations are aligned. According to the algorithm specifics there should be never more than two
         * iterations - the additional one could occur if there is not enough thumbnails in the viewport and we can't fill them
         * from the lower spacer - then we need to shift the upper spacer and take some thumbnails from there which means the change of
         * the offset and the upper spacer alignment.
         */
        while (!rangesAreInOrder) {
            /**
             * Re-calculate the offset first: the upper-spacer must be fully filled with 'virtual' thumbnails in order
             * to provide the correct value of the offset, so we need to see if the amount of thumbnails currently residing in
             * the offset range can be divided by the new amount of thumbnails per-row witout remainder. If there is a remainder -
             * it should be pushed into viewport and the offset should be adjusted.
             */
            int offsetRangeRemainder = offsetRange.length() % thumbnailsInRow;
            if (offsetRangeRemainder != 0) {
                addStubs(Range.withLength(relativeIndex(viewportActualRange.getStart()), offsetRangeRemainder));
                offsetRange = offsetRange.expand(0, -offsetRangeRemainder);
                viewportActualRange = viewportActualRange.expand(offsetRangeRemainder, 0);
                absoluteOffset -= offsetRangeRemainder;
            }

            /**
             * Update the viewport. We can be either in situation when there is not enough thumbnails in viewport (when we zoom out or
             * enlarge the viewport) or there is an overflow of thumbnails (when we zoom in).
             */
            Range viewportCalculatedRange = Range.withLength(absoluteOffset, rowsInViewport * thumbnailsInRow);

            /**
             * Let's see if the amount of thumbnails we have to display in viewport does not exceed the total amount.
             * {@code beyondTheTotalRange} variable tracks that overflow if any.
             */
            final Range[] calculatedVsTotalRange = viewportCalculatedRange.partitionWith(totalRange);
            final Range beyondTheTotalRange = calculatedVsTotalRange[2];

            /**
             * Let us meanwhile reduce the range of thumbnails to be displayed in viewport to those that are within available range.
             */
            viewportCalculatedRange = calculatedVsTotalRange[1];

            /**
             * In case we already display too many thumbnails - release the overflow.
             */
            if (viewportCalculatedRange.isSubsetOf(viewportActualRange)) {

                int overflow = viewportActualRange.partitionWith(viewportCalculatedRange)[2].length();
                releaseThumbnails(Range.withLength(imageContainer.getChildCount() - overflow, overflow));
                viewportActualRange = viewportActualRange.expand(0, -overflow);
                remainingRange = remainingRange.expand(overflow, 0);
                /**
                 * Else in case there're some thumbnails to be added - let's do that.
                 */
            } else if (viewportActualRange.isSubsetOf(viewportCalculatedRange)) {

                int lack = viewportCalculatedRange.partitionWith(viewportActualRange)[2].length();
                addStubs(Range.withLength(imageContainer.getChildCount(), lack));
                viewportActualRange = viewportActualRange.expand(0, lack);
                remainingRange = remainingRange.expand(-lack, 0);
            }

            /**
             * If the initial calculated range to be displayed goes beyond the total amount of thumbnails:
             * Fill viewport with the thumbnails that are displayed above by kind of "shifting" the viewport back.
             */
            if (!beyondTheTotalRange.isEmpty()) {
                int rowsToShift = (int) Math.floor((double) beyondTheTotalRange.length() / thumbnailsInRow);
                int toQueryFromOffset = rowsToShift * thumbnailsInRow;
                if (toQueryFromOffset > 0) {
                    absoluteOffset -= toQueryFromOffset;
                    offsetRange = offsetRange.expand(0, -toQueryFromOffset);
                    viewportActualRange = viewportActualRange.expand(toQueryFromOffset, 0);
                    addStubs(Range.between(0, toQueryFromOffset));
                }
            }

            // Since we might have compensated the lack of thumbnails in viewport from the offset
            // area - it might get out shape, we'd have to repeat the operation
            rangesAreInOrder = offsetRange.length() % thumbnailsInRow == 0;
        }

        /**
         * Update sized according to the calculations above, fix the scroll top based on the relation of the
         * former upper spacer height, to its new height.
         */
        int usH = upperSpacer.getOffsetHeight();


        int viewportHeight = rowsInViewport * thumbnailHeight;
        int upperSpacerHeight = (int) Math.ceil(offsetRange.length() / thumbnailsInRow) * thumbnailHeight;
        int lowerSpacerHeight = (int) Math.ceil(remainingRange.length() / thumbnailsInRow) * thumbnailHeight;

        // Set the sizes finally
        scroller.setHeight(scrollerHeight + "px");

        imageContainer.getStyle().setHeight(viewportHeight, Style.Unit.PX);
        imageContainer.getStyle().setWidth(viewportWidth, Style.Unit.PX);

        setSpacersHeight(upperSpacerHeight, lowerSpacerHeight);

        /**
         * Try to put the scroll top to where it used to be approximately.
         */
        isScrollProcessorLocked = true;
        scroller.setVerticalScrollPosition(initialScrollTop + upperSpacerHeight - usH);
        Scheduler.get().scheduleFinally(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                isScrollProcessorLocked = false;
                return false;
            }
        });

        updateViewport();
    }

    protected Element findThumbnail(Element element) {
        if (element != imageContainer && element != null && imageContainer.isOrHasChild(element)) {
            Element result = element;
            while (result.getParentElement() != imageContainer) {
                result = result.getParentElement();
            }
            return result;
        }
        return null;
    }

    protected int getThumbnailIndex(Element element) {
        return absoluteIndex(Util.getChildElementIndex((com.google.gwt.user.client.Element) element));
    }

    protected void scaleToWidth(int width) {
        size.scaleToWidth(width);
        thumbnailService.onThumbnailsScaled(size.getScaleRatio());
        resize();
    }

    protected void scale(float ratio) {
        thumbnailService.onThumbnailsScaled(ratio);
        size.scale(ratio);
        resize();
    }

    private void addStubs(Range relativeRange) {
        int currentThumbnailOffset = getCurrentThumbnailOffset();
        final Range actualRange = relativeRange.expand(-currentThumbnailOffset, currentThumbnailOffset).restrictTo(Range.between(0, thumbnailAmount));

        int relativeStartIndex = relativeIndex(actualRange.getStart());

        Element previousElement = relativeStartIndex == 0 ? null : Element.as(imageContainer.getChild(relativeStartIndex - 1));
        for (int i = 0; i < actualRange.length(); ++i) {
            final Element stub = floatingThumbnails.isEmpty() ? thumbnailFlyweight.createThumbnail() : floatingThumbnails.remove(0);
            size.applySizeToThumbnail(stub);
            if (previousElement == null) {
                imageContainer.insertFirst(stub);
            } else {
                imageContainer.insertAfter(stub, previousElement);
            }
            previousElement = stub;
        }
    }

    private void updateViewport() {
        final Range visibleRange = Range.between(absoluteIndex(0), absoluteIndex(imageContainer.getChildCount()));
        if (areAllVisibleThumbnailsCleared()) {
            final Range availableRange = Range.between(0, thumbnailAmount);
            if (!visibleRange.isSubsetOf(availableRange)) {
                Range thumbnailsToRelease = visibleRange.partitionWith(availableRange)[2];
                if (!thumbnailsToRelease.isEmpty()) {
                    for (int i = 0; i < thumbnailsToRelease.length(); ++i) {
                        clearThumbnail(Element.as(imageContainer.getLastChild()));
                    }
                }
            }
        }
        this.thumbnailService.onViewportChanged(Range.between(absoluteIndex(0), absoluteIndex(imageContainer.getChildCount())));
    }

    public void updateImageSource(String url, int index) {
        thumbnailFlyweight.setImageSrc(url, Element.as(imageContainer.getChild(relativeIndex(index))));
    }

    private int relativeIndex(int index) {
        return index - getCurrentThumbnailOffset();
    }

    private int absoluteIndex(int relativeIndex) {
        return getCurrentThumbnailOffset() + relativeIndex;
    }

    private void releaseThumbnailRow(boolean inFront) {
        for (int i = 0; i < thumbnailsInRow; ++i) {
            final Element thumbnail = (Element) (inFront ? imageContainer.getFirstChild() : imageContainer.getLastChild());
            clearThumbnail(thumbnail);
        }
    }

    private void releaseThumbnails(Range between) {
        final Set<Element> toRelease = new HashSet<Element>();
        for (int i = between.getStart(); i < between.getEnd(); ++i) {
            toRelease.add(Element.as(imageContainer.getChild(i)));
        }

        for (final Element thumbnail : toRelease) {
            clearThumbnail(thumbnail);
        }
    }

    private void clearThumbnail(Element thumbnail) {
        thumbnailFlyweight.clear(thumbnail);
        thumbnail.removeFromParent();
        floatingThumbnails.add(thumbnail);
    }

    public void updateIconFontStyle(String style, int index) {
        thumbnailFlyweight.setIconFontStyle(style, Element.as(imageContainer.getChild(relativeIndex(index))));
    }

    public void setSelectedThumbnailsViaIndices(List<Integer> indices) {
        clearSelection();
        for (int absIndex : indices) {
            Element.as(imageContainer.getChild(relativeIndex(absIndex))).addClassName("selected");
        }
    }

    private void setSpacersHeight(int usHeight, int lsHeight) {
        upperSpacer.getStyle().setHeight(Math.max(usHeight, 0), Style.Unit.PX);
        lowerSpacer.getStyle().setHeight(Math.max(lsHeight, 0), Style.Unit.PX);
    }

    private void clearSelection() {
        final JQueryWrapper selectedThumbnails = JQueryWrapper.select((com.google.gwt.user.client.Element)imageContainer).find(".selected");
        if (selectedThumbnails != null) {
            selectedThumbnails.removeClass("selected");
        }
    }

    private boolean areAllVisibleThumbnailsCleared() {
        return imageContainer.getChildCount() == JQueryWrapper.select((com.google.gwt.user.client.Element)imageContainer).find(".cleared").size();
    }

    private int getScrollableHeight() {
        return getElement().getOffsetHeight() - thumbnailSizeSlider.getOffsetHeight();
    }

    private int getScrollableWidth() {
        return getOffsetWidth();
    }

    private static class Thumbnail {

        static final String ICON_STYLE_NAME = "icon";
        static final String THUMBNAIL_STYLE_NAME = "thumbnail";
        static final String THUMBNAIL_IMAGE_STYLE_NAME = "thumbnail-image";
        public static final String CLEARED_STYLE_NAME = "cleared";

        Element createThumbnail() {

            final DivElement thumbnail = DivElement.as(DOM.createDiv());
            thumbnail.addClassName(THUMBNAIL_STYLE_NAME);

            final SpanElement iconFontEl = SpanElement.as(DOM.createSpan());
            iconFontEl.addClassName(ICON_STYLE_NAME);
            Style style = iconFontEl.getStyle();
            style.setDisplay(Style.Display.NONE);
            style.setFontSize(24, Style.Unit.PX);
            style.setLineHeight(1, Style.Unit.PX);

            final ImageElement image = ImageElement.as(DOM.createImg());
            image.addClassName(THUMBNAIL_IMAGE_STYLE_NAME);
            image.getStyle().setDisplay(Style.Display.NONE);

            thumbnail.appendChild(image);
            thumbnail.appendChild(iconFontEl);

            return thumbnail;
        }

        void setImageSrc(String src, Element thumbnail) {
            final Element img = getImage(thumbnail);
            final Element icon = getIcon(thumbnail);

            img.getStyle().setDisplay(Style.Display.INLINE_BLOCK);
            icon.getStyle().setDisplay(Style.Display.NONE);

            img.setAttribute("src", src);
            thumbnail.removeClassName(CLEARED_STYLE_NAME);
        }

        void setIconFontStyle(String style, Element thumbnail) {
            final Element img = getImage(thumbnail);
            final Element icon = getIcon(thumbnail);

            img.getStyle().setDisplay(Style.Display.NONE);
            icon.getStyle().setDisplay(Style.Display.INLINE_BLOCK);

            icon.setClassName(ICON_STYLE_NAME);
            icon.addClassName(style);
            thumbnail.removeClassName(CLEARED_STYLE_NAME);
        }

        private Element getIcon(Element thumbnail) {
            return Element.as(thumbnail.getChild(1));
        }

        private Element getImage(Element thumbnail) {
            return Element.as(thumbnail.getChild(0));
        }

        public void clear(Element thumbnail) {
            Element icon = getIcon(thumbnail);
            icon.setClassName(ICON_STYLE_NAME);
            icon.getStyle().setDisplay(Style.Display.NONE);

            Element image = getImage(thumbnail);
            image.removeAttribute("src");
            image.getStyle().setDisplay(Style.Display.NONE);

            thumbnail.addClassName(CLEARED_STYLE_NAME);
        }
    }
}
