module.exports = {
  makeTasksAndEmployees: function (user, cb) {
    var employees = [];
    employees[0] = {
      accountType: 'employee',
      name: 'Dummy Employee 1',
      phone: 'Dummy' + user.id + '17',
      manager: user.id
    };
    employees[1] = {
      accountType: 'employee',
      name: 'Dummy Employee 2',
      phone: 'Dummy' + user.id + '19',
      manager: user.id
    };
    User.create(employees, function (err, users) {
      if (err) {
        cb(err);
        return;
      }
      var tasks = [];
      tasks[0] = {
        title: 'Dummy Task 1',
        description: 'Dummy Task 1',
        status: 'open',
        frequency: 86400,
        assignedTo: users[0].id,
        assignedBy: user.id,
        lastUpdate: new Date(0)
      };
      tasks[1] = {
        title: 'Dummy Task 2',
        description: 'Dummy Task 2',
        status: 'open',
        frequency: 86400 * 7,
        assignedTo: users[1].id,
        assignedBy: user.id,
        lastUpdate: new Date(0)
      };
      Task.create(tasks, function (err, createdTasks) {
        if (err) {
          cb(err);
          return;
        }
        cb();
      });
    });
  },
}
