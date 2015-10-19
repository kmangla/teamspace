/**
 * UserController
 *
 * @description :: Server-side logic for managing users
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {
	create: function(req, res) {

    var userObj = {
      name: req.param('name'),
      company: req.param('company'),
      email: req.param('email'),
      phone: req.param('phone'),
      password: req.param('password')
    }

    // Create a User with the params sent from  
    User.create(userObj, function (err, user) {

      if (err) {
        Logging.logError('user_controller', null, null, null, 'User creation failed ' +  err);
        return res.send(err);
      }

      user.online = true;
      user.accountType = 'accountOwner';
      user.designation = 'Saheb'
      user.save(function(err, user) {
        if (err) {
          Logging.logError('user_controller', user.id, null, null, 'User creation failed ' +  err);
          return next(err);
        }
        StatsService.sendStats("user.create_count", 1);
        Logging.logInfo('user_controller', user.id, null, null, 'User creation succeeded');
  			res.json(user);
      });
    });
  },

  createEmployee: function(req, res) {

    var employeeObj = {
      name: req.param('name'),
      company: req.param('company'),
      phone: req.param('phone'),
      designation: req.param('designation'),
    }

    // Create a Employee with the params sent from  
    User.create(employeeObj, function (err, employee) {
      if (err) {
        Logging.logError('user_controller', null, null, null, 'Employee creation failed ' +  err);
        return res.serverError(err);
      }
      employee.manager = req.session.User.id;
      employee.online = true;
      employee.accountType = 'employee';
      employee.save(function(err, employee) {
        if (err) {
          Logging.logError('user_controller', null, employee.id, null, 'Employee creation failed ' +  err);
          return res.serverError(err);
        }
        StatsService.sendStats("employee.create_count", 1);
        PushToken.findOrAssignToken(employee, function (err, token) {
          if (err) {
            Logging.logError('user_controller', null, employee.id, null, 'Employee creation failed ' +  err);
            return res.serverError(err);
          }
          if (token) {
            employee.pairedNumber = token.deviceID;
          }
          Logging.logInfo('user_controller', null, employee.id, null, 'Employee creation succeeded');
          return res.send(employee); 
        });
      });
    });
  },

  listEmployee: function (req, res) {
    User.find({manager: req.session.User.id, accountType: 'employee', accountStatus: 'active'}).exec(function(err, employees) {
      if(err) return res.serverError(err);
      return res.json(employees);
    });
  },

  listEmployeeDetail: function (req, res) {
    //reset update count
  },

  updateEmployee: function (req, res) {
    var employeeUpdateObj = {}; 
    if (req.param('phone')) {
      employeeUpdateObj.phone = req.param('phone');
    }
    if (req.param('name')) {
      employeeUpdateObj.name = req.param('name');
    }
    if (req.param('designation')) {
      employeeUpdateObj.designation = req.param('designation');
    } 
    User.update({id: req.params.id}, employeeUpdateObj).exec(function(err, employee) {
      if (err) return res.serverError(err);
      res.json(employee);
    });
  },

  deleteEmployee: function (req, res) {
    User.update({id: req.params.id}, {accountStatus: 'deleted', phone: Date.now()}).exec(function(err, employee) {
      if (err) return res.serverError(err);
      Task.update({assignedTo: req.params.id}, {assignedTo: req.session.User.id}).exec(function(err) {
        if (err) return res.serverError(err);
      });
      res.json(employee);
    });
  },

  updateUser: function (req, res) {
    // set hide flag
  },

  deleteUser: function (req, res) {
    // set hide flag
  },
};

