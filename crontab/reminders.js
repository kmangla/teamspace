module.exports = {
  run : function(){
    User.find().populate('manager').exec(function(err, users) {
      for (var i = 0; i < users.length;i++) {
        TaskReminders.run(users[i]);
      } 
    })
  }
};

