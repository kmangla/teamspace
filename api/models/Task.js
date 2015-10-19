/**
* Task.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

	schema: true,

  attributes: {
  	
    title: {
      type: 'string',
      required: true,
      size: 140
    },

  	description: {
    	type: 'string',
    	size: 1000
  	},
		
		status: {
    	type: 'string',
    	enum: ['open', 'closed', 'blocked'],
      required: true
    },
    
    frequency: {
    	type: 'integer',
      defaultsTo: 604800,
    	//enum: ['daily', 'alterateday', 'weekly', 'biweekly', 'monthly']
  	},
  	
    updateCount: {
      type: 'integer',
      defaultsTo: 0
    },

  	lastUpdate: {
    	type: 'datetime'
  	},

  	assignedTo: {
  		model: 'user',
  		required: true
  	},

  	assignedBy: {
  		model: 'user',
  		required: true
  	},

    lastMessage: {
      model: 'message',
    },

    messages: {
      collection: 'message',
      via: 'forTask'
    },

    forceReminder: {
      type: 'boolean',
      defaultsTo: false
    },
  
    forceReminderTime: {
      type: 'datetime'
    },

  	currentStatus: {
  		model: 'taskstatus'
  	},

    // Custom attribute methods

    shouldGoBefore: function (task) {
      if (task.forceReminder && this.forceReminder) {
        if (this.forceReminderTime > task.forceReminderTime) {
          return true;
        }
        return false;
      }
      if (task.forceReminder && !this.forceReminder) {
        return false;
      }
      if (this.forceReminder && !task.forceReminder) {
        return true;
      }
      if (this.currentStatus.timeReminderSent > task.currentStatus.timeReminderSent) {
        return false;
      }
      return true;
    },
  
    getUpdateDueSince: function () {
      var dateNew = new Date(1);
      var timeUpdated = this.lastUpdate;
      if (this.lastUpdate < dateNew) {
        timeUpdated = this.createdAt;
      }
      var dueSince = timeUpdated.getTime() +  this.frequency * 1000;
      if (this.forceReminder && this.forceReminderTime) {
        var forceTime = this.forceReminderTime.getTime();
        if (forceTime < dueSince) {
          dueSince = forceTime;
        }
      }
      return dueSince;
    },

    taskPriority: function () {
      var date = new Date();
      var dueSince = this.getUpdateDueSince();
      var delayTimeInSec = Math.round((date.getTime() - dueSince)/1000);
      if (delayTimeInSec < 0) {
        return 0;
      }
      var maxDelay = 3 * 24 * 3600 ; // 3 days
      if (delayTimeInSec >  maxDelay) {
        return 100;
      }
      return Math.ceil(delayTimeInSec/maxDelay * 100);
    },

    toJSON: function () {
      var obj = this.toObject();
      obj.priority = this.taskPriority();
      return obj;
    },

    reminderIsDue: function(user) {
      if (this.forceReminder) {
        return true;
      }
      var date = new Date();
      var days = Util.daysSince(date, this.lastUpdate, user);
      if (days >= (this.frequency / 86400)) {
        return true;
      } else {
        return false;
      }
    },
  },

   beforeCreate: function(task, cb) {
     var taskStatus = {
       replyPending: false,
       reminderCount: 0
     };
     TaskStatus.create(taskStatus, function (err, taskStatusCreated) {
       task.currentStatus = taskStatusCreated.id;
       User.updateTaskCount(task.assignedTo, 1, function (err) {
         if (err) {
         }
         cb();
       });
     });
   },

   beforeUpdate: function(task, cb) {
     Task.findOne({id: task.id}, function (err, originalTask) {
       if (err) {
         cb();
         return;
       }
       if (!originalTask) {
         cb();
         return;
       }
       /*
       if(originalTask.assignedTo != task.assignedTo) {
         User.updateTaskCount(originalTask.assignedTo, -1, function (err) {
           if (err) { console.log(err);}
         });
         User.updateTaskCount(task.assignedTo, 1, function (err) {
           if (err) { console.log(err);}
         });
       }*/
       if ((originalTask.status == 'open') && (task.status == 'closed')) {
         User.updateTaskCount(originalTask.assignedTo, -1, function (err) {
           if (err) {
           }
           cb();
         });
       } else {
         cb();
       }
     });
   },

   afterDestroy: function(task, cb) {
    if (task[0]) {
      User.updateTaskCount(task[0].assignedTo, -1, function (err) {
        if (err) {
        }
        cb();
      });
    } else {
      cb();
    }
  },
   
   reminderMessageAndNotifications: function (task, cb) {
     Notification.destroy({task: task.id}).exec(function (err, notifications) {
       Task.reminderMessage(task, function (err, message) {
         cb(err, message, notifications);
       });
     });
   },

   reminderMessage: function(task, cb) {
     User.findOne({id: task.assignedBy}, function (err, user) {
       if (err) {
         cb(err);
         return;
       }
       var message = user.name + ':\n' + task.title;
       if (task.description) {
         message = message + '\n' + task.description;
       }
       message = message + '\nReply with update on job';
       cb(null, message);
     });
   },

   calculateLastUpdate: function(taskID, cb) {
     Task.findOne({id: taskID}).exec(function (err, task) {
       Message.find({forTask: taskID}).sort({createdAt: 'desc'}).exec(function (err, messages) {
         if (err) {
           cb(err);
         }
         var lastUpdate = new Date(0);
         var lastMessage = null;
         if (messages) {
           var lastMessage = messages[0].id;
           for (var i = 0; i < messages.length; i++) {
             if (messages[i].sentBy == task.assignedTo) {
               lastUpdate = messages[i].createdAt;
               break;
             }
           }
           var messageObj = {
             lastUpdate: lastUpdate,
             lastMessage: lastMessage
           };
           Task.update({id: taskID}, messageObj, function (err, task) {
            cb();
           }); 
         } else {
           cb();
         }
       });
     });
   },
};

