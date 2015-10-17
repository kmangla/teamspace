module.exports = {
  populateParamToExpand: function (req) {
    var toPopulateParam = req.param('toPopulate');
    var toPopulate =  toPopulateParam ? toPopulateParam.split(',') : ['assignedTo'];
    return toPopulate;
  },

  populateInQuery: function (query, toPopulate) {
    for (var i = 0; i < toPopulate.length; i++) {
      query.populate(toPopulate[i]);
    }
  },

  daysSince: function (date1, date2, user) {
    var moment = require('moment-timezone');
    var m_date1 = moment(date1).tz(user.getTZ()).startOf('day');
    var m_date2 = moment(date2).tz(user.getTZ()).startOf('day');
    return Math.floor((m_date1 - m_date2) / 86400000);
  },

  getDateObject: function (date) {
    if (!date) {
      return new Date();
    }
    return new Date(date);
  },

  extractMapList: function (objects, key, subkey) {
    var mapList = {};
    for (var i = 0; i < objects.length; i++) {
      if (!objects[i][key]) {
        continue;
      }
      mapList[objects[i][key][subkey]] = [];
    }
    for (var i = 0; i < objects.length; i++) {
      if (!objects[i][key]) {
        continue;
      }
      mapList[objects[i][key][subkey]].push(objects[i]);
    }
    return mapList;
  },

  extractMap: function (objects, key) {
    var map = {};
    for (var i = 0; i < objects.length; i++) {
      map[objects[i][key]] = objects[i];
    }
    return map;
  },

  extractKey: function(map, key) {
    if (key in map) {
      return map[key];
    }
    return null;
  }
}
