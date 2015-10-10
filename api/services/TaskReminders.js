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
        UserStatus.shouldMoveToNextTask(userStatus.id, function (err, shouldMoveToNextTask) {
        if (err) {
          console.log(err);
          return;
        }
        if (userStatus.replyPending && userStatus.taskSent && userStatus.taskSent.status == 'open' && userStatus.taskSent.assignedTo == user.id && !shouldMoveToNextTask && !user.priorityTask) {
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
                    Task.update({id: userStatus.taskSent.id}, {lastReminderTime: new Date()}).exec(function () {});
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
        if (!user.priorityTask && !userStatus.canStartNewTaskThread()) {
          return;
        }
        var tasksFind = {
          assignedTo: user.id,
          status: 'open'
        }
        if (user.priorityTask) {
          tasksFind['id'] = user.priorityTask;
        }
        Task.find(tasksFind).populate('assignedTo').exec(function(err, tasks) {
          if (!tasks) {
            User.update({id: user.id}, {priorityTask: ''}).exec(function(err, userUpate) {
            });
            return;
          }
          var dueTasks = [];
          for (var i = 0 ; i < tasks.length; i++) {
            if (tasks[i].reminderIsDue(tasks[i].assignedTo)) {
              dueTasks.push(tasks[i]);
            }
          }
          if (dueTasks.length == 0) {
            return;
          }
          TaskReminders.findMax(dueTasks, function (err, task) {
            console.log(task + 'send reminder');
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
                  UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function(err, userStatusUpdate) {});
                  User.update({id: user.id}, {priorityTask: ''}).exec(function(err, userUpate) {});
                  Task.update({id: task.id}, {lastReminderTime: new Date()}).exec(function () {});
                  for(var j = 0; j < notifications.length; j++) {
                    SMS.create({phone: user.phone, task: userStatus.taskSent.id, forMessage: notifications[i].forMessage, timeQueued: new Date(), tokenID: token, message: notifications[j].message}, function (err, reminder) {});
                  }
                });
              });
            }); 
          });
        });
        });
      });
    });
  },

  findMax: function (tasks, cb) {
    var maxTask = tasks[0];
    for (var i = 0 ; i < tasks.length; i++) {
      if (tasks[i].shouldGoBefore(maxTask)) {
        maxTask = tasks[i];
      }
    }
    cb(null, maxTask);
  }
}
