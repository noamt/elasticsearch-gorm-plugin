package test

import org.jadira.usertype.dateandtime.joda.PersistentDateTime
import org.joda.time.DateTime

class Store {

    String name
    String description = "A description of a store"
    String owner = "Owner of the store"
    DateTime openingDate

    static searchable = true

    static constraints = {
        name blank: false
        description nullable: true
        owner nullable: false
        openingDate nullable: true
    }

    static mapping = {
        autoImport(false)
        openingDate type: PersistentDateTime
    }

    public String toString() {
        name
    }
}
