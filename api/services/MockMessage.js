module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      var userStatusesToFetch = {};
      var dueTasks = [];
      for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        userStatusesToFetch[task.assignedTo.id] = 1;
        if (task.reminderIsDue(task.assignedTo)) {
          dueTasks.push(task);
        }
      }
      UserStatus.find().where({user: Object.keys(userStatusesToFetch)}).exec(function (err, statuses) {
        var statusMap = Util.extractMap(statuses, "user");
        var userMapList = Util.extractMapList(dueTasks, "assignedTo", "id");
        for (var i = 0; i < dueTasks.length; i++) {
          var task = dueTasks[i];
          var userID = task.assignedTo.id;
          if (statusMap[userID].replyPending && statusMap[userID].taskSent == dueTasks[i].id) {
            taskIDstoMessage[task.id] = MockMessage.createReminderCurrentlySentMessage(task);
          } else {
            if (task.currentStatus.replyPending) {
              taskIDstoMessage[task.id] = MockMessage.createRepeatReminderWillBeSent(task);
            } else {
              taskIDstoMessage[task.id] = MockMessage.createReminderWillBeSent(task);
            }
          }
        }
        cb(null, taskIDstoMessage);
      });
    });
  },

  createRepeatReminderWillBeSent: function (task, offset) {
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

  createReminderWillBeSent: function (task, offset) {
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
