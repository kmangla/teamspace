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
          console.log(err);
          return res.send(err);
        }
        StatsService.sendStats("message.receive_count.type_sms", 1);
        return res.json(message);
      });
      return;
    }
    var systemsGenerated = false;
    if (req.param('systemsGenerated')) {
      systemsGenerated = true;
    }
    var messageObj = {
      description: req.param('description'),
      forTask: req.param('taskID'),
      sentBy: req.param('sentBy'),
      systemsGenerated: systemsGenerated
    }

    Message.create(messageObj, function (err, message) {
      StatsService.sendStats("message.receive_count.type_app", 1);
      if (err) {
        console.log(err);
        return res.send(err);
      }
      message.save(function(err, message) {
        if (err) return res.send(err);
      	return res.json(message); 
      });
    });
  },

  list: function (req, res) {
    var query = Message.find({forTask: req.param('taskID')});
    PrivacyService.message(query, ['sentBy'], function(err, messages) {
      if(err) return res.send(err);
      Task.update({id: req.param('taskID')}, {updateCount: 0}).exec(function(err, task) {
        if (err) return res.send(err);
      });
      MockMessage.createMockMessage(req.param('taskID'), function (err, message) {
        if (err) return res.send(err);
        if (!message) return res.json(messages);
        messages.push(message);
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

