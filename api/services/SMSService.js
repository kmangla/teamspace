module.exports = {
  receiveSMS: function (phone, message, cb) {
    User.findOne({phone: phone}).exec(function (err, user) {
      if (err) {
        cb(err);
        return;
      }
      if (!user) {
        cb('User does not exist for phone ' + phone);
        return;
      }
      UserStatus.findOne({user: user.id}).populate('taskSent').exec(function (err, userStatus) {
        if (err) {
	  cb(err);
          return;
        }
        if (!userStatus.taskSent) {
          cb('No task reminder sent');
          return;
        }
        var messageObj = {};
	messageObj.forTask = userStatus.taskSent;
        messageObj.sentBy = user;
        messageObj.description = message;
        Message.create(messageObj).exec(function (err, message) {
          if (err) {
            cb(err);
            return;
          }
          var userStatusObj = {};
          userStatusObj.replyPending = false;
          UserStatus.update({user: user.id}, userStatusObj).exec(function (err, userStatusUpdate) {
            if (err) {
              cb(err);
              return;
            }
            console.log(userStatusUpdate);
          });
        });
      });
    });
  }
}
