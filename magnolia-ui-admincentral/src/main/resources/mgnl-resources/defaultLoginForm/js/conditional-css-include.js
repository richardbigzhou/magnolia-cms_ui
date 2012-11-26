var metas = document.getElementsByTagName("meta")
var contextPath = ""
for(i=0; i < metas.length; i++) {
    if(metas[i].name=='ctxPath') {
      contextPath = metas[i].content
      break
    }
}
if (navigator.userAgent.toLowerCase().indexOf('webkit')>-1 || navigator.appName == 'Microsoft Internet Explorer') {
    document.write('<link rel="stylesheet" type="text/css" href="' + contextPath + '/.resources/defaultLoginForm/css/fonts-heavy.css" />');
}else{
    document.write('<link rel="stylesheet" type="text/css" href="' + contextPath + '/.resources/defaultLoginForm/css/fonts-default.css" />');
}
