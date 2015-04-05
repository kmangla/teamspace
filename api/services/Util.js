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
}
