package childrenlab


import grails.transaction.Transactional

@Transactional
class UserService {

    def springSecurityService
    def ftpService

    def register(String email, String password){

        def user = User.findByEmail(email)
        def result
        try{

            Role role = Role.findByAuthority('ROLE_USER')

            if(user){
                //User exists
                result = [success: false, message: "The email already registered."]
            } else {
                user = new User(email: email, password: password).save(flush: true, failOnError: true)

                new UserRole(user: user, role: role).save(flush: true, failOnError: true)
                result = [success: true]
            }
        }catch(Exception e){
            log.error("Login or register error: ", e);
            result = [success: false, message: "The server incounter error stage, please try it again later."]
        }

        return result
    }

    def register(String email, String password, String phoneNumber, String firstName, String lastName, String zipCode){

        def user = User.findByEmail(email)

        Map result
        try{
//            Date birthdayDate = birthday ? new Date().parse("yyyy-MM-dd", birthday) : null
//            roleString = roleString ?: 'ROLE_USER'
            Role role = Role.findByAuthority('ROLE_USER')

            if(user){
                //User exists
                result = [success: false, message: "The email already registered."]
            } else {
                user = new User(email: email, password: password, phoneNumber: phoneNumber, firstName: firstName, lastName: lastName, zipCode: zipCode).save(flush: true, failOnError: true)

                new UserRole(user: user, role: role).save(flush: true, failOnError: true)
                result = [success: true, user: user]
            }
        }catch(Exception e){
            log.error("Login or register error: ", e);
            result = [success: false, message: "The server incounter error stage, please try it again later."]
        }

        return result
    }

    def leaveFeedback(String typeString, String text){
        User user = springSecurityService.getCurrentUser() as User

        FeedbackType type = typeString as FeedbackType
        new Feedback(user: user, type: type, text: text).save(failOnError: true)

        return [success: true]
    }


    def updateProfile(String email, String phoneNumber, String firstName, String lastName, String address, String city, String state, String zipCode){
        def user = springSecurityService.currentUser as User

        try{
            if(email && user.email != email){ user.email = email }
            if(phoneNumber && user.phoneNumber != phoneNumber){ user.phoneNumber = phoneNumber}
            if(firstName && user.firstName != firstName){ user.firstName = firstName }
            if(lastName && user.lastName != lastName){ user.lastName = lastName}
            if(address && user.address != address){ user.address = address}
            if(city && user.city != city){ user.city = city}
            if(state && user.state != state){ user.state = state}
            if(zipCode && user.zipCode != zipCode){ user.zipCode = zipCode}
            if(!user.completed) user.completed = true

            user.save(failOnError: true, flash: true)

            return [success: true, user: user]
        }catch(Exception e){
            log.error("Error on updating user profile. $e.message")
            return[success: false]
        }
    }

    def updateUserRole(String roleString){
        def user = springSecurityService.currentUser as User
        Role role = null
        switch(roleString){
            case "parent":
                role = Role.findByAuthority('ROLE_PARENT')
                break;
            case "nanny":
                role = Role.findByAuthority('ROLE_NANNY')
                break;
        }

        if(role){
            def roles = springSecurityService.getPrincipal().getAuthorities()
            if(!(roles.toString().contains(role.authority))){
                new UserRole(user: user, role: role).save(flush: true, failOnError: true)
                return [success: true]
            }else{
                log.error("User trying add role $user, $role")
            }
        }

        return [success: false]
    }

    def updateRegistration(String registrationId){
        if(!registrationId){
            return [success: false, message: 'need registrationId']
        }
        User user = springSecurityService.getCurrentUser() as User

        if(!user){
            return [success: false, message: "couldn't find your device"]
        }

        user.registrationId = registrationId
        user.save(flush:true, failOnError: true)

        return [success: true]
    }


}
