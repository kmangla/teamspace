module.exports = {
  run : function(user) {
    Digest.findOne({user: user.id}).exec(function (err, digest) {
      if (err) {
        return;
      }
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      // Only send digest between 9 and 11
      if ((date.hour() < 9) || (date.hour() >= 11)) {
        return;
      }
      // If a digest was sent, do not send in the same day.
      if (digest) {
        var daysSince = Util.daysSince(Util.getDateObject(), digest.timeSent, user);
        if (daysSince <= 0) {
          return;
        }
      }
      generateDigest.checkForPendingUpdates(user, digest);
    });
  },
  
  checkForPendingUpdates: function (user, digest) {
    Task.find({assignedBy: user.id, status: 'open'}).populate('currentStatus').populate('assignedTo').exec(function (err, tasks) {
      var updatedTask = [];
      var escalateTask = [];
      for (var i = 0; i < tasks.length; i++) {
        if (task.assignedTo.id == task.assignedBy) {
          continue;
        }
        if (tasks[i].updateCount && tasks[i].assignedTo) {
          updatedTask.push(tasks[i]);
        }
        if (tasks[i].taskPriority() >= 100 && tasks[i].currentStatus.replyPending && tasks[i].assignedTo) {
          escalateTask.push(tasks[i]);
        }
      }
      if (updatedTask.length) {
        var randomNumber = RandomNumber.randomInt(0, updatedTask.length);
        var randomTask = updatedTask[randomNumber];
        var message =
          'Update received from ' + randomTask.assignedTo.name;
        generateDigest.createDigest(user, digest, 'task_update', message, function () {
          SendNotification.sendNotification(user.id, user.id, 
            message,
            null,
           'digest',
            function (err) {}
          );
        });
        return;
      }
      if (escalateTask.length) {
        var randomNumber = RandomNumber.randomInt(0, escalateTask.length);
        var randomTask = escalateTask[randomNumber];
        var message =
          'No replies received from ' + randomTask.assignedTo.name + '. Contact for update';
        generateDigest.createDigest(user, digest, 'task_update', message, function () {
          SendNotification.sendNotification(user.id, user.id, 
            message, 
            null,
            'digest',
            function (err) {}
          );
        });
        return;
      }
      // If tasks created less than 3
      if (tasks.length < 3) {
        generateDigest.checkForTaskCreation(user, digest);
      }
    });
  },

  checkForTaskCreation: function (user, digest) {
    // For Task creation reminders send only once every 3 days
    if (digest) {
      var daysSince = Util.daysSince(Util.getDateObject(), digest.timeSent, user);
      if (daysSince <= 3) {
        return;
      }
    }
    User.find({manager: user.id, accountStatus: 'active'}).exec(function (err, users) {
      var usersWithNoTask = [];
      for (var i = 0; i < users.length; i++) {
        if (users[i].taskCount == 0) {
          usersWithNoTask.push(users[i]);
        }
      }
      if (!usersWithNoTask.length) {
       return;
      }
      var randomNumber = RandomNumber.randomInt(0, usersWithNoTask.length);
      var randomUser = usersWithNoTask[randomNumber];
      var message = 
        'No tasks assigned to ' + randomUser.name + '. Assign tasks to start automatically monitoring progress.';
      generateDigest.createDigest(user, digest, 'new_task', message, function () {
        SendNotification.sendNotification(user.id, user.id,
          message,
          null,
          'digest',
          function (err) {}
        );
      });
      return;
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
