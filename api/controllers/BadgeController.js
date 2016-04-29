/**
 * BadgeController
 */
module.exports = {
  getBadgeCount: function(req, res) {
    var userID = req.session.User.id;
    Memcache.get('badgeCount_' + userID, function (count) {
      if (count != null) {
        res.json({'count': count.toString()});
      }
      Task.find({assignedBy: userID, status: 'open'}).exec(function (err, tasks) {
        var badgeCount = 0;
        for (var i = 0; i < tasks.length; i++) {
          badgeCount = badgeCount + tasks[i].updateCount;
        }
        Memcache.set('badgeCount_' + userID, badgeCount);
        res.json({'count': badgeCount});
      });
    })
  }
}
