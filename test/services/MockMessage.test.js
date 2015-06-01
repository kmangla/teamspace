var assert = require('assert');
var sinon = require('sinon');

describe.only('MockMessage', function() {
  describe('Test message creation', function() {
    var userData1 = {
      accountType: 'accountOwner',
      name: 'Test',
      phone: '+919999999999',
    };

    var userData2 = {
      accountType: 'employee',
      name: 'Test',
      phone: '+919999999998',
    };
 
    var taskData = {
      title: 'test',
      status: 'open',
      frequency: 86400,
      assignedTo: 'id1',
      assignedBy: 'id2',
      lastUpdate: new Date(0)
    };
    var user1ID, user2ID, taskID;

    beforeEach(function(done) {
      User.create(userData1, function (err, user1) {
        user1ID = user1.id;
        User.create(userData2, function (err, user2) {
          user2ID = user2.id;
          taskData.assignedTo = user1.id;
          taskData.assignedBy = user2.id;
          Task.create(taskData, function (err, task) {
            taskID = task.id;
            done();
          });
        });
      });
    });

    it ('Check reminder sent', function (done) {
      UserStatus.update({user: user1ID}, {replyPending: true, taskSent: taskID, timeFirstReminderSent: new Date()}, function (err, status) {
        MockMessage.createMockMessage(taskID, function (err, message) {
          assert.equal(message.description, 'Reminder Sent');
          done();
        });
      });
    });
   
    it ('Check reply pending', function (done) {
      UserStatus.update({user: user1ID}, {replyPending: false, taskSent: taskID, timeFirstReminderSent: new Date()}, function (err, status) {
        MockMessage.createMockMessage(taskID, function (err, message) {
          assert.equal(message.description, 'Reply Pending');
          done();
        });
      });
    });
   
    afterEach(function(done){
      User.destroy({id: user1ID}, function (err, user) {
        User.destroy({id: user2ID}, function (err, user) {
          Task.destroy({id: taskID}, function (err, task) {
            done();
          });
        });
      });
    });
  });
});

