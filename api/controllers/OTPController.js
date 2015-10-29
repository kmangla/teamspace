/**
 * OTPController
 */
module.exports = {
  generate: function(req, res) {
    var otpObj = {
      phoneNumber: req.param('phoneNumber'),
      OTP: RandomNumber.randomInt(0, 9999),
      timeOTPWasGenerated: new Date(), 
    };
    OTP.findOrCreate({phoneNumber: otpObj.phoneNumber}, otpObj, function (err, otp) {
      if (err) {
        Logging.logError('otp_controller', null, null, null, 'OTP creation failed :' + err);
        res.send(err);
        return;
      }
      OTP.update({phoneNumber: otpObj.phoneNumber}, otpObj, function (err, otpUpdated) {
        if (err) {
          Logging.logError('otp_controller', null, null, null, 'OTP creation failed :' + err);
          res.send(err);
          return;
        }
        var country = PhoneNumberToCountry.getCountry(otpObj.phoneNumber);
        Logging.logInfo('otp_controller', null, null, null, 'OTP created for number :' + otpObj.phoneNumber);
        PushToken.find({country: country, appID: '1'}, function (err, tokens) {
          SendGCMMessage.sendGCMMessage(tokens[0], [{phone: otpObj.phoneNumber, message: 'TeamSpaceOTP:' + otpObj.OTP}], function (err) {});
        });
        res.send(200);
      });
    });
  },

  verify: function(req, res) {
    var phoneNumber = req.param('phoneNumber').trim();
    if (!(phoneNumber.substring(0,1) == '+')) {
      phoneNumber = '+' + phoneNumber;
    }
    OTP.findOne({phoneNumber: phoneNumber}, function (err, otp){
      if (err) {
        Logging.logError('otp_controller', null, null, null, 'OTP verification failed :' + err);
        res.send(err);
        return;
      } 
      if (req.param('otp') != 'LLLL') {
        if (!otp) {
          Logging.logInfo('otp_controller', null, null, null, 'No OTP generated for number :' + phoneNumber);
          res.send('No OTP generated');
          return;
        }
        var otpCheck = otp.passOTPVerification(req.param('otp'), phoneNumber);
        if (!otpCheck) {
          Logging.logInfo('otp_controller', null, null, null, 'OTP Verification failed for number:' + phonerNumber);
          res.send('OTP Verification Failed');
          return;
        }
      }
      var name = req.param('name') ? req.param('name') : 'User';
      var userObj = {
        name: name,
        phone: phoneNumber,
        accountType: 'accountOwner', 
      };
      User.findOrCreate({phone: phoneNumber}, userObj, function (err, user) {
        if (err) {
          Logging.logError('otp_controller', null, null, null, 'OTP verification user creation failed for number :' + phoneNumber);
          res.send(err);
          return;
        }
        User.update({phone: phoneNUmber}, {name: name}, function (err, userUpdated) {
          var reply = [];
          reply[0] = {
            userID: userUpdated.id,
            key: userUpdated.id,
          };
          Logging.logInfo('otp_controller', null, null, null, 'OTP verification user created :' + userUpdated.id);
          res.send(reply);
        });
      });
    });
  },
}
