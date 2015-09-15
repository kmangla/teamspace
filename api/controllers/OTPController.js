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
        res.send(err);
        return;
      }
      OTP.update({phoneNumber: otpObj.phoneNumber}, otpObj, function (err, otpUpdated) {
        if (err) {
          res.send(err);
          return;
        }
        var country = PhoneNumberToCountry.getCountry(otpObj.phoneNumber);
        PushToken.find({country: country, appID: '1'}, function (err, tokens) {
          SendGCMMessage.sendGCMMessage(tokens[0], [{phone: otpObj.phoneNumber, message: 'TeamSpaceOTP:' + otpObj.OTP}], function (err) {});
        });
        res.send(200);
      });
    });
  },

  verify: function(req, res) {
    console.log('verify called');
    var phoneNumber = req.param('phoneNumber').trim();
    if (!(phoneNumber.substring(0,1) == '+')) {
      phoneNumber = '+' + phoneNumber;
    }
    OTP.findOne({phoneNumber: phoneNumber}, function (err, otp){
      if (err) {
        console.log(err);
        res.send(err);
        return;
      } 
      if (!otp) {
        console.log('No OTP generated');
        res.send('No OTP generated');
        return;
      }
      var otpCheck = otp.passOTPVerification(req.param('otp'), phoneNumber);
      if (!otpCheck) {
        console.log('OTP Verification Failed');
        res.send('OTP Verification Failed');
        return;
      }
      var name = req.param('name') ? req.param('name') : 'User';
      var userObj = {
        name: name,
        phone: phoneNumber,
        accountType: 'accountOwner', 
      };
      User.findOrCreate({phone: phoneNumber}, userObj, function (err, user) {
        if (err) {
          console.log(err);
          res.send(err);
          return;
        }
        console.log(user);
        var reply = [];
        reply[0] = {
          userID: user.id,
          key: user.id,
        };
        console.log(reply);
        res.send(reply);
      });
    });
  },
}
