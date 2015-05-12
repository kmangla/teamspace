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
  },

  
   afterCreate: function(message, cb) {
     Task.findOne({id: message.forTask}).populate('assignedTo').populate('assignedBy').exec(function(err, task) {
       if(err) {
         console.log(err);
         return;
       }
       if (message.sentBy == task.assignedBy.id) {
         Message.enqueueNotification(message, task);
       }
       var updateCount = task.updateCount;
       var forceReminder = task.forceReminder;
       if (message.sentBy != task.assignedBy.id) {
         updateCount = updateCount+1;
       } else {
         forceReminder = true;
       }
       Task.update({id: message.forTask}, {updateCount: updateCount, lastMessage: message.id, forceReminder: forceReminder, lastUpdate: new Date()}).exec(function(err, updatedTask) {
       });
       User.findOne({id: message.sentBy}).exec(function(err, employee) {
         if(err) {
           console.log(err);
           return;
         }
         var employeeUpdateCount = employee.updateCount;
         if (message.sentBy != task.assignedBy.id) {
           employeeUpdateCount = employeeUpdateCount+1;
         }
         User.update({id: message.sentBy}, {updateCount: employee.updateCount}).exec(function(err, updatedEmployee) {
         });
       });
     });
     Message.sendCreateNotification(message);
     cb();
   },

   enqueueNotification: function(message, task) {
     var notifMessage = task.assignedBy.name + ' has said:\n' + message.description;
     Notification.create({user: task.assignedBy.id, task: task.id, timeQueued: new Date(), message: notifMessage}, function (err, notification) {
     });
   },
 
   sendCreateNotification: function(message) {
     Message.findOne({id: message.id}).populate('forTask').exec(function (err, message) {
       if (message.sentBy == message.forTask.assignedBy) {
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
