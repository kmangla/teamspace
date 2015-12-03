module.exports = {
  createMockMessage: function(taskIDs, cb) {
    var taskIDstoMessage = {};
    Task.find().where({id: taskIDs}).populate('assignedBy').populate('assignedTo').populate('currentStatus').exec(function (err, tasks) {
      var userStatusesToFetch = {};
      var dueTasks = [];
      for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        userStatusesToFetch[task.assignedTo.id] = 1;
        if (task.reminderIsDue(task.assignedTo)) {
          dueTasks.push(task);
        }
      }
      UserStatus.find().where({user: Object.keys(userStatusesToFetch)}).exec(function (err, statuses) {
        var statusMap = Util.extractMap(statuses, "user");
        var userMapList = Util.extractMapList(dueTasks, "assignedTo", "id");
        var userSortedTaskMap = {};
        for (var userID  in userMapList) {
          userSortedTaskMap[userID] = MockMessage.sortOrder(userMapList[userID], statusMap[userID]);
        }
        for (var i = 0; i < dueTasks.length; i++) {
          var task = dueTasks[i];
          var userID = task.assignedTo.id;
          if (statusMap[userID].taskSent == dueTasks[i].id) {
            taskIDstoMessage[task.id] = MockMessage.createReminderCurrentlySentMessage(task);
          } else {
            if (task.currentStatus.replyPending) {
              taskIDstoMessage[task.id] = MockMessage.createRepeatReminderWillBeSent(task, userSortedTaskMap[userID][task.id]);
            } else {
              taskIDstoMessage[task.id] = MockMessage.createReminderWillBeSent(task, userSortedTaskMap[userID][task.id]);
            }
          }
        }
        cb(null, taskIDstoMessage);
      });
    });
  },

  sortOrder: function(tasks, status) {
    var taskOrderList = {};
    for (var i = 0; i < tasks.length; i++) {
      var task = null;
      for (var j = 0; j < tasks.length; j++) {
        if (tasks[j].id in taskOrderList) {
          continue;
        }
        if (task == null) {
          task = tasks[j];
          continue;
        }
        if (status.taskSent == tasks[j].id) {
          task = tasks[j];
          continue;
        }
        if (status.taskSent == task.id) {
          continue;
        }
        if (tasks[j].shouldGoBefore(task)) {
          task = tasks[j];
        }
      }
      taskOrderList[task.id] = i;
    }
    return taskOrderList;
  },

  createRepeatReminderWillBeSent: function (task, offset) {
    var message = {
      id: 'm_' + task.id,
      description: 'Next reminder to be sent on ' +  Util.dateString(offset),
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    }; 
    return message;
  },

  createReminderWillBeSent: function (task, offset) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder to be sent on ' + Util.dateString(offset),
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }; 
    return message;
  },

  createReminderCurrentlySentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder being sent today',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    };
    return message;
  },

  createReminderSentMessage: function (task) {
    var message = {
      id: 'm_' + task.id,
      description: 'Reminder sent',
      forTask: task.id,
      sentBy: task.assignedBy,
      systemGenerated: true,
      createdAt: task.currentStatus.timeReminderSent,
      updatedAt: task.currentStatus.timeReminderSent
    };
    return message;
  },
}
