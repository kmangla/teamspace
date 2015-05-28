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

    repeatReminderIsDue: function() {
      var date = new Date();
      var timeSinceLastReminderSec = Math.round((date-this.timeReminderSent)/1000);
      var hoursTillNewReminder = 3;
      if (this.reminderCount > 3) {
        hoursTillNewReminder = 6;
      }
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
}
