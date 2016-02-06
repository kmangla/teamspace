module.exports = {
  run : function(user) {
    Digest.findOne({user: user.id}).exec(function (err, digest) {
      if (err) {
        return;
      }
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      // Only send digest between 10 and 12 am
      if ((date.hour() < 10) || (date.hour() >= 18)) {
        return;
      }
      // If a digest was sent, do not send in the same day.
      var daysSince = 100;
      if (digest) {
        daysSince = Util.daysSince(Util.getDateObject(), digest.timeSent, user);
      }

      if (daysSince <= 0) {
        return;
      }
      generateDigest.checkForPendingUpdates(user, digest);
    });
  },
  
  checkForPendingUpdates: function (user, digest) {
    Task.find({assignedBy: user.id, status: 'open'}).populate('currentStatus').populate('assignedTo').exec(function (err, tasks) {
      var statusesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        if (tasks[i].assignedTo) {
          statusesToFetch[tasks[i].assignedTo.id] = 1;
        }
      }
      var updatedTask = [];
      var updateCount = 0;
      for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        if (tasks[i].assignedTo && (tasks[i].assignedTo.id == tasks[i].assignedBy)) {
          continue;
        }
        if (tasks[i].updateCount && tasks[i].assignedTo) {
          updatedTask.push(tasks[i]);
          updateCount = updateCount + tasks[i].updateCount;
        }
      }
      if (updatedTask.length) {
        var randomNumber = RandomNumber.randomInt(0, updatedTask.length);
        var randomTask = updatedTask[randomNumber];
        var message =
          updateCount + ' tasks have new updates';
        generateDigest.createDigest(user, digest, 'task_update', message, function () {
          SendNotification.sendNotification(user.id, user.id, 
            message,
            null,
           'taskList',
            function (err) {}
          );
        });
        return;
      }
      generateDigest.checkForTaskCreation(user, digest);
    });
  },

  checkForEmployeeCreation: function (user, digest) {
    User.count({manager: user.id}).exec(function (error, found) {
      if (found <= 2 &&  (!digest || (digest.type !== 'employeeCreation'))) {
        var message = 
          'Add employees to monitor tasks';
        generateDigest.createDigest(user, digest, 'employeeCreation', message, function () {
          SendNotification.sendNotification(user.id, user.id,
            message,
            null,
            'employeeCreation',
            function (err) {}
          );
        });
        return;
      }
    });
  },

  checkForTaskCreation: function (user, digest) {
    var daysSince = 100;
    if (digest) {
      daysSince = Util.daysSince(Util.getDateObject(), digest.timeSent, user);
    }

    if (daysSince <= 2) {
      return;
    }
    var secondDate = new Date();
    secondDate.setDate(secondDate.getDate() - 14);
    Task.find({assignedBy: user.id, createdAt: {'>': secondDate, '<': new Date()}}).exec(function (err, tasks) {
      if (tasks.length == 0) {
        var message = 
          'No tasks created in last two weeks. Create new tasks to monitor employees';
        generateDigest.createDigest(user, digest, 'new_task', message, function () {
          SendNotification.sendNotification(user.id, user.id,
            message,
            null,
            'taskCreation',
            function (err) {}
          );
        });
        return;
      }
    });
  },

  createDigest: function (user, digest, type, message, cb) {
    Logging.logInfo('digest', user.id, null, null, 'Digest sent: ' + type + ':' + message);
    if (digest) {
      Digest.update({id: digest.id}, {timeSent: Util.getDateObject(), type: type}).exec(function (err, updateDigest) {
        cb();
      });
    } else {
      Digest.create({user: user.id, timeSent: Util.getDateObject(), type: type}).exec(function (err, updateDigest) {
        cb();
      });
    }
  }
}
