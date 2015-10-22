module.exports = {
  receiveSMS: function (phone, message, cb) {
    User.findOne({phone: phone}).exec(function (err, user) {
      if (err) {
        cb(err);
        return;
      }
      if (!user) {
        Logging.logError('sms_receive', null, null, null, 'User does not exist for phone:' + phone);
        cb('User does not exist for phone ' + phone);
        return;
      }
      UserStatus.findOne({user: user.id}).populate('taskSent').exec(function (err, userStatus) {
        if (err) {
      	  cb(err);
          return;
        }
        if (!userStatus.taskSent) {
          Logging.logError('sms_receive', null, user.id, null, 'No task reminder sent');
          cb('No task reminder sent');
          return;
        }
        var messageObj = {};
      	messageObj.forTask = userStatus.taskSent;
        messageObj.sentBy = user;
        messageObj.description = message;
        Message.create(messageObj).exec(function (err, messageCreated) {
          if (err) {
            cb(err);
            return;
          }
          Logging.logInfo(
            'sms_receive',
            userStatus.taskSent.assignedBy,
            userStatus.taskSent.assignedTo,
            userStatus.taskSent.id,
            message
          );
          var userStatusObj = {};
          userStatusObj.replyPending = false;
          userStatusObj.timeMessageSent = new Date();
          userStatusObj.reminderCount = 0;
          UserStatus.update({user: user.id}, userStatusObj).exec(function (err, userStatusUpdate) {
            if (err) {
              cb(err);
              return;
            }
            var taskStatus = {}
            taskStatus.replyPending = false;
            taskStatus.reminderCount = 0;
            UserGlobalStatus.update({user: user.id}, {replyPending: false, timeLastReplyReceived: new Date()}, function (err, update) {});
            TaskStatus.update({id:userStatus.taskSent.currentStatus}, taskStatus, function (err, taskStatusUpdate) {
              cb(null, messageCreated);
            });
          });
        });
      });
    });
  }
}
