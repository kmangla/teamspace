/**
 * Allow a logged-in user to see, edit and update her own profile
 * Allow admins to see everyone
 */

module.exports = function(req, res, next) {
	var employeeID = req.param('employeeID') || req.params.id;
  //console.log(employeeID);
  //TODO (pratyus): is user to emp lookup faster?
	User.findOne({manager: req.session.User.id, id: employeeID}).exec(function(err, employee) {
      if(err) return res.send(err);
      if (employee) {
      	next();
    	}
    	else {
        next();
     	  //res.send(403);
    	}
    });
	};
