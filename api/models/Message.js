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

        systemsGenerated: {
          type: 'boolean',
          defaultsTo: false
        },
  },

  
   afterCreate: function(message, cb) {
     Task.findOne({id: message.forTask}).populate('assignedTo').populate('assignedBy').exec(function(err, task) {
       if(err) {
         console.log(err);
         return;
       }
       if (!task || !task.assignedTo || !task.assignedBy) {
         console.log(err);
         return;
       }
       if ((message.sentBy == task.assignedBy.id) && !message.systemsGenerated) {
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
         updateCount = updateCount+1;
         Task.update({id: message.forTask}, {updateCount: updateCount, forceReminder: false, lastMessage: message.id, lastUpdate: new Date()}).exec(function(err, updatedTask) {});
       }
       User.findOne({id: message.sentBy}).exec(function(err, employee) {
         if(err) {
           console.log(err);
           return;
         }
         var employeeUpdateCount = employee.updateCount;
         if (!message.systemsGenerated && (message.sentBy != task.assignedBy.id)) {
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
           SMS.create({phone: user.phone, task: task.id, forMessage: message.id, timeQueued: new Date(), tokenID: token, message: notifMessage}, function (err, reminder) {});
           cb(false);
       });
      } else {
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
       if (message.systemsGenerated) {
         return;
       }
       if (err) {
         console.log(err);
         return;
       }
       SendNotification.sendNotification(message.forTask.assignedBy, message.sentBy, message.description, message.forTask.id, 'newMessage',
         function (err) {if (err) {console.log(err);}}
       );
     });
   }
};
