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
  logClientData: function(req, res) {
    console.log("Client Log: %s %s %s %s %s ", req.param('userID'),
      req.param('serverCode'), req.param('phone'),req.param('desc'),
     req.param('serverResponse'),req.param('url'));
    return res.send('Ok');
  },
};

