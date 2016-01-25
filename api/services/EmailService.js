module.exports = {
  sendMail: function (email, text) {
    var mailer = require("nodemailer");
    var smtpTransport = require('nodemailer-smtp-transport');
    var options = {
      service: "gmail",
      auth: {
        user: "teamspaceapp@gmail.com",
        pass: "teamspace2014"
      }
    };
    var smtpTransport = mailer.createTransport(smtpTransport(options));

    var mail  = {
      from: "teamspaceapp@gmail.com",
      to: email,
      subject: "TeamSpace Weekly Report",
      html: text,
    }

    smtpTransport.sendMail(mail, function (error, response) {
      if (error) {
        console.log(error);
      } else {
        console.log("Message sent: " + response.message);
      }
      smtpTransport.close();
    });
  }
}
