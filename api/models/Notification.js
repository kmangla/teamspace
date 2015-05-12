/**
* Notification.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

  attributes: {
    task: {
      model: 'Task',
    },
 
    user: {
      model: 'User',
    },

    timeQueued: {
      type: 'datetime',
    },

    message: {
      type: 'string',
      size: 1000
    },
  },
}
