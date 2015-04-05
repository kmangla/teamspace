/**
 * Auth via Phone number
 *
 * @module      :: Policy
 * @description :: Simple policy to allow any authenticated user
 *                 Assumes that your login action in one of your controllers sets `req.session.authenticated = true;`
 * @docs        :: http://sailsjs.org/#!documentation/policies
 *
 */
module.exports = function(req, res, next) {
   var key = HeaderParser.parse(req.headers.teamspaceheader);
   User.findOne({id: key}).exec(function(err, user) {
     if(err) return res.send(err);
     if (user) {
        req.session.User = user;
       next();
     } else {
       res.send(403);
     }
   });
};

