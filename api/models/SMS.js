/**
* SMS.js
*
*/

module.exports = {

  schema: true,

  attributes: {
    task: {
      model: 'Task',
    },
 
    phone: {
      type: 'string',
    },

    timeQueued: {
      type: 'datetime',
    },

    message: {
      type: 'string',
      size: 1000
    },

    tokenID: {
      model: 'PushToken',
      required: true,
    },

    forMessage: {
      model: 'Message'
    },
  },
}
