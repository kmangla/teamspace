/**
 * LogController
 *
 * @description :: Server-side logic for logging data
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {


  logData: function(req, res) {
    Logging.logInfo('metrics', req.session.User.id, null, null, req.allParams())
    return res.send('Ok');
  },

  logClientData: function(req, res) {
    console.log("Client Log: %s %s %s %s %s ", req.param('userID'),
      req.param('serverCode'), req.param('phone'),req.param('desc'),
     req.param('serverResponse'),req.param('url'));
    return res.send('Ok');
  },
};

