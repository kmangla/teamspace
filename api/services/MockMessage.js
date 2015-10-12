module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    console.log(taskIDs);
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      console.log(tasks);
      var userStatusesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        userStatusesToFetch[tasks[i].assignedTo.id] = 1;
      }
      console.log(Object.keys(userStatusesToFetch));
      console.log(userStatusesToFetch);
      UserStatus.find().where({user: Object.keys(userStatusesToFetch)}, function (err, statuses) {
        if (err) {
          console.log(err);
          cb(err, {});
        }
        console.log(statuses);
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
