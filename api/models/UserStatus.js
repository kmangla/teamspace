/**
* UserStatus.js
*
*/

module.exports = {

  schema: true,

  attributes: {
    user: {
      model: 'user',
      required: true,
      unique: true
    },
    
    taskSent: {
      model: 'task',
    },
    
    replyPending: {
      type: 'boolean',
      defaultsTo: false
    },
 
    timeReminderSent: {
      type: 'datetime',
    },

    timeMessageSent: {
      type: 'datetime',
    },

    timeFirstReminderSent: {
      type: 'datetime',
    },

    reminderCount: {
      type: 'integer',
      defaultsTo: 0
    },

    repeatReminderIsDue: function(user) {
      var date = new Date();
      var timeSinceLastReminderSec = Math.round((date-this.timeReminderSent)/1000);
      var hoursTillNewReminder = user.repeatMessageDelay;
      if (timeSinceLastReminderSec > hoursTillNewReminder * 3600) {
        return true;
      } else {
        return false;
      }
    },

    canStartNewTaskThread: function() {
      var date = new Date();
      if (!this.timeMessageSent) {
        return true;
      }
      var timeSinceLastMessageSec = Math.round((date-this.timeMessageSent)/1000);
      if (timeSinceLastMessageSec > 60 * 30) {
        return true;
      } else {
        return false;
      }
    },
  },

  shouldMoveToNextTask: function(userStatusID, cb) {
    UserStatus.findOne({id: userStatusID}).populate('taskSent').exec(function (err, status) {
      if (err) {
        cb(err);
        return;
      }
      if (!status) {
        cb('Status object does not exist');
        return;
      }
      if (!status.taskSent) {
        cb(null, false);
        return;
      }
      User.findOne({id: status.taskSent.assignedTo}, function (err, user) {
        var days = Util.daysSince(new Date(), status.timeFirstReminderSent, user);
        if (days > 0) {
          cb(null, true);
          return; 
        }
        cb(null, false);
      });
    });
  },

  changeStatusIfRequired: function (taskID, cb) {
    Task.findOne({id: taskID}).exec(function (err, task) {
      if (err) {
        cb(err);
        return;
      }
      UserStatus.findOne({user: task.assignedTo}).exec(function (err, status) {
        if (err) {
          cb(err);
          return;
        }
        if ((status.taskSent == taskID) && status.replyPending) {
          var userStatusObj = {};
          userStatusObj.replyPending = false;
          userStatusObj.timeMessageSent = new Date();
          userStatusObj.reminderCount = 0;
          UserStatus.update({user: task.assignedTo}, userStatusObj).exec(function (err, userStatusUpdate) {
            if (err) {
              cb(err);
              return;
            }
            cb();
          });
        }       
      });
    });
  }
}
