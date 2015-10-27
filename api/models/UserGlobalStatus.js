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
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      if (!((date.hour() >= 9) && (date.hour() <= 17) && (date.day() != 0) && (date.date() != 6))) {
        return false;
      }
      if (Util.daysSince(new Date(), this.timeFirstReminderSent, user) > 3) {
        if ((this.timeEmployeeSMSSent == null) || (Util.daysSince(new Date(), this.timeEmployeeSMSSent, user) > 3)) {
          return true;
        }
      }
      return false;
    }
  },

  sendReminderFromEmployer: function (employer, employee, taskID, cb) {
    PushToken.findOrAssignToken(employee, function (err, token) {
      var message = 'Please reply to reminder messages from ' + token.deviceID + ' with updates on tasks.';
      Logging.logInfo('employee_sms', employer.id, employee.id, taskID, message);
      if ((employer.id == '5548fbd7cd008003003f04ab') || employer.id == '5623a51e28c11d030061177e') {
        SendNotification.sendNotification(employer.id, employee.id, message, taskID, 'silentMessage', function () {
          UserGlobalStatus.update({user: employee.id}, {timeEmployeeSMSSent: new Date()}, function (err, updatedStatus) {cb();});
        });
      }
    });
  }
}
