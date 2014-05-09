var exec = require('cordova/exec');

var MDNS = function(_type){

  Object.defineProperties(this,{
    type: {
      enumerable: true,
      get: function(){
        return _type || "_http._tcp.";
      }
    },
    _services: {
      enumerable: false,
      writable: true,
      configurable: false,
      value: {}
    },
    services: {
      enumerable: true,
      get: function(){
        return this._services;
      }
    },
    handlers:{
      enumerable: false,
      writable: true,
      configurable: true,
      value: {}
    },
    oncehandlers:{
      enumerable: false,
      writable: true,
      configurable: true,
      value: {}
    },
    on: {
      enumerable: true,
      writable: false,
      configurable: false,
      value: function(eventName,fn){
        this.handlers[eventName] = this.handlers[eventName] || [];
        this.handlers[eventName].push(fn);
      }
    },
    once: {
      enumerable: true,
      writable: false,
      configurable: false,
      value: function(eventName,fn){
        this.oncehandlers[eventName] = this.oncehandlers[eventName] || [];
        this.oncehandlers[eventName].push(fn);
      }
    },
    handleEvent: {
      enumerable: false,
      writable: false,
      configurable: false,
      value: function(data){
        (this.handlers[data.action]||[]).forEach(function(handler){
          alert(data);
          handler.apply(me,[data]);
        });
        (this.oncehandlers[data.action]||[]).forEach(function(handler){
          handler.apply(me,[data]);
        });
        this.oncehandlers.hasOwnProperty(data.action) && delete this.oncehandlers[data.action];
      }
    },
    listening: {
      enumerable: false,
      writable: true,
      configurable: false,
      value: false
    },
    macaddress: {
      enumerable: false,
      writable: true,
      configurable: false,
      value: null
    },
    mac: {
      enumerable: true,
      get: function(){
        var me = this;
        if(this.macaddress === null){
          return exec(function(data){
            var data = typeof result === 'object' ? result : {
              action: result,
            };

            if (data.action === 'mac'){
              me.macaddress = data.address;
              return data.address;
            }
          }, function(e){
            throw e;
          }, "MDNS", "monitor", [me.type]);
        }
        return this.macaddress;
      }
    },
    listen: {
      enumerable: true,
      writable: false,
      configurable: false,
      value: function(callback){
        if (this.listening) {
          return;
        }

        var me = this;

        this.listening = true;

        return exec(function(result) {
          var data = typeof result === 'object' ? result : {
            action: result,
          };

          if (data.action === 'available'){
            me._services[data.service.type] = me._services[data.service.type] || {};
            var key = data.service.md5 || data.service.qualifiedname,
                obj = data.service;
            obj.md5 && delete obj.md5;
            me._services[data.service.type][key] = obj;
          }
          if (data.action === 'unavailable'){
            var key = data.service.md5 || data.service.qualifiedname;
            me.services[data.service.type][key] && delete me.services[data.service.type][key];
          }

          callback && callback(data);
        }, function(e){
          throw e;
        }, "MDNS", "monitor", [me.type]);
      }
    }
  });

  var x = this.mac;

};

module.exports = MDNS;

