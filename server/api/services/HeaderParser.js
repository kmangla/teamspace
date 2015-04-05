module.exports = {
  parse: function (header) {
    var arr = header.split(", ");
    var arrValues = [];
    for (var i = 0; i < 4; i++) {
      arrValues[i] = arr[i].split("=")[1];
    }
    return arrValues[1];
  }
}
