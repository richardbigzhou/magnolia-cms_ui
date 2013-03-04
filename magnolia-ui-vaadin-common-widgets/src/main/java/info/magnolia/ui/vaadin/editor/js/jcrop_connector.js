info_magnolia_ui_vaadin_editor_JCrop = function() {
	
    var jcrop = null;

    var self = this;

    var options = {};
    
    var statusLabel = null;
    
    var that = this;
    
    var lastBoxHeight = 0;
    
    var lastBoxWidth = 0;
    
    this.onStateChange = function() {
        var state = this.getState();
        var image = this.queryImage();
        options = {
            onSelect : this.onSelect,
            onChange : this.doOnChange,
            onRelease : this.doOnRelease,
            bgColor : state.backgroundColor,
            bgOpacity : state.backgroundOpacity,
            minSize : [ state.minWidth, state.minHeight ],
            maxSize : [ state.maxWidth, state.maxHeight ],
        	boxWidth : parseInt(image.css("width")),
    		boxHeight : parseInt(image.css("height"))            
        };
    	
        if (state.trueHeight > 0 && state.trueWidth > 0) {
        	options.trueSize = [state.trueWidth, state.trueHeight];
        }
        
        if (state.selection) {
            var selection = state.selection;
            options.setSelect = [ selection.left, selection.top,
                    selection.left + selection.width,
                    selection.top + selection.height ];
        }

        if (state.aspectRatio > 0) {
            options.aspectRatio = state.aspectRatio;
        }

        if (!jcrop || !state.isValid) {
        	this.invalidate();
        } else {
            jcrop.setOptions(options);
            if (!self.getState().enabled) {
                this.disable();
            }
        }
    };

    this.onSelect = function(coords) {
    	var trueSize = jcrop.tellSelect();
    	var scaled = jcrop.tellScaled();
    	var param = coords;
    	that.doOnSelect(coords);
    }
    
    this.doOnChange = function(coords) {
    	statusLabel = $(".crop-status" + that.getConnectorId());
    	if (statusLabel) {
    		statusLabel.html(
    				"Position: " + Math.floor(coords.x) + 
    				"," + Math.floor(coords.y)  + 
    				"  Cropped size: " + 
    				Math.floor(coords.w) + "," + Math.floor(coords.h));
    	}
    };
    
    this.invalidate = function() {
    	if (jcrop) {
    		jcrop.destroy();
    	}
    	this.initJCrop(options);
    };
    
    this.initJCrop = function(options) {
        this.queryImage().Jcrop(options, function() {
            jcrop = this;
            if (!self.getState().enabled) {
                this.disable();
            }
        });
        this.onCreated();
    };

    this.onUnregister = function() {
        if (jcrop) {
            jcrop.destroy();
            jcrop = null;
            this.queryImage().css("visibility", "");
        }
    };

    this.queryImage = function() {
        return $(".croppable" + this.getConnectorId());
    };

    this.animateTo = function(area, intval) {
        if (jcrop) {
            var newArea = [ area.left, area.top, area.left + area.width,
                    area.top + area.height ];
            jcrop.animateTo(newArea);
        }
    };
};
