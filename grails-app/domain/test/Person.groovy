package test

/**
 * @author Noam Y. Tenne
 */
class Person {

    String firstName
    String lastName
    String password

    List<String> nickNames

    String getFullName() {
        return firstName + " " + lastName
    }

    static transients = ['fullName']
    static hasMany = [nickNames: String]

    static mapping = {
        autoImport(false)
    }

    static searchable = {
        root false
    }

    static constraints = {
        password nullable: true
    }
}
