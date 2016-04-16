/**
* PushToken.js
*
*/

module.exports = {

  schema: true,

  attributes: {
    deviceID: {
      type: 'string',
      required: true,
    },
    
    regID: {
      type: 'string',
      required: true,
    },

    appID: {
      type: 'string',
      required: true,
    },

    userID: {
      model: 'user',
    },
   
    country: {
      type: 'string',
      enum: ['US', 'IN'],
    },
  },

  findOrAssignToken: function(user, cb) {
    if (!user.token) {
      PushToken.assignToken(user, cb);
    } else {
      PushToken.findOne({id: user.token}).exec(function (err, token) {
        if (err) {
          cb(err);
          return;
        }
        var country = PhoneNumberToCountry.getCountry(user.phone);   
        if (!token || (token.country != country)) {
          Logging.logInfo('push_token', user.id, null, null, 'Push token not assigned');
          PushToken.assignToken(user, cb);
          return;
        }
        cb(null, token);
      });
    }
  },

  assignToken: function (user, cb) {
    var country = PhoneNumberToCountry.getCountry(user.phone);   
    PushToken.find({appID: '1', country: country}).exec(function (err, tokens) {
      if (err) {
        cb(err);
        return;
      }
      if (!tokens.length) {
        Logging.logInfo('push_token', user.id, null, null, 'No tokens available for country :' + country);
        cb('No tokens available');
        return;
      }
      var randToken = tokens[Math.floor(Math.random() * tokens.length)];
      User.update({id: user.id}, {token: randToken.id}).exec(function (err, updateUser) {
        if (err) {
          cb(err);
          return; 
        }
        Logging.logInfo('push_token', user.id, null, null, 'Token ' + randToken.id + ' assigned for user');
        cb(null, randToken); 
      });
    });
  }
}
