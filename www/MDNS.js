var exec = require('cordova/exec');

var MDNS = function(type){

  type = type || "_http._tcp.";

  Object.defineProperties(this,{
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
          alert('JAVA CALLED: '+data.action);

          if (data.action === 'available'){
            me._services[data.service.type] = me._services[data.service.type] || {};
            var key = data.service.uuid || data.service.qualifiedname,
                obj = data.service;
            obj.uuid && delete obj.uuid;
            me._services[data.service.type][key] = obj;
          }
          if (data.action === 'unavailable'){
            var key = data.service.uuid || data.service.qualifiedname;
            me.services[data.service.type][key] && delete me.services[data.service.type][key];
          }

          callback && callback(data);
        }, function(e){
          throw e;
        }, "MDNS", "monitor", [type]);
      }
    }
  });

};

module.exports = MDNS;

