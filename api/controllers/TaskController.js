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
    }

    Task.create(taskObj, function (err, task) {
      if (err) {
        console.log(err);
        return res.send(err);
      }
      task.lastUpdate = new Date(0);
      task.updateCount = 0;
      task.save(function(err, task) {
        if (err) {
        	return res.send(err);
        }
      	task.employeeName = task.assignedTo.name;
        StatsService.sendStats("task.create_count", 1);
      	return res.json(task.id); 
      });
    });
  },

  listAll: function (req, res) {
    //TODO: req.param('status') is valid, ie its in [open, close]
    var query = Task.find({assignedBy: req.session.User.id, status: req.param('status')});
    PrivacyService.task(query, Util.populateParamToExpand(req), function(err, tasks) {
      if(err) return res.send(err);
      return res.json(tasks);
    });
  },

  list: function (req, res) {
    //TODO: req.param('status') is valid, ie its in [open, close]
    var query = Task.find({assignedTo: req.param('employeeID'), assignedBy: req.session.User.id, status: req.param('status')});
    PrivacyService.task(query, Util.populateParamToExpand(req), function(err, tasks) {
      if(err) return res.send(err);
      //If user requested employee details reset update count
      //TODO: need to maintain counter for both user and employee
      /*if (task[0].assignedBy === req.session.User.id) {
        User.update({id: task[0].assignedTo.id}, {updateCount: 0}).exec(function(err, employee) {
          if (err) return res.send(err);
        });
      }*/
      return res.json(tasks);
    });
  },

  updateTask: function (req, res) {
    var taskUpdateObj = {}; 
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
    }
    taskUpdateObj.id = req.params.id;
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

