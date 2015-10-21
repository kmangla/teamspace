/**
 *  NotificationController
 *
 * @description :: Server-side logic for logging data
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {
  sendNotification: function(req, res) {
    SendNotification.sendNotification(req.session.User.id, req.param('senderID'), req.param('text'), req,param('ntype'), function () {});
    return res.send('Ok');
  }
};

