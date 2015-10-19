module.exports = {
  logInfo : function(id, employerID, employeeID, taskID, message) {
    sails.log.info('%s\t%s\t%s\t%s\t%s', id, employerID, employerID, taskID, message);
  },

  logError : function(id, employerID, employeeID, taskID, message) {
    sails.log.error('%s\t%s\t%s\t%s\t%s', id, employerID, employerID, taskID, message);
  }
}
