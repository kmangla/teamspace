/**
* Digest.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

  attributes: {
    user: {
      model: 'User',
      required: true,
      unique: true
    },

    timeSent: {
      type: 'datetime',
      required: true
    },

    type: {
  		type: 'string',
  		enum: ['new_employee', 'new_task', 'task_update']
    }
  },
}
