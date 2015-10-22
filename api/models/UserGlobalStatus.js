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
    },

    // Custom attributes

    shouldSendReminderFromEmployer: function (user) {
      if (!this.replyPending) {
        return false;
      }
      if (Util.daysSince(new Date(), this.timeFirstReminderSent, user) > 7) {
        if (Util.daysSince(new Date(), this.timeEmployeeSMSSent, user) > 7) {
          return true;
        }
      }
      return false;
    }
  }
}
