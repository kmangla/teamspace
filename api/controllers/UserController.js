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
        console.log(err);
        return res.send(err);
      }

      user.online = true;
      user.accountType = 'accountOwner';
      user.designation = 'Saheb'
      user.save(function(err, user) {
        if (err) return next(err);
      StatsService.sendStats("user.create_count", 1);
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
        console.log(err);
        return res.send(err);
      }
      employee.manager = req.session.User.id;
      employee.online = true;
      employee.accountType = 'employee';
      employee.save(function(err, employee) {
        if (err) return res.send(err)
	    StatsService.sendStats("employee.create_count", 1);
      res.send(employee.id); 
      });
    });
  },

  listEmployee: function (req, res) {
    User.find({manager: req.session.User.id, accountType: 'employee', accountStatus: 'active'}).exec(function(err, employees) {
      if(err) return res.send(err);
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
      if (err) return res.send(err);
      res.json(employee);
    });
  },

  deleteEmployee: function (req, res) {
    User.update({id: req.params.id}, {accountStatus: 'deleted'}).exec(function(err, employee) {
      if (err) return res.send(err);
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

