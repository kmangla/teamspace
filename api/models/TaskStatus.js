/**
* TaskStatus.js
*/

module.exports = {

  schema: true,

  attributes: {
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

    reminderCount: {
      type: 'integer',
      defaultsTo: 0
    },
  }
}
