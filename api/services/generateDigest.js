module.exports = {
  run : function(user) {
    Digest.findOne({user: user.id}).exec(function (err, digest) {
      if (err) {
        return;
      }
      var moment = require('moment-timezone');
      var date = moment(Util.getDateObject()).tz(user.getTZ());
      if ((date.hour() < 9) || (date.hour() >= 11)) {
        return;
      }
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
    Task.find({assignedBy: user.id}).exec(function (err, tasks) {
      var updateCount = 0;
      var taskCount = 0;
      var escalateTask = 0;
      for (var i = 0; i < tasks.length; i++) {
        if (tasks[i].updateCount) {
          updateCount += tasks[i].updateCount;
          taskCount += 1;
        }
        if (tasks[i].taskPriority >= 100) {
          escalateTask += 1;
        }
      }
      if (updateCount) {
        generateDigest.createDigest(user, digest, function () {
          SendNotification.sendNotification(user.id, user.id, 
            updateCount + ' updates received on '+ taskCount + ' tasks.', 
            null,
           'digest',
            function (err) {}
          );
        });
        return;
      }
      if (escalateTask) {
        generateDigest.createDigest(user, digest, function () {
          SendNotification.sendNotification(user.id, user.id, 
            'Updates not received for ' + escalateTask + ' tasks. Escalation is required.', 
            null,
            'digest',
            function (err) {}
          );
        });
        return;
      }
      generateDigest.checkForTaskCreation(user, digest);
    });
  },

  checkForTaskCreation: function (user, digest) {
    User.find({manager: user.id}).exec(function (err, users) {
      for (var i = 0; i < users.length; i++) {
        if (users[i].taskCount == 0) {
          generateDigest.createDigest(user, digest, function () {
            SendNotification.sendNotification(user.id, user.id,
              'No tasks assigned to ' + users[i].name + '. Assign tasks to start automatically monitoring progress.',
              null,
              'digest',
              function (err) {}
            );
          });
          return;
        }
      }
    });
  },

  createDigest: function (user, digest, cb) {
    if (digest) {
      Digest.update({id: digest.id}, {timeSent: Util.getDateObject()}).exec(function (err, updateDigest) {
        cb();
      });
    } else {
      Digest.create({user: user.id, timeSent: Util.getDateObject()}).exec(function (err, updateDigest) {
        cb();
      });
    }
  }
}
