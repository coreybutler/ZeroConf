/**
 * ZeroConf plugin for Cordova/Phonegap
 *
 * Copyright (c) 2013 Vlad Stirbu <vlad.stirbu@ieee.org> Converted to Cordova 3.0 format
 * MIT license
 *
 * @author Matt Kane
 * Copyright (c) Triggertrap Ltd. 2012. All Rights Reserved.
 * Available under the terms of the MIT License.
 *
 */

/*global module, console, require*/
/*jshint -W097 */
//'use strict';
//var exec = require('cordova/exec');
//
//var ZeroConf = {
//  watch: function (type, callback) {
//    return exec(function (result) {
//      if (callback) {
//        callback(result);
//      }
//
//    }, ZeroConf.fail, "ZeroConf", "watch", [type]);
//  },
//  unwatch: function (type) {
//    return exec(null, ZeroConf.fail, "ZeroConf", "unwatch", [type]);
//  },
//  close: function () {
//    return exec(null, ZeroConf.fail, "ZeroConf", "close", []);
//  },
//  register: function (type, name, port, text) {
//    if (!type) {
//      console.error("'type' is a required field");
//      return;
//    }
//    return exec(null, ZeroConf.fail, "ZeroConf", "register", [type, name, port, text]);
//  },
//  unregister: function () {
//    return exec(null, ZeroConf.fail, "ZeroConf", "unregister", []);
//  },
//  fail: function (o) {
//    console.error("Error " + JSON.stringify(o));
//  },
//  get: function(type, callback){
//    return exec(function(result){
//      callback && callback(result);
//    }, ZeroConf.fail, "ZeroConf", "get", [type]);
//  }
//};

//module.exports = ZeroConf;

var exec = require('cordova/exec');

var MDNS = function(_type){

  Object.defineProperties(this,{
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
    listen: {
      enumerable: true,
      writable: false,
      configurable: false,
      value: function(type){
        type = type || null;
        var me = this;

        return exec(function(result) {
          var data = typeof result === 'object' ? result : {
            action: result,
          };
          alert('JAVA CALLED: '+data.action);
          if (type !== null){
            data.action === type && me.handleEvent(data);
            return;
          } else {
            me.handleEvent(data);
          }
        }, function(e){
          throw e;
        }, "MDNS", "monitor", []);
      }
    }
  });

};

module.exports = MDNS;

