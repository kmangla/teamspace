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
    console.log(logData);
    return res.send('Ok');
  },
};

