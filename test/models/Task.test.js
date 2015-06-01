var assert = require('assert');
var sinon = require('sinon');

describe.only('TaskModel', function() {
  describe('Test user task count updates', function() {
    var data = {
      title: 'test',
      status: 'open',
      frequency: 86400,
      assignedTo: 'id1',
      assignedBy: 'id2'
    };

    var task;

    it ('On creation', function (done) {
      var stub = sinon.stub(sails.models.user, 'updateTaskCount', function (user, count, cb) {
        cb();
      });
      Task.create(data, function (err, task1) {
        task = task1;
        assert(stub.calledWith('id1', 1));
        done();
      });
    });

    it ('On destroy', function (done) {
      Task.create(data, function (err, task1) {
        task = task1;
        var stub = sinon.stub(sails.models.user, 'updateTaskCount', function (user, count, cb) {
          cb();
        });
        Task.destroy({id: task.id}, function (err, task) {
          assert(stub.calledWith('id1', -1));
          done();
        });
      });
    });

    it ('On update', function (done) {
      Task.create(data, function (err, task1) {
        task = task1;
        var stub = sinon.stub(sails.models.user, 'updateTaskCount', function (user, count, cb) {
          cb();
        });
        Task.update({id: task.id}, {id: task.id, status: 'closed'}, function (err, task) {
          assert(stub.calledWith('id1', -1));
          done();
        });
      });
    });

    it ('On non closing update', function (done) {
      Task.create(data, function (err, task1) {
        task = task1;
        var stub = sinon.stub(sails.models.user, 'updateTaskCount', function (user, count, cb) {
          cb();
        });
        Task.update({id: task.id}, {id: task.id, title: 'test2'}, function (err, task) {
          assert(!stub.calledOn());
          done();
        });
      });
    });

    afterEach(function (done) {
      sails.models.user.updateTaskCount.restore();
      Task.destroy({id: task.id}, function (err, task) {done()});
    });
  });
  describe('Test setting last update', function() {
    var data = {
      title: 'test',
      status: 'open',
      frequency: 86400,
      assignedTo: 'id1',
      assignedBy: 'id2'
    };
    var messageData1 = {
      description: 'test message',
      sentBy: 'id2',
      createdAt: new Date(1433137252000)
    };
    var messageData2 = {
      description: 'test message 2',
      sentBy: 'id1',
      createdAt: new Date(1433137200000)
    };
    var messageData3 = {
      description: 'test message 3',
      sentBy: 'id1',
      createdAt: new Date(1433137000000)
    };
    var messagesCreated;
    beforeEach(function (done) {
      Task.create(data, function (err, task) {
        var taskID = task.id;
        messageData1.forTask = taskID;
        messageData2.forTask = taskID;
        messageData3.forTask = taskID;
        Message.create([messageData1,messageData2,messageData3], function (err, messages) {
          messagesCreated = messages;
          done();
        });
      });
    });

    it ('latest message updated', function (done) {
      Task.calculateLastUpdate(messageData1.forTask, function () {
        Task.findOne({id: messageData1.forTask}).populate('lastMessage').exec(function (err, task) {
          assert.equal(task.lastUpdate.getTime(), messageData2.createdAt.getTime());
          assert.equal(task.lastMessage.description, messageData1.description);
          done();
        });
      });
    });

    afterEach(function (done) {
      Task.destroy({id: messageData1.forTask}, function (err, task) {});
      Message.destroy({id: messagesCreated[0].id}, function (err, messages) {});
      Message.destroy({id: messagesCreated[1].id}, function (err, messages) {});
      done();
    });
  });
});
