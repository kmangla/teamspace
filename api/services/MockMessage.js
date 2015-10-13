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
        for (var i = 0; i < tasks.length; i++) {
          var task = tasks[i];
          if (task.reminderIsDue(task.assignedTo)) {
            if (task.currentStatus.replyPending) {
              if (statusMap[task.assignedTo.id] && (statusMap[task.assignedTo.id].taskSent == task.id) && (task.taskPriority == 100)) {
                taskIDstoMessage[task.id] =  MockMessage.createUrgentReminderSentMessage(task);
              } else {
                taskIDstoMessage[task.id] =  MockMessage.createReminderSentMessage(task);
              }
            } else {
              taskIDstoMessage[task.id] = MockMessage.createReplyPendingMessage(task, task.getUpdateDueSince());
            }
          }
        }
        cb(null, taskIDstoMessage);
      });
    });
  },

  createReplyPendingMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder scheduled to be sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(task.getUpdateDueSince()),
      updatedAt: new Date(task.getUpdateDueSince())
    }; 
    return message;
  },

  createReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: task.currentStatus.reminderCount + ' Reminders sent',
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
      description: 'No replies received. Contact employee for update',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },
}
