module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      var userStatusesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        userStatusesToFetch[tasks[i].assignedTo] = 1;
      }
      UserStatus.find().where({id: Object.keys(userStatusesToFetch)}, function (err, statuses) {
        var statusMap = Util.extractMap(statuses, "user");
        for (var i = 0; i < tasks.length; i++) {
          var task = tasks[i];
          if (task.reminderIsDue(task.assignedTo)) {
            if (task.currentStatus.replyPending) {
              if ((statusMap[task.assignedTo].taskSent == task.id) && (task.taskPriority == 100)) {
                taskIDstoMessage[task.id] =  createUrgentReminderSentMessage(task);
              } else {
                taskIDstoMessage[task.id] =  createReminderSentMessage(task);
              }
            } else {
              taskIDstoMessage[task.id] = createReplyPendingMessage(task, task.getUpdateDueSince());
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
      description: 'Reminder Scheduled To Be Sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.getUpdateDueSince(),
      updatedAt: task.getUpdateDueSince()
    }; 
    return message;
  },

  createReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: task.currentStatus.reminderCount + ' Reminders Sent',
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
      description: 'No Replies Received. Contact Employee for Update',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },
}
