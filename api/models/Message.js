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
     Task.update({id: message.forTask}, {id: message.forTask, lastMessage: message.id, lastUpdate: new Date()}, function (err, task) {
       if (err) {
         cb(err);
         return;
       }
       Message.sendCreateNotification(message);
       cb();
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
