module.exports = {
  run : function(user) {
    User.isContactable(user, function (isContactable) {
      if (!isContactable) {
        return;
      }
      UserStatus.findOne({user: user.id}).populate('taskSent').exec(function (err, userStatus) {
        if (err) {
          console.log(err);
          return;
        }
        if (!userStatus) {
          console.log('No userStatus object for user ' + user.id);
          return;
        }
        if (userStatus.replyPending && userStatus.taskSent && userStatus.taskSent.status == 'open' && userStatus.taskSend.assignedTo == user.id) {
          if (userStatus.repeatReminderIsDue()) {
            console.log('send repeat reminder');
            PushToken.findOrAssignToken(user, function (err, token) {
              if (err) {
                console.log(err);
                return;
              }
              Task.reminderMessage(userStatus.taskSent, function (err, message) {
                userStatus.taskSent.forceReminder = false;
                userStatus.taskSent.save(function (err, task) {
                  console.log(err);
                });
                Reminder.create({phone: user.phone, task: userStatus.taskSent.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
         	  if (err) {
                    console.log(err);
                    return;
                  } 
                  console.log(reminder);
                  var statusUpdateObj = {}; 
                  statusUpdateObj.timeReminderSent = new Date();
                  UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function (err, userStatusUpdate) {
                  });
                });
              });
            });
          } else {
            console.log('do not send repeat reminder');
          }
          return;
        }
        Task.find({assignedTo: user.id, status: 'open'}).exec(function(err, tasks) {
          for (var i = 0 ; i < tasks.length; i++) {
            if (tasks[i].reminderIsDue()) {
              console.log(tasks[i] + 'send reminder');
              var task = tasks[i];
              PushToken.findOrAssignToken(user, function (err, token) {
                if (err) {
                  console.log(err);
                  return;
                }
                Task.reminderMessage(task, function (err, message) {
                  task.forceReminder = false;
                  task.save(function (err, taskSaved) {
                    console.log(err);
                  });
                  Reminder.create({phone: user.phone, task: task.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
      	            if (err) {
                      console.log(err);
                      return;
                    } 
                    console.log(reminder);
                    var statusUpdateObj = {}; 
                    statusUpdateObj.timeReminderSent = new Date();
                    statusUpdateObj.timeFirstReminderSent = new Date();
                    statusUpdateObj.replyPending = true;
                    statusUpdateObj.taskSent = task.id;
                    UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function(err, userStatusUpdate) {
                    });
                  });
                });
              });
            } else {
              console.log(tasks[i] + 'reminder not due');
            }
          } 
        });
      });
    });
  }
}
