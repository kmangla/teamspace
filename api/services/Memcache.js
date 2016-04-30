module.exports = {

  create: function () {
    var memjs = require('memjs');
    var mc = memjs.Client.create('pub-memcache-16695.us-east-1-2.1.ec2.garantiadata.com:16695', {
        username: 'memcached-app35149535',
        password: 'TeSmHT8yHBfbTpvk'
    });    
    return mc;
  },

  set: function (key, value, cb) {
    var client = Memcache.create();
    client.set(key, value);
    cb();
  },

  get: function (key, cb) {
    var client = Memcache.create();
    client.get(key, function (err, value, key) {
      if (value != null) {
        cb(value);
      } else {
        cb();
      }
    });
  },

  delete: function (key, cb) {
    var client = Memcache.create();
    client.delete(key, function (err, success) {
      cb();
    });
  }
}
