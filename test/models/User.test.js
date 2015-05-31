var assert = require('assert');
var sinon = require('sinon');

describe.only('UserModel', function() {
  describe('Test creation', function() {
    var data = {
      accountType: 'accountOwner',
      name: 'Test',
      phone: '+919999999999',
    };
 

    beforeEach(function(done){
      User.destroy({phone: '+919999999999'}, function (err, users) {
        done();
      });
    });

    it('Creating multiple users with same number fails ', function (done) {
      User.create(data, function (err, user) {
        assert(user, err);
        User.create(data, function (err, user) {
          assert(err, 'No error thrown');
          done();
        });
      });
    });

    it ('Check user status object is created', function (done) {
      User.create(data, function (err, user) {
        UserStatus.findOne({user: user.id}, function (err, status) {
          assert(status, 'User status was created');
          User.destroy({id: user.id}, function (err, userDel) {
            UserStatus.findOne({user: userDel.id}, function (err, statusDel) {
              assert.equal(statusDel, null, 'deleting failed');
              done();
            });
          });
        });
      });
    });
   
    afterEach(function(done){
      User.destroy({phone: '+919999999999'}, function (err, users) {
        done();
      })
    });
  });

  describe('Test update task count', function() {
    var data = {
      accountType: 'accountOwner',
      name: 'Test',
      phone: '+919999999999',
    };
    
    var user;
    before(function(done){
      User.destroy({phone: '+919999999999'}, function (err, users) {
        User.create(data, function (err, user1) {
          user = user1;
          done();
        });
      });
    });
   
    it('Check incrementing count', function (done) {
      User.updateTaskCount(user.id, 1, function (err) {
        User.findOne({id: user.id}, function (err, user2) {
          assert.equal(user2.taskCount, 1, 'task Count not incremented');
          done();
        });
      });
    });

    it('Check decrementing count', function (done) {
      User.updateTaskCount(user.id, -1, function (err) {
        User.findOne({id: user.id}, function (err, user3) {
          assert.equal(user3.taskCount, 0, 'task Count not decremented');
          done();
        });
      });
    });

    after(function(done){
      User.destroy({phone: '+919999999999'}, function (err, users) {
        done();
      });
    });
  });

  describe('Test is contactable', function () {
    describe('Test account owner', function () {
      var data = {
        accountType: 'accountOwner',
        name: 'Test',
        phone: '+919999999999',
      };
      var user;

      before(function(done) {
        User.destroy({phone: '+919999999999'}, function (err, users) {
          User.create(data, function (err, user1) {
            user = user1;
            done();
          });
        });
      });

      it ('account owners are not contactable', function (done) {
        User.isContactable(user, function (result) {
          assert.equal(result, false);
          done();
        });
      });

      after(function(done) {
        User.destroy({phone: '+919999999999'}, function (err, users) {
          done();
        });
      });
    });

    describe('Test for IN employee', function () {
      var data = {
        accountType: 'employee',
        name: 'Test',
        phone: '+919999999999',
      };
      var user;

      before(function(done) {
        User.destroy({phone: '+919999999999'}, function (err, users) {
          User.create(data, function (err, user1) {
            user = user1;
            done();
          });
        });
      });

      it ('Not contactable on Sunday', function (done) {
        sinon.stub(sails.services.util, 'getDateObject', function () {return new Date(1433068024000)});
        User.isContactable(user, function (result) {
          assert.equal(result, false);
          done();
        });
        sails.services.util.getDateObject.restore();
      });

      it ('contactable from 9am - 9pm', function (done) {
        sinon.stub(sails.services.util, 'getDateObject', function () {return new Date(1432980000000)});
        User.isContactable(user, function (result) {
          assert.equal(result, true);
          done();
        });
        sails.services.util.getDateObject.restore();
      });

      it ('not contactable from 9pm - 9am', function (done) {
        sinon.stub(sails.services.util, 'getDateObject', function () {return new Date(1433016000000)});
        User.isContactable(user, function (result) {
          assert.equal(result, false);
          done();
        });
        sails.services.util.getDateObject.restore();
      });

      after(function(done) {
        User.destroy({phone: '+919999999999'}, function (err, users) {
          done();
        });
      });
    });

    describe('Test for US employee', function () {
      var data = {
        accountType: 'employee',
        name: 'Test',
        phone: '+19999999999',
      };
      var user;

      before(function(done) {
        User.destroy({phone: '+19999999999'}, function (err, users) {
          User.create(data, function (err, user1) {
            user = user1;
            done();
          });
        });
      });

      it ('not contactable from 9pm - 9am', function (done) {
        sinon.stub(sails.services.util, 'getDateObject', function () {return new Date(1432980000000)});
        User.isContactable(user, function (result) {
          assert.equal(result, false);
          done();
        });
        sails.services.util.getDateObject.restore();
      });

      after(function(done) {
        User.destroy({phone: '+19999999999'}, function (err, users) {
          done();
        });
      });
    });
  });
});
