module.exports = {
  run : function(){
    User.find({accountType: 'accountOwner', accountStatus: 'active'}).exec(function(err, users) {
      for (var i = 0; i < users.length;i++) {
        generateDigest.run(users[i]);
      } 
    })
  }
};
