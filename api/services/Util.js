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
    var m_date1 = moment(date1).startOf('day').tz(user.getTZ());
    var m_date2 = moment(date2).startOf('day').tz(user.getTZ());
    return Math.floor((m_date1 - m_date2) / 86400000);
  },

  getDateObject: function (date) {
    if (!date) {
      return new Date();
    }
    return new Date(date);
  },

  extractMap: function (objects, key) {
    var map = {};
    for (var i = 0; i < objects.length; i++) {
      map[objects[i][key]] = objects[i];
    }
    return map;
  },

  extractKey: function(map, key) {
    if (map.hasKey(key)) {
      return map[key];
    }
    return null;
  }
}
