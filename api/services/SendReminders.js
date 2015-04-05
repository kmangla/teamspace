module.exports = {
  run : function() {
    PushToken.find({appID: '1'}, function (err, tokens) {
      for (var j = 0; j < tokens.length; j++) {
        Reminder.find({where: {tokenID: tokens[j].id}, sort: 'timeQueued ASC'}).populate('task').populate('tokenID').exec(function (err, reminders) {
          if (err) {
            console.log(err);
            return;
          }
          var messages = [];
          var limit = 10;
          if (reminders.length < limit) {
            limit = reminders.length;
          }
          console.log('Reminders fetching ' + reminders.length);
          for (var i = 0; i < limit; i++) {
            var reminder = reminders[i];
            if (!reminder.tokenID) {
              continue;
            }
            messages[i] = {
              phone: reminders[i].phone,
              message: reminders[i].message,
            };
            Reminder.destroy({id: reminder.id}, function (err) {
            });
          }
          if (messages.length) {
            SendGCMMessage.sendGCMMessage(reminders[0].tokenID, messages, function (err) {
              if (err) {
                console.log('here' + err);
              }
            });
          }
        }); 
      }
    });
  }
}
