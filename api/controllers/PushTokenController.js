/**
 * PushTokenController
 */
module.exports = {
  register: function(req, res) {
    var registerObj = {
      regID: req.param('regID'),
      deviceID: req.param('deviceID'),
      appID: req.param('appID'),
    };
    var updateObj = {
      regID: req.param('regID'),
    };
    if (req.session.User && req.session.User.id) {
      registerObj.userID = req.session.User.id;
      updateObj.userID = req.session.User.id;
    }
    var country = PhoneNumberToCountry.getCountry(req.param('deviceID'));
    registerObj.country = country;
    updateObj.country = country;

    PushToken.findOrCreate({deviceID: registerObj.deviceID, appID: registerObj.appID}, registerObj).exec(function (err, token) {
      if (err) {
        Logging.LogError('token_controller', null, null, null, 'Token creation failed for ' + registerObj.deviceID + ':' + registerObj.appID + ' ' + err);
        res.send(err);
      }
      PushToken.update({deviceID: registerObj.deviceID, appID: registerObj.appID}, updateObj).exec(function (err, token) {
        if (err) {
          Logging.LogError('token_controller', null, null, null, 'Token creation failed for ' + registerObj.deviceID + ':' + registerObj.appID + ' ' + err);
          res.send(err);
        }
        Logging.LogInfo('token_controller', null, null, null, 'Token creation successful for ' + registerObj.deviceID + ':' + registerObj.appID);
        res.send(200);
      });
    });
  },
}
