/**
* Message.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

  attributes: {
  	description: {
      	  type: 'string',
    	  required: true,
    	  size: 200
  	},

  	forTask: {
  		model: 'task',
  		required: true
  	},

  	sentBy: { 
  		model: 'user',
  		required: true
  	},
     
        notifSent: {
          type: 'boolean',
          defaultsTo: false
        },

        systemGenerated: {
          type: 'boolean',
          defaultsTo: false
        },
  },

   afterCreate: function(message, cb) {
     Task.findOne({id: message.forTask}).populate('assignedTo').populate('assignedBy').exec(function(err, task) {
       if(err) {
         return;
       }
       if (!task || !task.assignedTo || !task.assignedBy) {
         return;
       }
       if ((message.sentBy == task.assignedBy.id) && !message.systemGenerated) {
         Message.enqueueNotification(message, task, function (shouldForceReminder) {
           var forceReminder = task.forceReminder || shouldForceReminder;
           var forceReminderTime = task.forceReminderTime;
           if (shouldForceReminder) {
             forceReminderTime = new Date();
           }
           Task.update({id: message.forTask}, {forceReminder: forceReminder, forceReminderTime: forceReminderTime, lastMessage: message.id}).exec(function(err, updatedTask) {});
         });
       } else {
         var updateCount = task.updateCount;
         if (!message.systemGenerated) {
           updateCount = updateCount+1;
           Memcache.delete('badgeCount_' + task.assignedBy.id, function (err, success) {});
         }
         Task.update({id: message.forTask}, {updateCount: updateCount, forceReminder: false, lastMessage: message.id, lastUpdate: new Date()}).exec(function(err, updatedTask) {});
       }
       User.findOne({id: message.sentBy}).exec(function(err, employee) {
         if(err) {
           return;
         }
         var employeeUpdateCount = employee.updateCount;
         if (!message.systemGenerated && (message.sentBy != task.assignedBy.id)) {
           employeeUpdateCount = employeeUpdateCount+1;
         }
         User.update({id: message.sentBy}, {updateCount: employee.updateCount}).exec(function(err, updatedEmployee) {
         });
       });
     });
     Message.sendCreateNotification(message);
     cb();
   },

   enqueueNotification: function(message, task, cb) {
     var notifMessage = task.assignedBy.name + ' has said:\n' + message.description;
     UserStatus.findOne({user: task.assignedTo.id}, function (err, status) {
       if (status.taskSent == task.id) { 
         PushToken.findOrAssignToken(task.assignedTo, function (err, token) {
           var user = task.assignedTo;
           UserStatus.update({id: status.id}, {timeMessageSent: new Date()}).exec(function (err, userStatusUpdate) {});
           Logging.logInfo('notification', task.assignedBy.id, task.assignedTo.id, task.id, 'Notification created for : ' + notifMessage); 
           SMS.create({phone: user.phone, task: task.id, forMessage: message.id, timeQueued: new Date(), tokenID: token, message: notifMessage}, function (err, reminder) {});
           cb(false);
       });
      } else {
        Logging.logInfo('notification', task.assignedBy.id, task.assignedTo.id, task.id, 'Notification queued for : ' + notifMessage); 
        Notification.create({user: task.assignedBy.id, forMessage: message.id, task: task.id, timeQueued: new Date(), message: notifMessage}, function (err, notification) {
          cb(true);
        });
      }
     });
   },
 
   sendCreateNotification: function(message) {
     Message.findOne({id: message.id}).populate('forTask').exec(function (err, message) {
       if (!message.forTask) {
         return;
       }
       if (message.sentBy == message.forTask.assignedBy) {
         return;
       }
       if (message.systemGenerated) {
         return;
       }
       if (err) {
         return;
       }
       Logging.logInfo('notification', message.forTask.assignedBy, message.sentBy, message.forTask.id, 'Notification created for : ' + message.description); 
       SendNotification.sendNotification(message.forTask.assignedBy, message.sentBy, message.description, message.forTask.id, 'newMessage',
         function (err) {if (err) {console.log(err);}}
       );
     });
   }
};
