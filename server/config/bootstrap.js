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

  setInterval(reminder.run, 1000 * 60 * 15);
  setInterval(sender.run, 1000 * 60 * 2);
 
  var http = require("http");
  http.get("http://teamspace.herokuapp.com");
setInterval(function() {
    http.get("http://teamspace.herokuapp.com");
}, 300000); 
  // It's very important to trigger this callback method when you are finished
  // with the bootstrap!  (otherwise your server will never lift, since it's waiting on the bootstrap)
  cb();
};
