/**
 * BadgeController
 */
module.exports = {
  getBadgeCount: function(req, res) {
    var userID = req.session.User.id;
    Task.find({assignedBy: userID, status: 'open'}).exec(function (err, tasks) {
      var badgeCount = 0;
      for (var i = 0; i < tasks.length; i++) {
        badgeCount = badgeCount + tasks[i].updateCount;
      }
      res.json({'count': badgeCount});
    });
  }
}
