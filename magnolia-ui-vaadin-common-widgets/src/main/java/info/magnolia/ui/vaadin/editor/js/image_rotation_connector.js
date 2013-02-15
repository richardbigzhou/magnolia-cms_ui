info_magnolia_ui_vaadin_editor_ImageRotaion = function() {
	
    this.onStateChange = function() {
        var state = this.getState();
        var angle = state.angle;
        var image = $(".rotatable" + this.getConnectorId());
        
        var cos = Math.cos(angle * 3.14 / 180);
        var sin = Math.sin(angle * 3.14 / 180);
        var w = image.width();
        var h = image.height();
        image.rotate(angle);
        image.parent().css("width", h * sin + w * cos);
        image.parent().css("height", w * sin + h * cos);
    };
    
};