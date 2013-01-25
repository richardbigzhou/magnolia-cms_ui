info_magnolia_ui_vaadin_lightbox_LightBoxBase = function() {
  
    var anchorControl = $("<div class='zoom-container'/>").append($("<a>", {
        "rel" : 'lightbox', 
        "class": 'zoom-anchor'
    }));
    
    var parent;
    
    var anchor;
    
    this.onStateChange = function() {
        parent = $(".lightboxed" + this.getConnectorId()).parent().append(anchorControl);
        parent.append(anchorControl);
        anchor = anchorControl.children('a');
        var source = this.translateVaadinUri(this.getState().resources['source']['uRL']);
        if (source) {
            anchor.attr("href", source);   
        }
    };
};