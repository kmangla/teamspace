module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      var userStatusesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        userStatusesToFetch[task.assignedTo.id] = 1;
      }
      UserStatus.find().where({user: Object.keys(userStatusesToFetch)}).exec(function (err, statuses) {
        var statusMap = Util.extractMap(statuses, "user");
        var userMapList = Util.extractMapList(tasks, "assignedTo", "id");
        for (var i = 0; i < tasks.length; i++) {
          var task = tasks[i];
          var userID = task.assignedTo.id;
          if (task.assignedBy.notApproved) {
            taskIDstoMessage[task.id] = MockMessage.createFixProperTask(task);
            continue;
          }
          if (task.reminderIsDue(task.assignedTo)) {
            if (statusMap[userID].replyPending && statusMap[userID].taskSent == tasks[i].id) {
              taskIDstoMessage[task.id] = MockMessage.createReminderCurrentlySentMessage(task);
            } else {
              if (task.currentStatus.replyPending) {
                taskIDstoMessage[task.id] = MockMessage.createRepeatReminderWillBeSent(task);
              } else {
                taskIDstoMessage[task.id] = MockMessage.createReminderWillBeSent(task);
              }
            }
          } else {
            taskIDstoMessage[task.id] = MockMessage.createUpdated(task);
          }
        }
        cb(null, taskIDstoMessage);
      });
    });
  },

  createFixProperTask: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Task description is incomplete. Please fix',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }; 
    return message;
  },

  createUpdated: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Task is up to date',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.lastUpdate,
      updatedAt: task.lastUpdate
    }; 
    return message;
  },

  createRepeatReminderWillBeSent: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders sent earlier. Total reminders ' + task.currentStatus.reminderCount,
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },

  createReminderWillBeSent: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder due. Reminder will be sent soon',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }; 
    return message;
  },

  createReminderCurrentlySentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminders sent today. Total reminders ' + task.currentStatus.reminderCount,
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
