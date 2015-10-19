module.exports = {
  sendStats: function(counter, count) {
    var dgram = require('dgram');

    var message = new Buffer("0776b307-41da-4767-89f5-4d244dcd6d3e." + counter + " " + count + "\n");
    var client = dgram.createSocket("udp4");
    client.send(message, 0, message.length, 2003, "carbon.hostedgraphite.com", function(err, bytes) {
    client.close();
    });
  }
}
