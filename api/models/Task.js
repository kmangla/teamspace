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

   // Custom attribute methods

    taskPriority: function () {
      var dateNew = new Date(1);
      var date = new Date();
      var timeSinceLastUpdateSec = Math.round((date-this.lastUpdate)/1000);
      if (this.lastUpdate < dateNew) {
        timeSinceLastUpdateSec = Math.round((date-this.createdAt)/1000);
      }
      if (timeSinceLastUpdateSec < this.frequency) {
        return 0;
      }
      var delayTimeInSec = timeSinceLastUpdateSec - this.frequency;
      var maxDelay = 3 * 24 * 3600 ; // 3 days
      if (delayTimeInSec >  maxDelay) {
        return 100;
      }
      return Math.floor(delayTimeInSec/maxDelay * 100);
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
      //var timeSinceLastUpdateSec = Math.round((date-this.lastUpdate)/1000);
      var days = Util.daysSince(date, this.lastUpdate, user);
      console.log(days);
      if (days >= (this.frequency / 86400)) {
        return true;
      } else {
        return false;
      }
    },
  },

   beforeCreate: function(task, cb) {
     User.updateTaskCount(task.assignedTo, 1, function (err) {
       if (err) {
         console.log(err);
       }
       cb();
     });
   },

   beforeUpdate: function(task, cb) {
     Task.findOne({id: task.id}, function (err, originalTask) {
       if (err) {
         console.log(err);
         cb();
         return;
       }
       if (!originalTask) {
         console.log('No task exists');
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
             console.log(err); 
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
          console.log(err); 
        }
        cb();
      });
    } else {
      cb();
    }
  },
   
   reminderMessageAndNotifications: function (task, cb) {
     Notification.destroy({task: task.id}).exec(function (err, notifications) {
       console.log(notifications);
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

};

