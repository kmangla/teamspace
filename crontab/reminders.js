module.exports = {
  run : function() {
    var query = User.find();
    PrivacyService.user(query, ['manager'], function (err, users) {   
      for (var i = 0; i < users.length;i++) {
        TaskReminders.run(users[i]);
      } 
    });
  }
};
