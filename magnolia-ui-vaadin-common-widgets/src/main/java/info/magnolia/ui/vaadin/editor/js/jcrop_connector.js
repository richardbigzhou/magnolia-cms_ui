info_magnolia_ui_vaadin_editor_JCrop = function() {
	
	this.image = null;
	
	this.jcrop = null;
	
	var self = this;
	
	this.onStateChange = function() {
        image = $(".croppable" + this.getConnectorId());
    };
	
	this.on = function() {
        if (image && !this.jcrop) {
			image.Jcrop({
				onSelect: this.doOnSelect,
				onChange: this.doOnChange,
				onRelease: this.doOnRelease
			}, function() {
				self.jcrop = this;
			});	
		}
	};
	
	this.enable = function() {
		if (this.jcrop) {
			this.jcrop.enable();	
		}
	};
	
	this.disable = function() {
		if (this.jcrop) {
			this.jcrop.disable();	
		}
	};
	
	this.off = function() {
		if (this.jcrop) {
			this.jcrop.destroy();
			this.jcrop = undefined;
			image.css("visibility", "");
		}
	};
};
