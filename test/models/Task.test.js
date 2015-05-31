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
});
