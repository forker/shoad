


function ArrayDiff(arr1, arr2) {
    this.firstOnly = arr1.slice();
    this.secondOnly = arr2.slice();
    this.common = [];
    
    for(var i=this.firstOnly.length-1; i>=0; i--) {
        for(var j in this.secondOnly) {
            if(this.firstOnly[i] === this.secondOnly[j]) {
                this.common.push(this.firstOnly[i]);
                this.firstOnly.splice(i, 1);
                this.secondOnly.splice(j, 1);
            }
        }
    }
}


function areArraysEqual(array1, array2) {
    var temp = new Array();
    if ( (!array1[0]) || (!array2[0]) ) { // If either is not an array
        return false;
    }
    if (array1.length != array2.length) {
        return false;
    }
    // Put all the elements from array1 into a "tagged" array
    for (var i=0; i<array1.length; i++) {
        key = (typeof (array1[i])) + "~" + array1[i];
        // Use "typeof" so a number 1 isn't equal to a string "1".
        if (temp[key]) {
            temp[key]++;
        } else {
            temp[key] = 1;
        }
    // temp[key] = # of occurrences of the value (so an element could appear multiple times)
    }
    // Go through array2 - if same tag missing in "tagged" array, not equal
    for (var i=0; i<array2.length; i++) {
        key = (typeof array2[i]) + "~" + array2[i];
        if (temp[key]) {
            if (temp[key] == 0) {
                return false;
            } else {
                temp[key]--;
            }
        // Subtract to keep track of # of appearances in array2
        } else { // Key didn't appear in array1, arrays are not equal.
            return false;
        }
    }
    // If we get to this point, then every generated key in array1 showed up the exact same
    // number of times in array2, so the arrays are equal.
    return true;
}



function Map(data) {
    if(data == null) {
        data = {};
    }
    this.data = data;
}

Map.prototype.setData = function(data) {
    this.data = data;
}

Map.prototype.put = function(key, value) {
    this.data[key] = value;
    return value;
}

Map.prototype.containsKey = function(checkKey) {
    for(var key in this.data) {
        if(key == checkKey) {
            return true;
        }
    }
    return false;
}

Map.prototype.remove = function(key) {
   delete this.data[key];
}

Map.prototype.get = function(key) {
    return this.data[key];
}

Map.prototype.toJson = function() {
    return JSON.stringify(this.data);
}

Map.prototype.getAsJsObj = function() {
    return this.data;
}

Map.prototype.getData = function() {
    return this.data;
}

Map.prototype.getKeys = function() {
    var keys = [];
    for(var key in this.data) {
        keys.push(key);
    }
    return keys;
}

function RPCService(url) {
    this.endpoint = url;
}

RPCService.prototype.call = function(query) {
    var request = null;
    
    if(query.data) {
        request = $.extend({}, query.data);
    } else {
        request = {};
    }
    
    request.rpcAction = query.action;
    
    $.ajax({
        type: 'POST',
        url: this.endpoint,
        dataType: 'json',
        data: JSON.stringify(request),
        success: query.callback
    });
}

function bool(val) {
    return val == true;
}
function S4() {
    return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
}
function guid() {
    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
}