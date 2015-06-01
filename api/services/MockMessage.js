module.exports = {
  createMockMessage: function(taskID, cb) {
    Task.findOne({id: taskID}).populate('assignedBy').exec(function (err, task) {
      UserStatus.findOne({user: task.assignedTo}).exec(function (err, status) {
        if (status.replyPending && (status.taskSent == taskID)) {
          cb(null, MockMessage.createReminderSentMessage(task));
        } else if (task.reminderIsDue(task.assignedTo)) {
          cb(null, MockMessage.createReplyPendingMessage(task));
        }
        cb();
      });
    });
  },

  createReplyPendingMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reply Pending',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }; 
    return message;
  },

  createReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder Sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }; 
    return message;
  },
}
