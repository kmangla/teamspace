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
  	}
  },

  
   afterCreate: function(message, cb) {
     Task.findOne({id: message.forTask}).exec(function(err, task) {
       if(err) {
         console.log(err);
         return;
       }
       var updateCount = task.updateCount;
       if (message.sentBy != task.assignedBy) {
         updateCount = updateCount+1;
       }
       Task.update({id: message.forTask}, {updateCount: updateCount, lastMessage: message.id, lastUpdate: new Date()}).exec(function(err, updatedTask) {
       });
       User.findOne({id: message.sentBy}).exec(function(err, employee) {
         if(err) {
           console.log(err);
           return;
         }
         var employeeUpdateCount = employee.updateCount;
         if (message.sentBy != task.assignedBy) {
           employeeUpdateCount = employeeUpdateCount+1;
         }
         User.update({id: message.sentBy}, {updateCount: employee.updateCount}).exec(function(err, updatedEmployee) {
         });
       });
     });
     Message.sendCreateNotification(message);
     cb();
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
