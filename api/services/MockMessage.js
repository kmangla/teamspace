module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      var userStatusesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        userStatusesToFetch[tasks[i].assignedTo.id] = 1;
      }
      UserStatus.find().where({user: Object.keys(userStatusesToFetch)}).exec(function (err, statuses) {
        var statusMap = Util.extractMap(statuses, "user");
        var userMapList = Util.extractMapList(tasks, "assignedTo", "id");
        var userMaxTaskMap = {};
        for (var userID  in userMapList) {
          userMaxTaskMap[userID] = TaskReminders.findMax(userMapList[userID]);
        }
        for (var i = 0; i < tasks.length; i++) {
          var task = tasks[i];
          if (task.reminderIsDue(task.assignedTo)) {
            if (task.currentStatus.replyPending) {
              if (statusMap[task.assignedTo.id] && (statusMap[task.assignedTo.id].taskSent == task.id) && (task.taskPriority == 100)) {
                taskIDstoMessage[task.id] =  MockMessage.createUrgentReminderSentMessage(task);
              } else {
                if (statusMap[task.assignedTo.id] && (statusMap[task.assignedTo.id].taskSent == task.id)) {
                  taskIDstoMessage[task.id] =  MockMessage.createCurrentReminderSentMessage(task);
                } else {
                  taskIDstoMessage[task.id] =  MockMessage.createPreviousReminderSentMessage(task);
                }
              }
            } else {
              if (userMaxTaskMap[task.assignedTo.id].id == task.id) {
                taskIDstoMessage[task.id] = MockMessage.createReplyPendingNextMessage(task, task.getUpdateDueSince());
              } else {
                taskIDstoMessage[task.id] = MockMessage.createReplyPendingMessage(task, task.getUpdateDueSince());
              }
            }
          }
        }
        cb(null, taskIDstoMessage);
      });
    });
  },

  createReplyPendingNextMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders scheduled to be sent next',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(task.getUpdateDueSince()),
      updatedAt: new Date(task.getUpdateDueSince())
    }; 
    return message;
  },

  createReplyPendingMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders scheduled to be sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(task.getUpdateDueSince()),
      updatedAt: new Date(task.getUpdateDueSince())
    }; 
    return message;
  },

  createCurrentReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders being sent currently',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },

  createPreviousReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders already sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },

  createUrgentReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'No updates received. Contact employee',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },

  createReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    };
    return message;
  },
}
