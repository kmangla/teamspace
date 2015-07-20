module.exports = {
  task: function (query, toPopulate, cb) {
    query.exec(function (err, tasks) {
      if (err) {
        cb(err, []);
        return;
      }
      var usersToFetch = {};
      var messagesToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        usersToFetch[tasks[i].assignedTo] = 1;
        usersToFetch[tasks[i].assignedBy] = 1;
        if (toPopulate.indexOf('lastMessage') != -1) {
          messagesToFetch[tasks[i].lastMessage] = 1;
        }
      }
      User.find().where({id: Object.keys(usersToFetch)}).exec(function (err, users) {
        Message.find().where({id: Object.keys(messagesToFetch)}).exec(function (err, messages) {
          var userMap = Util.extractMap(users, "id");
          var messageMap = Util.extractMap(messages, "id");
          var workingTasks = [];
          for (var i = 0; i < tasks.length; i++) {
            var task = tasks[i];
            if (!tasks[i].assignedBy || !tasks[i].assignedTo || !userMap[tasks[i].assignedBy] || !userMap[tasks[i].assignedTo]) {
              continue;
            }
            if (toPopulate.indexOf('assignedBy') != -1) {
              task.assignedBy = userMap[tasks[i].assignedBy];
            }
            if (toPopulate.indexOf('assignedTo') != -1) {
              task.assignedTo = userMap[tasks[i].assignedTo];
            }
            if (toPopulate.indexOf('lastMessage') != -1) {
              task.lastMessage = messageMap[tasks[i].lastMessage];
            }
            workingTasks.push(task);
          }
          cb(null, workingTasks);
        });
      });
    });
  },

  message: function (query, toPopulate, cb) {
    var origToPopulate = toPopulate.slice(0);
    if (toPopulate.indexOf('forTask') == -1) {
      toPopulate.push('forTask');
    }
    if (toPopulate.indexOf('sentBy') == -1) {
      toPopulate.push('sentBy');
    }
    Util.populateInQuery(query, toPopulate);
    query.exec(function (err, messages) {
      if (err) {
        cb(err, []);
        return;
      }
      var workingMessages = [];
      for (var i = 0; i < messages.length; i++) {
        if (!messages[i].sentBy || !messages[i].forTask) {
          continue;
        }
        var message = messages[i];
        if (origToPopulate.indexOf('forTask') == -1) {
          message.forTask = message.forTask.id;
        }
        if (origToPopulate.indexOf('sentBy') == -1) {
          message.sentBy = message.sentBy.id;
        }
        workingMessages.push(message);
      }
      cb(null, workingMessages);
    });
  },
};
