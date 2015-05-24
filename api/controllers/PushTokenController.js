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

    PushToken.findOrCreate({deviceID: registerObj.deviceID, appID: registerObj.appID}, registerObj).exec(function (err, token) {
      if (err) {
        res.send(err);
      }
      PushToken.update({deviceID: registerObj.deviceID, appID: registerObj.appID}, updateObj).exec(function (err, token) {
        if (err) {
          res.send(err);
        }
        res.send(200);
      });
    });
  },
}
