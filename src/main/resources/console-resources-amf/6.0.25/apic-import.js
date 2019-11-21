
var st=document.getElementsByTagName('script');
var au=st[st.length - 1].src;
var bp = au.replace('apic-import.js', '');
function addScript(src) {
  var s = document.createElement('script');
  s.setAttribute('nomodule', '');
  s.src = bp + src;
  document.body.appendChild(s);
}
    try{window.importShim=s=>{s=bp+s; return import(s);}}catch(_){};addScript('polyfills/core-js.8e88fc5b880b02431d6fad7b3a34116d.js');addScript('polyfills/systemjs.6dfbfd8f2c3e558918ed74d133a6757a.js');addScript('polyfills/regenerator-runtime.61c01f337f4241bb46c77e10a7e01300.js');try{!function(){function e(t,n){return new Promise(function(e,o){document.head.appendChild(Object.assign(document.createElement("script"),{src:bp+t,onload:e,onerror:o},n?{type:"module"}:void 0))})}var o=[];function t(){"noModule"in HTMLScriptElement.prototype?window.importShim("./api-console.js"):System.import("./legacy/api-console.js")}"fetch"in window||o.push(e("polyfills/fetch.25d91ed49dc86803b0aa17858b018737.js",!1)),"noModule"in HTMLScriptElement.prototype&&!("importShim"in window)&&o.push(e("polyfills/dynamic-import.b745cfc9384367cc18b42bbef2bbdcd9.js",!1)),"attachShadow"in Element.prototype&&"getRootNode"in Element.prototype||o.push(e("polyfills/webcomponents.88b4b5855ede008ecad6bbdd4a69e57d.js",!1)),!("noModule"in HTMLScriptElement.prototype)&&"getRootNode"in Element.prototype&&o.push(e("polyfills/custom-elements-es5-adapter.551c76d38426de62c33d8c61995c1d0f.js",!1)),o.length?Promise.all(o).then(t):t()}()}catch(_){};