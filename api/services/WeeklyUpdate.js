module.exports = {
  generateReport: function (user, cb) {
    User.find({manager: user.id}).exec(function (err, users) {
      PushToken.findOrAssignToken(user, function (err, token) {
      var firstDate = new Date();
      firstDate.setHours(0,0,0,0);
      firstDate.setDate(firstDate.getDate() + 1);
      var secondDate = new Date();
      secondDate.setHours(0,0,0,0);
      secondDate.setDate(firstDate.getDate() - 6);
      var usersById = Util.extractMap(users, "id");
      Task.find().where({assignedBy: user.id, status: "closed", updatedAt: {'>': secondDate, '<': firstDate}}).exec(function (err, closedTasks) {
      Task.find().where({assignedBy: user.id, status: "open", lastUpdate: {'>': secondDate, '<': firstDate}}).exec(function (err, updatedTasks) {
      Task.find().where({assignedBy: user.id, status: "open", lastUpdate: {'<': secondDate}}).exec(function (err, pendingTasks) {
        var duePendingTasks = [];
        for (var i = 0; i < pendingTasks.length; i++) {
          if (usersById[pendingTasks[i].assignedTo] && pendingTasks[i].reminderIsDue(usersById[pendingTasks[i].assignedTo])) {
            duePendingTasks.push(pendingTasks[i]);
          }
        }
        var employeeClosedTaskList = Util.extractMapListBasic(closedTasks, "assignedTo");
        var employeeUpdatedTaskList = Util.extractMapListBasic(updatedTasks, "assignedTo");
        var employeePendingTaskList = Util.extractMapListBasic(duePendingTasks, "assignedTo");
        var messageToSend = WeeklyUpdate.createMessage(usersById, employeeClosedTaskList, employeeUpdatedTaskList, employeePendingTaskList);
        if (messageToSend == null) {
          return;
        }
        if (user.email == null) {
          return;
        }
        EmailService.sendMail(user.email, messageToSend);
      });
      });
      });
    });
    });
  },

  createMessage: function (employeeObjects, closedTasks, updatedTasks, pendingTasks) {
    var message = 'TeamSpace Weekly Report:';
    var employees = Object.keys(employeeObjects);
    var htmlReport = '<table border=\"1\" style=\"width:100%\">';
    htmlReport = htmlReport +
        '<tr>' +
            '<td>Name</td>' +
            '<td>Total Tasks Assigned</td>' +
            '<td>Completed Tasks</td>' +
            '<td>Tasks Replied To</td>' +
        '</tr>';
    for (var i = 0; i < employees.length; i++) {
      var employeeID = employees[i];
      var employee = employeeObjects[employeeID];
      var closedCount = 0;
      var updatedCount = 0;
      var pendingCount = 0;
      if (employeeID in closedTasks) {
        closedCount = closedTasks[employeeID].length;
      }
      if (employeeID in updatedTasks) {
        updatedCount = updatedTasks[employeeID].length;
      }
      if (employeeID in pendingTasks) {
        pendingCount = pendingTasks[employeeID].length;
      }
      if ((closedCount + updatedCount + pendingCount) == 0) {
        continue;
      }
      var totalCount = closedCount + updatedCount + pendingCount;
      message = message + '\n' + employee.name + ': ';
      message = message + closedCount + ' completed, ' + updatedCount + ' updated, ' + totalCount + ' tasks assigned';
      var color = '#ACFA58';
      if (pendingCount >= totalCount/2) {
        color = '#FE642E';
      }
      htmlReport = htmlReport +
        '<tr bgcolor=\"' + color + '\">' + 
            '<td>' + employee.name + '</td>' +
            '<td>' + totalCount + '</td>' +
            '<td>' + closedCount + '</td>' +
            '<td>' + updatedCount + '</td>' +
        '</tr>';
    }
    htmlReport = htmlReport +  '</table>';
    htmlReport = htmlReport + '</br></br>';
    return htmlReport;
  }
}
