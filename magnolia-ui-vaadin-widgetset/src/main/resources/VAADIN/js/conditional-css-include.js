if (navigator.userAgent.toLowerCase().indexOf('webkit')>-1 || navigator.appName == 'Microsoft Internet Explorer') {
    document.write('<link rel="stylesheet" type="text/css" href="../VAADIN/themes/admincentraltheme/css-conditional/fonts-heavy.css" />');
}else{
    document.write('<link rel="stylesheet" type="text/css" href="../VAADIN/themes/admincentraltheme/css-conditional/fonts-default.css" />');
}
