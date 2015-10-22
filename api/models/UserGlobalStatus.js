/**
* UserGlobalStatus.js
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
    
    timeFirstReminderSent: {
      type: 'datetime',
    },

    timeLastReplyReceived: {
      type: 'datetime',
    },

    timeEmployeeSMSSent: {
      type: 'datetime',
    },

    replyPending: {
      type: 'boolean',
    }
  }
}
