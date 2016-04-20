module.exports = {
  getCountry: function (number) {
    if (number.substring(0,2) == '+1') {
      return 'US';
    } else {
      return 'IN';
    }
  },
}
