module.exports = {
  getCountry: function (number) {
    if (number.substring(0,3) == '+91') {
      return 'IN';
    } else {
      return 'US';
    }
  },
}
