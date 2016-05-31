module.exports = {
  run : function(user) {
    Digest.findOne({user: user.id}).exec(function (err, digest) {
      if (err) {
        return;
      }
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      // Only send digest between 10 and 12 am
      if ((date.hour() < 10) || (date.hour() >= 14)) {
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
      generateDigest.checkForNonResponsiveEmployees(user, digest);
    });
  },

  checkForNonResponsiveEmployees: function (user, digest) {
    User.find({manager: user.id}).exec(function (err, employees) {
      var userMap = Util.extractMap(employees, "id");
      UserGlobalStatus.find().where({user: Object.keys(userMap)}).exec(function (err, statuses) {
        Task.find().where({assignedTo: Object.keys(userMap), status: 'open'}).exec(function (err, tasks) {
          var taskMap = Util.extractMapListBasic(tasks, "assignedTo");
          for (var i = 0; i < statuses.length; i++) {
            var employee = userMap[statuses[i].user];
            var tasks = taskMap[employee.id];
            if (tasks != null) {
              var taskIsPending = false;
              for (var j = 0; j < tasks.length; j++) {
                if (tasks[j].reminderIsDue(employee)) {
                  taskIsPending = true;
                }
              }
              if (taskIsPending && statuses[i].employerCallNeeded(employee)) {
                var message = 'No updates received from ' + employee.name + ' on any assigned tasks. Please contact directly.'; 
                generateDigest.createDigest(user, digest, 'no_update', message, function () {
                  SendNotification.sendNotification(user.id, user.id, 
                    message,
                    null,
                    'noOwner',
                    function (err) {}
                  );
                });
              return;
              }
            }
          }
        });
        generateDigest.checkForPendingUpdates(user, digest);
      });
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
          updateCount + ' new updates received';
        generateDigest.createDigest(user, digest, 'task_update', message, function () {
          SendNotification.sendNotification(user.id, user.id, 
            message,
            null,
           'noOwner',
            function (err) {}
          );
        });
        return;
      }
      generateDigest.checkForTaskCreation(user, digest);
    });
  },

  checkForTaskCreation: function (user, digest) {
    Task.find({assignedBy: user.id, status: 'open'}).exec(function (err, tasks) {
      if (tasks.length == 0) {
        var message = 
          'All tasks complete. Create new tasks to monitor progress.';
        generateDigest.createDigest(user, digest, 'new_task', message, function () {
          SendNotification.sendNotification(user.id, user.id,
            message,
            null,
            'noOwner',
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
