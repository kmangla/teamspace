module.exports = {
  createMockMessage: function(taskID, cb) {
    Task.findOne({id: taskID}).populate('assignedBy').populate('assignedTo').exec(function (err, task) {
      UserStatus.findOne({user: task.assignedTo.id}).exec(function (err, status) {
        if (status.replyPending && (status.taskSent == taskID)) {
          cb(null, MockMessage.createReminderSentMessage(task, status.timeReminderSent));
        } else if (task.reminderIsDue(task.assignedTo)) {
          var time = new Date(task.getUpdateDueSince());
          if (time < task.lastReminderTime) {
            // Reply is pending and a reminder has been sent
            cb(null, MockMessage.createReminderSentMessage(task, task.lastReminderTime));
          } else {
            // Reply is pending and no reminders have been sent
            var currentTime = new Date();
            if (currentTime < time) {
              time = currentTime;
            }
            cb(null, MockMessage.createReplyPendingMessage(task, time));
          }
        } else {
          cb();
        }
      });
    });
  },

  createReplyPendingMessage: function (task, time) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reply Pending',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: time,
      updatedAt: time
    }; 
    return message;
  },

  createReminderSentMessage: function (task, time) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder Sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: time,
      updatedAt: time
    }; 
    return message;
  },
}
