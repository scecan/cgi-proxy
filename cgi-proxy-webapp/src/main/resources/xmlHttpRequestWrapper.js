
(function(global) {
    if (global.XMLHttpRequest) {
        var xhrOpenFn = global.XMLHttpRequest.prototype.open;
        global.XMLHttpRequest.prototype.open = function() {
            console.log("open: "+ arguments[1]);
            return xhrOpenFn.apply(this, arguments);
        }
    }
})(this);
