module.exports = {
  run : function() {
    PushToken.find({appID: '1'}, function (err, tokens) {
      for (var j = 0; j < tokens.length; j++) {
        SMS.find({where: {tokenID: tokens[j].id}, sort: 'timeQueued ASC'}).populate('tokenID').exec(function (err, reminders) {
          if (err) {
            return;
          }
          var messages = [];
          var limit = 10;
          if (reminders.length < limit) {
            limit = reminders.length;
          }
          for (var i = 0; i < limit; i++) {
            var reminder = reminders[i];
            if (!reminder.tokenID) {
              Logging.logError('reminder', null, null, reminder.task, 'No token exists for the user');
              continue;
            }
            messages[i] = {
              phone: reminders[i].phone,
              message: reminders[i].message,
            };
            SMS.destroy({id: reminder.id}, function (err) {});
            Message.update({id: reminder.forMessage}, {notifSent: true}, function (err, message) {});
            Logging.logInfo('reminder', null, null, reminder.task, 'Reminder message sent for task:' + reminders[i].message);
          }
          if (messages.length) {
            SendGCMMessage.sendGCMMessage(reminders[0].tokenID, messages, function (err) {
              if (err) {
                return;
              }
            });
          }
        }); 
      }
    });
  }
}
