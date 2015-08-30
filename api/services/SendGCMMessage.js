module.exports = {
  sendGCMMessage: function(token, messages, cb) {
    var gcm = require('node-gcm');
    var dataObj = [];
    
    for (var i = 0; i < messages.length; i++) {
      dataObj[i] = {
            'phone': messages[i].phone,
            'message': messages[i].message,
      };
    }
    var message = new gcm.Message({
      data: {'push' : dataObj},
      time_to_live: 3 * 3600
    });
   
    var sender = new gcm.Sender('AIzaSyBSBLLF9lHR9CbilhrMeWjhli8nd6cX45c');
    var registrationIds = [token.regID];
 
   
    StatsService.sendStats('sms.send_sms_cnt', messages.length);
    sender.send(message, registrationIds, function (err, result) {
      if (err) {
        cb(err);
      } else {
        cb();
      }
    });
  }
}
