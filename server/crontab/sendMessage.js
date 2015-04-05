module.exports = {
  run : function() {
    SMSService.receiveSMS('+918510006309', 'test', function (err) {
      console.log(err);
    });
  }
};

