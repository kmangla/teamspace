module.exports = {
  run : function(user) {
    User.isContactable(user, function (isContactable) {
      if (!isContactable) {
        Logging.logInfo('schedule_reminders', user.manager.id, user.id, null, 'User is not contactable');
        return;
      }
      UserStatus.findOne({user: user.id}).populate('taskSent').exec(function (err, userStatus) {
        if (err) {
          return;
        }
        if (!userStatus) {
          Logging.logError('schedule_reminders', user.manager.id, user.id, null, 'No userStatus object');
          return;
        }
        UserStatus.shouldMoveToNextTask(userStatus.id, function (err, shouldMoveToNextTask) {
          if (err) {
            return;
          }
          if (userStatus.replyPending && userStatus.taskSent && userStatus.taskSent.status == 'open' && userStatus.taskSent.assignedTo == user.id && !shouldMoveToNextTask && !user.priorityTask) {
            Logging.logInfo('schedule_reminders', user.manager.id, user.id, userStatus.taskSent.id, 'Keep current task for reminders');
            PushToken.findOrAssignToken(user, function (err, token) {
              if (err) {
                return;
              }
              TaskStatus.findOne({id: userStatus.taskSent.currentStatus}, function (err, taskStatus) {
                Task.reminderMessageAndNotifications(userStatus.taskSent, function (err, message, notifications) {
                  if (userStatus.repeatReminderIsDue(user)) {
                    SMS.create({phone: user.phone, task: userStatus.taskSent.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
                 	    if (err) {
                        return;
                      }
                      TaskReminders.checkShouldSendEmployerReminder(user, userStatus.taskSent.id, function () {});
                      Logging.logInfo('schedule_reminders', user.manager.id, user.id, userStatus.taskSent.id, 'Reminder sent for task');
                      var statusUpdateObj = {}; 
                      statusUpdateObj.timeReminderSent = new Date();
                      statusUpdateObj.timeMessageSent = new Date();
                      statusUpdateObj.reminderCount = userStatus.reminderCount + 1;
                      UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function (err, userStatusUpdate) {});

                      var taskStatusUpdateObj = {}; 
                      taskStatusUpdateObj.timeReminderSent = new Date();
                      taskStatusUpdateObj.reminderCount = taskStatus.reminderCount + 1;
                      TaskStatus.update({id: taskStatus.id}, taskStatusUpdateObj).exec(function (err, taskStatusUpdate) {});
                      TaskReminders.createSMSForNotifications(user, userStatus, notifications, token);
                    });
                  } else if (notifications.length > 0) {
                    var statusUpdateObj = {}; 
                    statusUpdateObj.timeMessageSent = new Date();
                    UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function (err, userStatusUpdate) {});
                    TaskReminders.createSMSForNotifications(user, userStatus, notifications, token);
                  }
                });
              });
            });
          return;
        }
        Logging.logInfo('schedule_reminders', user.manager.id, user.id, null, 'Change current task for reminders');
        if (!user.priorityTask && !userStatus.canStartNewTaskThread()) {
          Logging.logInfo('schedule_reminders', user.manager.id, user.id, null, 'New thread cannot be started for now');
          return;
        }
        var tasksFind = {
          assignedTo: user.id,
          status: 'open'
        }
        if (user.priorityTask) {
          tasksFind['id'] = user.priorityTask;
        }
        Task.find(tasksFind).populate('assignedTo').populate('currentStatus').exec(function(err, tasks) {
          if (!tasks) {
            User.update({id: user.id}, {priorityTask: ''}).exec(function(err, userUpate) {});
            return;
          }
          var dueTasks = [];
          for (var i = 0 ; i < tasks.length; i++) {
            if (!tasks[i].forceReminder && (Util.daysSince(new Date(), tasks[i].currentStatus.timeReminderSent, tasks[i].assignedTo) <= 1)) {
              continue;
            }
            if (tasks[i].reminderIsDue(tasks[i].assignedTo)) {
              dueTasks.push(tasks[i]);
            }
          }
          if (dueTasks.length == 0) {
            return;
          }
          var task = TaskReminders.findMax(dueTasks);
          Logging.logInfo('schedule_reminders', user.manager.id, user.id, task.id, 'New task selected for sending reminders');
          PushToken.findOrAssignToken(user, function (err, token) {
            if (err) {
              return;
            }
            Task.reminderMessageAndNotifications(task, function (err, message, notifications) {
              SMS.create({phone: user.phone, task: task.id, timeQueued: new Date(), tokenID: token, message: message}, function (err, reminder) {
                TaskReminders.checkShouldSendEmployerReminder(user, task.id, function () {});
                Logging.logInfo('schedule_reminders', user.manager.id, user.id, task.id, 'Reminder sent for task');
   	            if (err) {
                  return;
                } 
                var statusUpdateObj = {}; 
                statusUpdateObj.timeReminderSent = new Date();
                statusUpdateObj.timeMessageSent = new Date();
                statusUpdateObj.timeFirstReminderSent = new Date();
                statusUpdateObj.replyPending = true;
                statusUpdateObj.taskSent = task.id;
                statusUpdateObj.reminderCount = 1;
                UserStatus.update({id: userStatus.id}, statusUpdateObj).exec(function(err, userStatusUpdate) {});

                var taskStatusUpdateObj = {};
                if (!task.currentStatus.replyPending) {
                  taskStatusUpdateObj.timeReminderSent = new Date();
                  taskStatusUpdateObj.timeFirstReminderSent = new Date();
                  taskStatusUpdateObj.replyPending = true;
                  taskStatusUpdateObj.reminderCount = 1;
                } else {
                  taskStatusUpdateObj.timeReminderSent = new Date();
                  taskStatusUpdateObj.reminderCount = task.currentStatus.reminderCount + 1;
                }
                TaskStatus.update({id: task.currentStatus.id}, taskStatusUpdateObj).exec(function (err, taskStatusUpdate) {});
                User.update({id: user.id}, {priorityTask: ''}).exec(function(err, userUpate) {});
                TaskReminders.updateUserGlobalStatus(user);
                TaskReminders.createSMSForNotifications(user, userStatus, notifications, token);
              });
            });
          }); 
        });
        });
      });
    });
  },

  updateUserGlobalStatus: function (user) {
    UserGlobalStatus.findOne({user: user.id}).exec(function (err, userGlobalStatus) {
      if (!userGlobalStatus.replyPending) {
        var updateObj = {
          replyPending: true,
          timeFirstReminderSent: new Date()
        };
        UserGlobalStatus.update({id:userGlobalStatus.id}, updateObj, function (err, userStatusUpdated) {});
      }
    });
  },

  checkShouldSendEmployerReminder: function(user, taskID, cb) {
    UserGlobalStatus.findOne({user: user.id}).exec(function (err, status) {
      if (status.shouldSendReminderFromEmployer(user)) {
        UserGlobalStatus.sendReminderFromEmployer(user.manager, user, taskID, cb);
      }
    });
  },

  createSMSForNotifications: function (user, userStatus, notifications, token) {
    for(var j = 0; j < notifications.length; j++) {
      SMS.create({
        phone: user.phone,
        task: userStatus.taskSent.id,
        forMessage: notifications[j].forMessage,
        timeQueued: new Date(), tokenID: token, message: notifications[j].message
      }, function (err, reminder) {});
    }
  },

  findMax: function (tasks) {
    var maxTask = tasks[0];
    for (var i = 0 ; i < tasks.length; i++) {
      if (tasks[i].shouldGoBefore(maxTask)) {
        maxTask = tasks[i];
      }
    }
    return maxTask;
  }
}
