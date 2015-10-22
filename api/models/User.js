/**
* User.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

	schema: true,

  attributes: {

  	accountType: {
  		type: 'string',
  		enum: ['accountOwner', 'employee', 'manager', 'admin'],
  		defaultsTo: 'accountOwner',
  		required: true
  	},

  	name: {
  		type: 'string',
  		required: true
  	},

  	company: {
      type: 'string'
    },

    employee: {
      collection: 'user',
      via: 'manager'
    },

    designation: {
    	type: 'string',
    },
    
    online: {
      type: 'boolean',
      defaultsTo: false
    },
  	
  	email: {
  		type: 'string',
  		email: true,
  	},

  	phone: {
      type: 'string',
      required: true,
      unique: true
    },

    manager: {
    	model: 'user'
    },

    priorityTask: {
        model: 'task'
    },

    password: {
      type: 'string'
    },

    // updates since manager last viewed
    updateCount: {
    	type: 'integer',
    	defaultsTo: 0
    },

    taskCount: {
      type: 'integer',
      defaultsTo: 0
    },

    accountStatus: {
    	type: 'string',
    	enum: ['active', 'deleted', 'blocked'],
  		defaultsTo: 'active',
  		required: true

    },

    messages: {
      collection: 'message',
      via: 'sentBy'
    },
 
    // Push token to send messages to the employee
    token: {
      model: 'PushToken',
    },

    toJSON: function() {
      var obj = this.toObject();
      delete obj.password;
      delete obj.accountStatus;
      return obj;
    },

    /**
     * Temporary function to fix two timezones for our users. In the future we will
     * need the client to send this down.
     */
    getTZ: function() {
      var country = PhoneNumberToCountry.getCountry(this.phone);
      if (country == 'US') {
        return 'America/Los_Angeles';
      }
      return 'Asia/Colombo';
    }
  },

    beforeCreate: function(user, cb) {
      /*
      bcrypt.genSalt(10, function(err, salt) {
          bcrypt.hash(user.password, salt, function(err, hash) {
            if(err) {
                console.log(err);
                cb(err);
            } else {
                user.password = hash;
                console.log(hash);
                cb(null, user);
            }
          });
      });
      */
      cb();
  },
 
  afterCreate: function(user, cb) {
    var userStatusObj = {};
    userStatusObj.user = user.id;
    UserStatus.create(userStatusObj, function (err, userStatus) {
      UserGlobalStatus.create({user: user.id, replyPending: false}, function (err, userGlobalStatus) {
        cb();
      });
    });
  },

  afterDestroy: function (user, cb) {
    UserStatus.destroy({user: user.id}, function (err, status) {
      if (err) {
      }
      cb();
    });
  },

  updateTaskCount: function(id, inc, cb) {
    User.findOne({id: id}, function (err, user){
      if (err) {
        cb(err);
        return;
      }
      if (!user) {
        return;
      }
      if (!user.taskCount) {
        user.taskCount = 0;
      }
      user.taskCount = user.taskCount + inc;
      User.update({id: id}, {taskCount: user.taskCount}, function(err, userUpdated) {
        if (err) {
          cb(err);
          return; 
        }
        cb();
      });
    });
  },

  fixTaskCount: function(id, cb) {
    Task.count().where({status: 'open', assignedTo: id}).exec(function (err, taskCount) {
      if (err) {
        cb(err);
        return; 
      }
      User.update({id: id}, {taskCount: taskCount}).exec(function (err, user) {
        if (err) {
          cb(err);
          return; 
        }
        cb();
      });
    });
  },

  isContactable: function(user, cb) {
    var moment = require('moment-timezone');
    if (user.accountType == 'accountOwner') {
      cb(false);
      return;
    }
    if (!user.manager) {
      cb(false);
      return;
    }
    var date = moment(Util.getDateObject()).tz(user.getTZ());
    cb((date.hour() >= 9) && (date.hour() <= 19) && (date.day() != 0) && !(user.phone.indexOf('Dummy') == 0));
  },
};

