/**
 * TaskController
 *
 * @description :: Server-side logic for managing tasks
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

	create: function(req, res, next) {
    var taskObj = {
    	title: req.param('title'),
      description: req.param('description'),
      status: req.param('status') || 'open',
      frequency: req.param('frequency'),
      assignedTo: req.param('employeeID'),
      assignedBy: req.session.User.id,
      lastUpdate: new Date(0),
      updateCount: 0,
      fav: 0,
    }
   
    Task.create(taskObj, function (err, task) {
      if (err) {
        Logging.logError('task_controller', taskObj.assignedBy, taskObj.assignedTo, null, 'Task creation failed ' +  err);
        return res.send(err);
      }
      task.employeeName = task.assignedTo.name;
      StatsService.sendStats("task.create_count", 1);
      Logging.logInfo('task_controller', task.assignedBy, task.assignedTo, task.id, 'Task creation succeeded');
      if (req.param('sendReminderNow') == true) {
        User.update({id: task.assignedTo}, {priorityTask: task.id}).exec(function (err, user) {
          return res.json(task); 
        });
      }
    });
  },

  listAll: function (req, res) {
    var query = Task.find({assignedBy: req.session.User.id, status: 'open'});
    var params = Util.populateParamToExpand(req);
    params.push('currentStatus');
    var paging = req.param('paging');
    if (paging) {
      query = null;
    }
    var status = req.param('status');
    PrivacyService.task(query, params, function(err, tasks) {
      var query = Task.find({assignedBy: req.session.User.id, status: 'closed'}).sort({createdAt: 'desc'}).skip(paging).limit(10 - tasks.length);
      if (tasks.length >= 10) {
        query = null;
      }
      if (status == 'open') {
        query = null;
      }
      var params = Util.populateParamToExpand(req);
      params.push('currentStatus');
      PrivacyService.task(query, params, function(err, closedTasks) {
        tasks = tasks.concat(closedTasks); 
        var employeeIDs = {};
        for (var j = 0; j < tasks.length; j++) {
          employeeIDs[tasks[j].assignedTo.id] = 1;
        }
        UserGlobalStatus.find().where({user: Object.keys(employeeIDs)}).exec(function (err, statuses) {
          if(err) return res.send(err);
          var taskIDs = Object.keys(Util.extractMap(tasks, "id"));
          var statusMap = Util.extractMap(statuses, "user");
          MockMessage.createMockMessage(taskIDs, function (err, taskMap) {
            var tasksWithMessages = [];
            for (var i = 0; i < tasks.length; i++) {
              var task = tasks[i];
              var message = Util.extractKey(taskMap, task.id);
              if (message) {
                task.lastMessage = message;
              }
              task.priority = task.taskPriority(task.assignedTo, task.currentStatus, statusMap[task.assignedTo.id]);
              task.currentStatus = task.currentStatus.id;
              tasksWithMessages.push(task);
            }
            return res.json(tasksWithMessages);
          });
        });
      });
    });
  },

  list: function (req, res) {
    //TODO: req.param('status') is valid, ie its in [open, close]
    var query = Task.find({assignedTo: req.param('employeeID'), assignedBy: req.session.User.id, status: 'open'});
    var params = Util.populateParamToExpand(req);
    params.push('currentStatus');
    var paging = req.param('paging');
    if (paging) {
      query = null;
    }
    var status = req.param('status');
    PrivacyService.task(query, params, function(err, tasks) {
      var query = Task.find({assignedTo: req.param('employeeID'), assignedBy: req.session.User.id, status: 'closed'}).sort({createdAt: 'desc'}).skip(paging).limit(10 - tasks.length);
      if (tasks.length >= 10) {
        query = null;
      }
      if (status == 'open') {
        query = null;
      }
      var params = Util.populateParamToExpand(req);
      params.push('currentStatus');
      PrivacyService.task(query, params, function(err, closedTasks) {
        tasks = tasks.concat(closedTasks); 
      var employeeIDs = {};
      for (var j = 0; j < tasks.length; j++) {
        employeeIDs[tasks[j].assignedTo.id] = 1;
      }
      UserGlobalStatus.find().where({user: Object.keys(employeeIDs)}).exec(function (err, statuses) {
        if(err) return res.send(err);
        var statusMap = Util.extractMap(statuses, "user");
        var taskIDs = Object.keys(Util.extractMap(tasks, "id"));
        MockMessage.createMockMessage(taskIDs, function (err, taskMap) {
          var tasksWithMessages = [];
          for (var i = 0; i < tasks.length; i++) {
            var task = tasks[i];
            var message = Util.extractKey(taskMap, task.id);
            if (message) {
              task.lastMessage = message;
            }
            task.priority = task.taskPriority(task.assignedTo, task.currentStatus, statusMap[task.assignedTo.id]);
            task.currentStatus = task.currentStatus.id;
            tasksWithMessages.push(task);
          }
          return res.json(tasksWithMessages);
        });
      });
    });
    });
  },

  updateTask: function (req, res) {
    var taskUpdateObj = {};
    taskUpdateObj.id = req.params.id;
    if (req.param('markUpdated')) {
      Logging.logInfo('here');
      var id = req.params.id;
      taskUpdateObj.lastUpdate = new Date();
      taskUpdateObj.forceReminder = false;
      UserStatus.changeStatusIfRequired(req.params.id, function (err) {
        if (err) {}
      });
    }
    if (req.param('title')) {
      taskUpdateObj.title = req.param('title');
    }
    if (req.param('description')) {
      taskUpdateObj.description = req.param('description');
    }
    //TODO: check status is in enum list
    if (req.param('status')) {
      taskUpdateObj.status = req.param('status');
    }
    //TODO: check frequency is int
    if (req.param('frequency')) {
      taskUpdateObj.frequency = req.param('frequency');
    }
    //TODO: check employee reports to user
    if (req.param('employeeID')) {
      taskUpdateObj.assignedTo = req.param('employeeID');
    }

    if (req.param('forceReminder')) {
      taskUpdateObj.forceReminder = true;
      taskUpdateObj.forceReminderTime = new Date();
    }

    if (req.param('fav')) {
      taskUpdateObj.fav = req.param('fav');
    }
    Task.update({id: req.params.id}, taskUpdateObj).exec(function(err, task) {
      if (err) return res.send(err);
      res.json(task);
    });
  },

  deleteTask: function (req, res) {
    Task.destroy({id: req.params.id}).exec(function(err, task) {
      if (err) return res.send(err);
      res.send(200);
    });
  },
	
};
