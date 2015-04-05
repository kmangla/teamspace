/**
 * LogController
 *
 * @description :: Server-side logic for logging data
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {


  logData: function(req, res) {
    var logObj = {
      userID: req.session.User.id,
      logData: req.param('log'), 
    };
    Log.create(logObj, function (err, log) {
      if (err) {
        return res.send(400);
      }
      return res.send('Ok');
    });
  },
};

