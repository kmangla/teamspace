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
    query.exec(function (err, messages) {
      if (err) {
        cb(err, []);
        return;
      }
      var usersToFetch = {};
      var tasksToFetch = {};
      for (var i = 0; i < messages.length; i++) {
        usersToFetch[messages[i].sentBy] = 1;
        tasksToFetch[messages[i].forTask] = 1;
      }
      User.find().where({id: Object.keys(usersToFetch)}).exec(function (err, users) {
        Task.find().where({id: Object.keys(tasksToFetch)}).exec(function (err, tasks) {
          var workingMessages = [];
          var userMap = Util.extractMap(users, "id");
          var taskMap = Util.extractMap(tasks, "id");
          for (var i = 0; i < messages.length; i++) {
            if (!messages[i].sentBy || !messages[i].forTask || !userMap[messages[i].sentBy] || !taskMap[messages[i].forTask]) {
              continue;
            }
            var message = messages[i];
            if (toPopulate.indexOf('forTask') != -1) {
              message.forTask = taskMap[messages[i].forTask];
            }
            if (toPopulate.indexOf('sentBy') != -1) {
              message.sentBy = userMap[messages[i].sentBy];
            }
            workingMessages.push(message);
          }
          cb(null, workingMessages);
        });
      });
    });
  },
};
