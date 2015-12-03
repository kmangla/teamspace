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

    employeeNotResponding: function (user) {
      if (!this.replyPending) {
        return false;
      }
      if (Util.daysSince(new Date(), this.timeFirstReminderSent, user) > 3) {
        return true;
      }
      return false;
    },

    shouldSendReminderFromEmployer: function (user) {
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      if (!((date.hour() >= 11) && (date.hour() <= 17) && (date.day() != 0) && (date.date() != 6))) {
        return false;
      }
      if (this.employeeNotResponding(user)) {
        if ((this.timeEmployeeSMSSent == null) || (Util.daysSince(new Date(), this.timeEmployeeSMSSent, user) > 3)) {
          return true;
        }
      }
      return false;
    }
  },

  sendReminderFromEmployer: function (employer, employee, taskID, cb) {
    PushToken.findOrAssignToken(employee, function (err, token) {
      var message = 'You will have received SMS\'s from ' + token.deviceID + '. Please reply to that number with updates on tasks.';
      Logging.logInfo('employee_sms', employer.id, employee.id, taskID, message);
      SendNotification.sendNotification(employer.id, employee.id, message, taskID, 'silentMessage', function () {
        UserGlobalStatus.update({user: employee.id}, {timeEmployeeSMSSent: new Date()}, function (err, updatedStatus) {cb();});
      });
    });
  }
}
