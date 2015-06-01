module.exports = {
  createMockMessage: function(taskID, cb) {
    Task.findOne({id: taskID}).populate('assignedBy').populate('assignedTo').exec(function (err, task) {
      UserStatus.findOne({user: task.assignedTo.id}).exec(function (err, status) {
        if (status.replyPending && (status.taskSent == taskID)) {
          cb(null, MockMessage.createReminderSentMessage(task, status.timeFirstReminderSent));
        } else if (task.reminderIsDue(task.assignedTo)) {
          var time = new Date(task.lastUpdate.getTime() + task.frequency * 1000);
          cb(null, MockMessage.createReplyPendingMessage(task, time));
        }
        cb();
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
