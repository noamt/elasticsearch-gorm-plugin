package test

class Player extends Person {

    static hasMany = [attributes: String]

    static searchable = true

    static mapping = {
        attributes lazy: false, fetch: 'join'
    }

    static constraints = {
    }
}
