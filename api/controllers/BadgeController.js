/**
 * BadgeController
 */
module.exports = {
  getBadgeCount: function(req, res) {
    Task.find({assignedBy: userID, status: 'open'}).exec(function (err, tasks) {
      var badgeCount = 0;
      for (var i = 0; i < tasks.length; i++) {
        badgeCount = badgeCount + tasks[i].updateCount;
      }
      res.json(badgeCount);
    });
  }
}
