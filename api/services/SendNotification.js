module.exports = {
  sendNotification: function(receiverID, senderID, text, taskID, ntype, cb) {
    User.findOne({id: senderID}).exec(function (err, senderUser) { 
      if (err) {
        cb(err);
        return;
      }
      PushToken.find({userID: receiverID}).exec(function (err, tokens) {
        if (err) {
          cb(err);
          return;
        }
        if (!tokens.length) {
          Logging.logError('notification', receiverID, senderID, taskID, 'Token does not exist for user');
          cb('Token does not exist for user');
          return;
        }
        var gcm = require('node-gcm');
        var notificationData = {
          text: text,
          taskID: taskID,
          user: senderUser,
          ntype: ntype,
        };
        var message = new gcm.Message({
          data: {'push' : notificationData},
        });
   
        var sender = new gcm.Sender('AIzaSyDzmF3qC-gXkmOkBiIsAxv5x53l9h1Bzj8');
        var registrationIds = [];
        for (var i = 0; i < tokens.length; i++) {
          registrationIds[i] = tokens[i].regID;
        }
        
        Logging.logInfo('notification', receiverID, senderID, taskID, 'Notification sent for: ' + text);
        sender.send(message, registrationIds, function (err, result) {
          if (err) {
            cb(err);
          } else {
            cb();
          }
        });
      });
    });
  }
}
