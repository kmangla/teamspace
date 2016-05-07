module.exports = {
  task: function (query, toPopulate, cb) {
    if (query == null) {
      cb(null, []);
      return;
    }
    query.exec(function (err, tasks) {
      if (err) {
        cb(err, []);
        return;
      }
      var usersToFetch = {};
      var messagesToFetch = {};
      var statusToFetch = {};
      for (var i = 0; i < tasks.length; i++) {
        usersToFetch[tasks[i].assignedTo] = 1;
        usersToFetch[tasks[i].assignedBy] = 1;
        if (toPopulate.indexOf('lastMessage') != -1) {
          messagesToFetch[tasks[i].lastMessage] = 1;
        }
        if (toPopulate.indexOf('currentStatus') != -1) {
          statusToFetch[tasks[i].currentStatus] = 1;
        }
      }
      User.find().where({id: Object.keys(usersToFetch)}).exec(function (err, users) {
        Message.find().where({id: Object.keys(messagesToFetch)}).exec(function (err, messages) {
          TaskStatus.find().where({id: Object.keys(statusToFetch)}).exec(function (err, statuses) {
            var userMap = Util.extractMap(users, "id");
            var messageMap = Util.extractMap(messages, "id");
            var taskStatusMap = Util.extractMap(statuses, "id");
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
              if (toPopulate.indexOf('currentStatus') != -1) {
                task.currentStatus = taskStatusMap[tasks[i].currentStatus];
              }
              workingTasks.push(task);
            }
            cb(null, workingTasks);
          });
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

  user: function (query, toPopulate, cb) {
    query.exec(function (err, users) {
      if (err) {
        cb(err, []);
        return;
      }
      var usersToFetch = {};
      for (var i = 0; i < users.length; i++) {
        usersToFetch[users[i].manager] = 1;
      }
      User.find().where({id: Object.keys(usersToFetch)}).exec(function (err, managers) {
        var managerMap = Util.extractMap(managers, "id");
        var workingUsers = [];
        for (var i = 0; i < users.length; i++) {
          var user = users[i];
          if (!users[i].manager || !managerMap[users[i].manager] || (users[i].accountStatus != 'active')) {
            continue;
          }
          if (toPopulate.indexOf('manager') != -1) {
            user.manager = managerMap[users[i].manager];
          }
          workingUsers.push(user);
        }
        cb(null, workingUsers);
      });
    });
  },
};
