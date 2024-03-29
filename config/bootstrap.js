/**
 * Bootstrap
 * (sails.config.bootstrap)
 *
 * An asynchronous bootstrap function that runs before your Sails app gets lifted.
 * This gives you an opportunity to set up your data model, run jobs, or perform some special logic.
 *
 * For more information on bootstrapping your app, check out:
 * http://sailsjs.org/#/documentation/reference/sails.config/sails.config.bootstrap.html
 */

module.exports.bootstrap = function(cb) {
  var reminder = require('../crontab/reminders.js');
  var sender = require('../crontab/sendReminders.js');
  var digest = require('../crontab/digest.js');
  //var reports = require('../crontab/userRecords.js');

  OTP.native(function(err, collection) {
    collection.ensureIndex('phoneNumber', {
      unique: true
    }, function(err, result) {
      if (err) {
        sails.log.error(err);
      }
    });
  });

  User.native(function(err, collection) {
    collection.ensureIndex('phone', {
      unique: true
    }, function(err, result) {
      if (err) {
        sails.log.error(err);
      }
    });
  });

  UserStatus.native(function(err, collection) {
    collection.ensureIndex('user', {
      unique: true
    }, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Digest.native(function(err, collection) {
    collection.ensureIndex('user', {
      unique: true
    }, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Task.native(function(err, collection) {
    collection.ensureIndex('assignedTo', {}, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Task.native(function(err, collection) {
    collection.ensureIndex('assignedBy', {}, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Task.native(function(err, collection) {
    collection.ensureIndex('status', {}, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Message.native(function(err, collection) {
    collection.ensureIndex('forTask', {}, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  Notification.native(function(err, collection) {
    collection.ensureIndex('task', {}, function(err, result) {
      if (err) {
        console.log(err);
      }
    });
  });

  setInterval(reminder.run, 1000 * 60 * 15);
  setInterval(sender.run, 1000 * 60 * 1);
  setInterval(digest.run, 1000 * 60 * 60);

  var http = require("http");
  http.get("http://teamspace.herokuapp.com");
  setInterval(function() {
    http.get("http://teamspace.herokuapp.com");
  }, 300000);
  //reports.run();
  // It's very important to trigger this callback method when you are finished
  // with the bootstrap!  (otherwise your server will never lift, since it's waiting on the bootstrap)
  cb();
};
