window.info_magnolia_ui_ckeditor_CKEditor = function() {

   var element = this.getElement();
   element.id = 'editable';
   element.setAttribute('contenteditable', 'true');


   // Turn off automatic editor creation first.
   CKEDITOR.disableAutoInline = true;
   var ckeditorInline = CKEDITOR.inline( 'editable' );

   this.insertText = function(txt) {
	   ckeditorInline.insertText(txt);
   };

   this.insertHtml = function(html) {
	   ckeditorInline.insertHtml(html);
   };

}
