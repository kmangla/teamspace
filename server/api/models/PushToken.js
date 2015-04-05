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
        if (!token) {
          PushToken.assignToken(user, cb);
          return;
        }
        console.log('using ' + token);
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
        cb('No tokens available');
        return;
      }
      var randToken = tokens[Math.floor(Math.random() * tokens.length)];
      console.log(randToken);
      User.update({id: user.id}, {token: randToken.id}).exec(function (err, updateUser) {
        if (err) {
          cb(err);
          return; 
        }
        console.log('assigning ' + randToken);
        cb(null, randToken); 
      });
    });
  }
}
