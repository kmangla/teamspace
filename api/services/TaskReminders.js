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
        if (userStatus.replyPending && userStatus.taskSent && userStatus.taskSent.status == 'open' && userStatus.taskSent.assignedTo == user.id && !userStatus.shouldMoveToNextTask()) {
            PushToken.findOrAssignToken(user, function (err, token) {
              if (err) {
                console.log(err);
                return;
              }
              Task.reminderMessageAndNotifications(userStatus.taskSent, function (err, message, notifications) {
                if (userStatus.repeatReminderIsDue()) {
                  console.log('send repeat reminder');
                SMS.create({phone: user.phone, task: userStatus.taskSent.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
         	  if (err) {
                    console.log(err);
                    return;
                  } 
                  console.log(reminder);
                  var statusUpdateObj = {}; 
                  statusUpdateObj.timeReminderSent = new Date();
                  statusUpdateObj.timeMessageSent = new Date();
                  statusUpdateObj.reminderCount = userStatus.reminderCount + 1;
                  UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function (err, userStatusUpdate) {});
                  for(var i = 0; i < notifications.length; i++) {
                    SMS.create({phone: user.phone, task: userStatus.taskSent.id, forMessage: notifications[i].forMessage, timeQueued: new Date(), tokenID: token, message: notifications[i].message}, function (err, reminder) {
                    });
                  }
                });
                } else if (notifications.length > 0) {
                  var statusUpdateObj = {}; 
                  statusUpdateObj.timeMessageSent = new Date();
                  UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function (err, userStatusUpdate) {});
                  for(var i = 0; i < notifications.length; i++) {
                    SMS.create({phone: user.phone, task: userStatus.taskSent.id, timeQueued: new Date(), forMessage: notifications[i].forMessage, tokenID: token, message: notifications[i].message}, function (err, reminder) {
                    });
                  }
                }
              });
            });
          return;
        }
        if (!userStatus.canStartNewTaskThread()) {
          return;
        }
        Task.find({assignedTo: user.id, status: 'open'}).populate('assignedTo').exec(function(err, tasks) {
          for (var i = 0 ; i < tasks.length; i++) {
            if (tasks[i].reminderIsDue(tasks[i].assignedTo)) {
              console.log(tasks[i] + 'send reminder');
              var task = tasks[i];
              PushToken.findOrAssignToken(user, function (err, token) {
                if (err) {
                  console.log(err);
                  return;
                }
                Task.reminderMessageAndNotifications(task, function (err, message, notifications) {
                  SMS.create({phone: user.phone, task: task.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
      	            if (err) {
                      console.log(err);
                      return;
                    } 
                    console.log(reminder);
                    var statusUpdateObj = {}; 
                    statusUpdateObj.timeReminderSent = new Date();
                    statusUpdateObj.timeMessageSent = new Date();
                    statusUpdateObj.timeFirstReminderSent = new Date();
                    statusUpdateObj.replyPending = true;
                    statusUpdateObj.taskSent = task.id;
                    statusUpdateObj.reminderCount = 1;
                    UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function(err, userStatusUpdate) {
                    });
                    for(var j = 0; j < notifications.length; j++) {
                      SMS.create({phone: user.phone, task: userStatus.taskSent.id, forMessage: notifications[i].forMessage, timeQueued: new Date(), tokenID: token, message: notifications[j].message}, function (err, reminder) {
                    });
                  }
                  });
                });
              });
              break;
            } else {
              console.log(tasks[i] + 'reminder not due');
            }
          } 
        });
      });
    });
  }
}
