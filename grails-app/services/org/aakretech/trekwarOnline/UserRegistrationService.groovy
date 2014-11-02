package org.aakretech.trekwarOnline

import java.security.SecureRandom
import grails.plugin.mail.MailService

class UserRegistrationService {
    def MailService mailService
    def grailsApplication

    private static SecureRandom prng = new SecureRandom()

    def boolean registerUser(User user) {
        def now = new Date()
        user.accountRegistrationDate = now
        user.salt = generateCode(48)

        if(user.validate()) {
            user.password = hashPasswordWithSalt(user.username, user.password)

            if(user.save(flush:  true)) {
                UserVerification verification = new UserVerification()
                verification.user = user
                verification.code = generateCode(40)
                verification.codeGeneratedDate = now
                println("trying to send email to " + user.email)

                mailService.sendMail {
                    to user.email
                    from "trekwaronline@gmail.com"
                    subject "Trekwar Online account registration"
                    html "<h2>Account Registration</h2>" +
                            "Thank you for registering for a Trekwar Online account " + user.username +
                            "<br/>before you can log in and use your account you have to click the link below to verify your email address" +
                            "<br/><br/>" +
                            "<b>Code: </b> " + verification.code +
                            "<br/><br/>" +
                            "<a href=\"http://www.trekwar.org/registration/validate?code=" + verification.code + "\">Click here to verify account</a>"
                }

                println("User verification code made: " + verification.code)
                return verification.save(flush: true)
            }
            else {
                return false
            }
        }
        else {
            return false
        }
    }

    def boolean validateUser(String code) {
        def verification = UserVerification.findByCode(code)
        if(verification != null) {
            verification.user.emailVerifiedDate = new Date()
            println("======== Set email verified date for " + verification.user.username)
            verification.delete()
            return true
        }
        return false
    }

    def boolean isUsernameAvailable(String username) {
        return (User.findByUsername(username) == null)
    }


    def static String hashPasswordWithSalt(String username, String password) {
        (username + password).encodeAsMD5()
    }

    def static String generateCode(int codeLength) {
        String keyset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        StringBuilder buffer = new StringBuilder()
        int index = 0
        while( index++ < codeLength) {
            buffer.append(keyset.charAt(prng.nextInt(keyset.length())))
        }
        return buffer.toString()
    }
}