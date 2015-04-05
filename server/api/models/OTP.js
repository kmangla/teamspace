/**
* OTP.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

	schema: true,

  attributes: {
  	phoneNumber: {
  		type: 'string',
  		required: true,
                unique: true,
  	},
      
        OTP: {
                type: 'integer',
  		required: true
        },

        timeOTPWasGenerated: {
                type: 'datetime',
                required: true
        },
  }
}
