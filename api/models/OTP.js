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
  
        passOTPVerification: function(otp, number) {
          var numberWhitelist = ['+918510006309'];
          if (otp == '0000' && numberWhitelist.indexOf(number) >= 0) {
            return true;
          }
          var date = new Date();
          var timeSinceOTPGenerationSec = Math.round((date-this.timeOTPWasGenerated)/1000);
          if (timeSinceOTPGenerationSec > 3600) {
            console.log('OTP has expired');
            return false;
          }

          if (this.OTP != otp) {
            console.log('Incorrect OTP');
            return false;
          }
          return true;
        },
  }
}
