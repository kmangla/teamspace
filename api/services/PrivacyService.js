module.exports = {
  task: function (query, toPopulate, cb) {
    var origToPopulate = toPopulate.slice(0);
    if (toPopulate.indexOf('assignedBy') == -1) {
      toPopulate.push('assignedBy');
    }
    if (toPopulate.indexOf('assignedTo') == -1) {
      toPopulate.push('assignedTo');
    }
    Util.populateInQuery(query, toPopulate);
    query.exec(function (err, tasks) {
      if (err) {
        cb(err, []);
        return;
      }
      var workingTasks = [];
      for (var i = 0; i < tasks.length; i++) {
        if (!tasks[i].assignedTo || !tasks[i].assignedBy) {
          continue;
        }
        var task = tasks[i];
        if (origToPopulate.indexOf('assignedBy') == -1) {
          task.assignedBy = task.assignedBy.id;
        }
        if (origToPopulate.indexOf('assignedTo') == -1) {
          task.assignedTo = task.assignedTo.id;
        }
        workingTasks.push(task);
      }
      cb(null, workingTasks);
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
