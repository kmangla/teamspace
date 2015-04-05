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

    timeFirstReminderSent: {
      type: 'datetime',
    },
    repeatReminderIsDue: function() {
      var date = new Date();
      var timeSinceLastReminderSec = Math.round((date-this.timeReminderSent)/1000);
      if (timeSinceLastReminderSec > 3 * 3600) {
        return true;
      } else {
        return false;
      }
    },
  },
}
