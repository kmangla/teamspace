/**
 * MessageController
 *
 * @description :: Server-side logic for managing messages
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

  create: function(req, res, next) {
    if (!req.param('taskID')) {
      SMSService.receiveSMS(req.param('sentBy'), req.param('description'), function (err, message) {
        if (err) {
          Logging.LogError('message_controller', null, req.param('sentBy'), null, 'Message creation failed ' + err);
          return res.send(err);
        }
        StatsService.sendStats("message.receive_count.type_sms", 1);
        Logging.LogInfo('message_controller', null, message.sentBy, message.forTask, 'Message creation was successful');
        return res.json(message);
      });
      return;
    }
    var systemGenerated = false;
    if (Number(req.param('systemGenerated'))) {
      systemGenerated = true;
    }
    var messageObj = {
      description: req.param('description'),
      forTask: req.param('taskID'),
      sentBy: req.param('sentBy'),
      systemGenerated: systemGenerated
    }

    Message.create(messageObj, function (err, message) {
      StatsService.sendStats("message.receive_count.type_app", 1);
      if (err) {
        Logging.LogError('message_controller', null, req.param('sentBy'), req.param('taskID'), 'Message creation failed ' + err);
        return res.send(err);
      }
      Logging.LogInfo('message_controller', null, req.param('sentBy'), req.param('taskID'), 'Message creation was successful');
      message.save(function(err, message) {
        if (err) {
          Logging.LogError('message_controller', null, req.param('sentBy'), req.param('taskID'), 'Message creation failed ' + err);
          return res.send(err);
        }
      	return res.json(message); 
      });
    });
  },

  list: function (req, res) {
    var query = Message.find({forTask: req.param('taskID')});
    if (!req.param('taskID')) {
      return res.json({});
    }
    PrivacyService.message(query, ['sentBy'], function(err, messages) {
      if(err) return res.send(err);
      Task.update({id: req.param('taskID')}, {updateCount: 0}).exec(function(err, task) {
        if (err) return res.send(err);
      });
      Task.findOne({id: req.param('taskID')}).populate('assignedTo').populate('currentStatus').exec(function (err, task) {
        if (task.currentStatus.replyPending) {
          var message = MockMessage.createReminderSentMessage(task);
          messages.push(message);
        }
        return res.json(messages);
      });
    });
  },

  update: function (req, res) {
    var newTask = req.param('forTask');
    Message.update({id: req.params.id}, {forTask: newTask}, function (err, message) {
      if (err) return res.send(err);
      Task.calculateLastUpdate(newTask, function (err) {
        return res.json(message.id);
      })
    });
  },
};

